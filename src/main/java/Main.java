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

            if (command.equals("exit")) break;
            else if (command.startsWith("type ")) {
                String cmd = command.substring(5);
                if (cmds.contains(cmd)) {
                    System.out.println(cmd + " is a shell builtin");
                }
                else {
                    
                    String path = System.getenv("PATH");
                    String[] directories = path.split(":");

                    boolean found = false;

                    for(String directory: directories) {
                        File file = new File(directory, cmd);
                        if (file.exists() && file.canExecute()) {
                            found = true;
                            System.out.println(cmd + " is " + file.getAbsolutePath());
                            break;
                        }
                    }

                    if (!found) System.out.println(cmd  + ": not found");
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
