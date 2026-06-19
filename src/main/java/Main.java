import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String command = sc.next()

            if (command.equals("exit")) break;
            else if (command.equals("echo")){
                String line = sc.nextLine();
                System.out.println(line);
            }
            else {
                System.out.println(command + ": command not found");
            }
        }
    }
}
