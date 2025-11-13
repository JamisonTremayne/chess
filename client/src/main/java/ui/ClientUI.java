package ui;

import serverfacade.ServerFacade;

import java.util.Scanner;

public abstract class ClientUI {

    private final String statusMessage;
    private ClientUI toUI = null;
    public boolean quit = false;
    public ServerFacade serverFacade;

    public ClientUI(ServerFacade serverFacade, String statusMessage) {
        this.statusMessage = statusMessage;
        this.serverFacade = serverFacade;
    }

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
        if (toUI != null) {
            toUI.run();
        }
    }

    public void changeUITo(ClientUI toUI) {
        quit = true;
        this.toUI = toUI;
    }

    public abstract String parseCommand(String command) throws Exception;
}
