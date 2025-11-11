package ui;

import java.util.Scanner;

public abstract class ClientUI {

    public void run() {
        boolean quit = false;
        while (!quit) {
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            String result = parseCommand(command);
            System.out.println(result);
        }
    }

    public abstract String parseCommand(String command);
}
