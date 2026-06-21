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

            for (int i = 0; i < command.length(); i++) {
                char c = command.charAt(i);

                if (c == '\'') {
                    inSingleQuote = !inSingleQuote;
                }
                else if (c == ' ' && !inSingleQuote) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                }
                else {
                    current.append(c);
                }
            }

            if (current.length() > 0)
            tokens.add(current.toString());

            String[] parts = tokens.toArray(new String[0]);

            // exit
            if (parts[0].equals("exit")) {
                break;
            }

            // echo
            else if (parts[0].equals("echo")) {

                for (int i = 1; i < parts.length; i++) {
                    if (i > 1) System.out.print(" ");
                    System.out.print(parts[i]);
                }
                System.out.println();

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
                    List<String> cmd = new ArrayList<>(Arrays.asList(parts));

                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.directory(currentDirectory);
                    pb.inheritIO();

                    Process process = pb.start();
                    process.waitFor();
                }
            }
        }

        sc.close();
    }
}