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
        builtins.add("jobs");

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

            // Check if the command should run in the background
            boolean isBackground = false;
            if (parts.length > 0 && parts[parts.length - 1].equals("&")) {
                isBackground = true;
                // Exclude the '&' from the executable parts
                parts = Arrays.copyOf(parts, parts.length - 1);
            }

            if (parts.length == 0) {
                continue;
            }

            int stdoutRedirect = -1;
            int stderrRedirect = -1;
            boolean appendStdout = false;
            boolean appendStderr = false;

            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals(">") || parts[i].equals("1>")) {
                    stdoutRedirect = i;
                    appendStdout = false;
                } else if (parts[i].equals(">>") || parts[i].equals("1>>")) {
                    stdoutRedirect = i;
                    appendStdout = true;
                } else if (parts[i].equals("2>")) {
                    stderrRedirect = i;
                    appendStderr = false;
                } else if (parts[i].equals("2>>")) {
                    stderrRedirect = i;
                    appendStderr = true;
                }
            }

            // Determine the true end of the command arguments before redirection symbols
            int end = parts.length;
            if (stdoutRedirect != -1 && stderrRedirect != -1) {
                end = Math.min(stdoutRedirect, stderrRedirect);
            } else if (stdoutRedirect != -1) {
                end = stdoutRedirect;
            } else if (stderrRedirect != -1) {
                end = stderrRedirect;
            }

            // exit
            if (parts[0].equals("exit")) {
                break;
            }

            // echo
            else if (parts[0].equals("echo")) {

                PrintStream out = System.out;

                if (stdoutRedirect != -1) {
                    File outFile = new File(parts[stdoutRedirect + 1]);
                    if (outFile.getParentFile() != null) outFile.getParentFile().mkdirs();
                    out = new PrintStream(new FileOutputStream(outFile, appendStdout));
                }

                if (stderrRedirect != -1) {
                    File errFile = new File(parts[stderrRedirect + 1]);
                    if (errFile.getParentFile() != null) errFile.getParentFile().mkdirs();
                    new FileOutputStream(errFile, appendStderr).close(); 
                }

                for (int i = 1; i < end; i++) {
                    if (i > 1) out.print(" ");
                    out.print(parts[i]);
                }

                out.println();

                if (out != System.out)
                    out.close();
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

            // jobs
            else if (parts[0].equals("jobs")) {
                continue;
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

                    for (int i = 0; i < end; i++) {
                        cmd.add(parts[i]);
                    }

                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.directory(currentDirectory);

                    pb.redirectInput(ProcessBuilder.Redirect.INHERIT);

                    if (stdoutRedirect != -1) {
                        File outFile = new File(parts[stdoutRedirect + 1]);
                        if (outFile.getParentFile() != null) outFile.getParentFile().mkdirs();
                        
                        if (appendStdout) {
                            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(outFile));
                        } else {
                            pb.redirectOutput(outFile);
                        }
                    } else {
                        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    }

                    if (stderrRedirect != -1) {
                        File errFile = new File(parts[stderrRedirect + 1]);
                        if (errFile.getParentFile() != null) errFile.getParentFile().mkdirs();
                        
                        if (appendStderr) {
                            pb.redirectError(ProcessBuilder.Redirect.appendTo(errFile));
                        } else {
                            pb.redirectError(errFile);
                        }
                    } else {
                        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                    }

                    Process process = pb.start();
                    
                    if (isBackground) {
                        // Print background job info sequence: [JOB_NUMBER] PID
                        System.out.println("[1] " + process.pid());
                    } else {
                        // Wait only if it's NOT a background job
                        process.waitFor();
                    }
                }
            }
        }

        sc.close();
    }
}