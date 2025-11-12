package ui;

import java.util.Scanner;

public abstract class ClientUI {

    public String statusMessage = "NO STATUS";
    public boolean quit = false;

    private static final String resetString = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_BOLD_FAINT
            + EscapeSequences.RESET_TEXT_BLINKING + EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_TEXT_ITALIC
            + EscapeSequences.RESET_TEXT_UNDERLINE;

    public void run() throws Exception {
        while (!quit) {
            System.out.printf("[%s] >>> ", statusMessage);
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            String result = parseCommand(command);
            System.out.println(result);
            System.out.println(resetString);
        }
    }

    public abstract String parseCommand(String command) throws Exception;
}
