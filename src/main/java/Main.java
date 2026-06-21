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

        HashSet<String> builtins = new HashSet<>();
        builtins.add("exit");
        builtins.add("echo");
        builtins.add("type");

        while (true) {

            System.out.print("$ ");

            String command = sc.nextLine();

            if (command.isEmpty()) {
                continue;
            }

            String[] parts = command.split(" ");

            // exit
            if (parts[0].equals("exit")) {
                break;
            }

            // echo
            else if (parts[0].equals("echo")) {

                if (parts.length > 1) {
                    System.out.println(command.substring(5));
                } else {
                    System.out.println();
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
                    cmd.add(executable.getAbsolutePath());

                    for (int i = 1; i < parts.length; i++) {
                        cmd.add(parts[i]);
                    }

                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.inheritIO();

                    Process process = pb.start();
                    process.waitFor();
                }
            }
        }

        sc.close();
    }
}