import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        HashSet<String> cmds = new HashSet<>();
        cmds.add("exit");
        cmds.add("type");
        cmds.add("echo");

        while (true) {
            System.out.print("$ ");
            String command = sc.nextLine();
            String[] parts = command.split(" ");

            if (command.equals("exit")) break;
            else if (command.startsWith("type ")) {
                if (cmds.contains(parts[0])) {
                    System.out.println(parts[0] + " is a shell builtin");
                }
                else {         
                    String path = System.getenv("PATH");
                    String[] directories = path.split(":");
                    File executable = null;

                    boolean found = false;

                    for(String directory: directories) {
                        File file = new File(directory, parts[0]);
                        if (file.exists() && file.canExecute()) {
                            found = true;
                            executable = file;
                            break;
                        }
                    }

                    if (found) {
                        ArrayList<String> cmd = new ArrayList<>();
                        cmd.add(executable.getAbsolutePath());

                        for(int i = 1; i < parts.length; i++) {
                            cmd.add(parts[i]);
                        }

                        ProcessBuilder pb = new ProcessBuilder(cmd);
                        pb.inheritIO();

                        Process p = pb.start();
                        p.waitFor();
                    } 
                    else {
                        System.out.println(parts[0]  + ": command not found");
                    }
                }
            }
            else if (command.startsWith("echo ")){
                System.out.println(command.substring(5));
            }
            else {
                System.out.println(command + ": command not found");
            }
        }
    }
}
