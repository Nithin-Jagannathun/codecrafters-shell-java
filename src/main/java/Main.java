import java.util.*;

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
                if (cmds.contains(command.substring(5))) {
                    System.out.println(command.substring(5) + " is a shell builtin");
                }
                else {
                    System.out.println(command.substring(5) + ": not found");
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
