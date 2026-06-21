import java.util.*;
import java.io.*;

public class Main {

    static File findExecutable(String cmd) {
        String path = System.getenv("PATH");
        String[] directories = path.split(":");

        for (String directory : directories) {
            File file = new File(directory, cmd);

            if (file.exists() && file.canExecute()) {
                return file;
            }
        }

        return null;
    }

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        File currentDirectory = new File(System.getProperty("user.dir"));

        HashSet<String> builtins = new HashSet<>();
        builtins.add("exit");
        builtins.add("echo");
        builtins.add("type");
        builtins.add("pwd");
        builtins.add("cd");

        while (true) {

            System.out.print("$ ");

            String command = sc.nextLine();

            if (command.isEmpty()) {
                continue;
            }

            ArrayList<String> tokens = new ArrayList<>();
            StringBuilder current = new StringBuilder();

            boolean inSingleQuote = false;
            boolean inDoubleQuote = false;

            for (int i = 0; i < command.length(); i++) {
                char c = command.charAt(i);

                // Inside single quotes: everything is literal
                if (inSingleQuote) {
                    if (c == '\'') {
                        inSingleQuote = false;
                    } else {
                        current.append(c);
                    }
                    continue;
                }

                // Inside double quotes
                if (inDoubleQuote) {

                    if (c == '\\') {
                        if (i + 1 < command.length()) {
                            char next = command.charAt(i + 1);

                            if (next == '"' || next == '\\') {
                                current.append(next);
                                i++;
                            } else {
                                current.append('\\');
                            }
                        } else {
                            current.append('\\');
                        }
                        continue;
                    }

                    if (c == '"') {
                        inDoubleQuote = false;
                        continue;
                    }

                    current.append(c);
                    continue;
                }

                // Outside quotes
                if (c == '\\') {
                    if (i + 1 < command.length()) {
                        current.append(command.charAt(++i));
                    }
                    continue;
                }

                if (c == '\'') {
                    inSingleQuote = true;
                    continue;
                }

                if (c == '"') {
                    inDoubleQuote = true;
                    continue;
                }

                if (c == ' ') {
                    if (current.length() > 0) {
                        tokens.add(current.toString());
                        current.setLength(0);
                    }
                } else {
                    current.append(c);
                }
            }

            if (current.length() > 0)
                tokens.add(current.toString());

            String[] parts = tokens.toArray(new String[0]);

            int redirectIndex = -1;

            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals(">") || parts[i].equals("1>")) {
                    redirectIndex = i;
                    break;
                }
            }
            // exit
            if (parts[0].equals("exit")) {
                break;
            }

            // echo
            else if (parts[0].equals("echo")) {

                PrintStream out = System.out;

                if (redirectIndex != -1) {
                    out = new PrintStream(new FileOutputStream(parts[redirectIndex + 1]));
                }

                for (int i = 1; i < (redirectIndex == -1 ? parts.length : redirectIndex); i++) {
                    if (i > 1) out.print(" ");
                    out.print(parts[i]);
                }

                out.println();

                if (out != System.out) out.close();

            }

            // pwd
            else if (parts[0].equals("pwd")) {
                System.out.println(currentDirectory.getCanonicalPath());
            }

            // cd
            else if (parts[0].equals("cd")) {
                if (parts.length < 2) continue;

                File newDir = null;
                if (parts[1].equals("~")) {
                    newDir = new File(System.getenv("HOME"));
                }
                else if (parts[1].startsWith("/")) {
                    newDir = new File(parts[1]);
                }
                else {
                    newDir = new File(currentDirectory, parts[1]);
                }

                newDir = newDir.getCanonicalFile();
                if (newDir.exists() && newDir.isDirectory()) {
                currentDirectory = newDir;
                } else {
                System.out.println("cd: " + parts[1] + ": No such file or directory");
                }
            }

            // type
            else if (parts[0].equals("type")) {

                if (parts.length < 2)
                    continue;

                String cmd = parts[1];

                if (builtins.contains(cmd)) {
                    System.out.println(cmd + " is a shell builtin");
                }
                else {

                    File executable = findExecutable(cmd);

                    if (executable != null) {
                        System.out.println(cmd + " is " + executable.getAbsolutePath());
                    }
                    else {
                        System.out.println(cmd + ": not found");
                    }
                }
            }

            // external command
            else {

                File executable = findExecutable(parts[0]);

                if (executable == null) {
                    System.out.println(parts[0] + ": command not found");
                }
                else {

                    List<String> cmd = new ArrayList<>();

                    int end = redirectIndex == -1 ? parts.length : redirectIndex;

                    for (int i = 0; i < end; i++)
                        cmd.add(parts[i]);

                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.directory(currentDirectory);

                    if (redirectIndex != -1) {
                        pb.redirectOutput(new File(parts[redirectIndex + 1]));
                        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                    } else {
                        pb.inheritIO();
                    }

                    Process process = pb.start();
                    process.waitFor();
                }
            }
        }

        sc.close();
    }
}