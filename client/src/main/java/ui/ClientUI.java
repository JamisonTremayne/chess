package ui;

import exception.RequestException;
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
            try {
                String result = parseCommand(command);
                System.out.println(result);
            } catch (RequestException ex) {
                System.out.println(formatError(handleExceptions(ex, command)));
            }
                System.out.print(resetString);
        }
        if (toUI != null) {
            toUI.run();
        }
    }

    public void changeUITo(ClientUI toUI) {
        quit = true;
        this.toUI = toUI;
    }

    public String invalidCommand(String head) {
        String invalidString = EscapeSequences.SET_TEXT_COLOR_RED + EscapeSequences.SET_TEXT_ITALIC;
        invalidString += head + EscapeSequences.RESET_TEXT_ITALIC;
        invalidString += " is not a valid command." + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        invalidString += " Try typing ";
        invalidString += EscapeSequences.SET_TEXT_BOLD + EscapeSequences.SET_TEXT_COLOR_GREEN;
        invalidString += "help" + EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        invalidString += " to get a list of valid commands :)";
        return invalidString;
    }

    public String formatHelp(String head, String description) {
        String helpString = EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.SET_TEXT_BOLD;
        helpString += "     " + head;
        helpString += EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.SET_TEXT_COLOR_MAGENTA;
        helpString += " - " + description + "\n";
        return helpString;
    }

    public String formatError(String description) {
        String errorString = EscapeSequences.SET_TEXT_COLOR_RED + EscapeSequences.SET_TEXT_BOLD;
        errorString += "ERROR: " + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        errorString += EscapeSequences.RESET_TEXT_BOLD_FAINT + description;
        return errorString;
    }

    private String handleExceptions(RequestException ex, String command) {
        String[] commandWords = command.split(" ");
        if (commandWords.length == 0) {
            return "Invalid command input.";
        }
        String commandHead = commandWords[0].toLowerCase();
        switch (ex.toHttpStatusCode()) {
            case 400 -> { // BAD REQUEST
                return "Invalid request made.";
            } case 401 -> { // UNAUTHORIZED
                if (commandHead.equals("login")) {
                    return """
                            Not authorized to log in.
                            Make sure your username and password are correct, or register if you have not registered yet.
                            """;
                }
                return "Not authorized.";
            } case 403 -> { // ALREADY TAKEN
                if (commandHead.equals("register")) {
                    return """
                            An account with this username already exists!
                            Try creating your account with another username.
                            """;
                } else if (commandHead.equals("join") || commandHead.equals("join_game") || commandHead.equals("joingame")) {
                    return """
                            A user is already playing as that color in that game!
                            Try joining the other team, joining as an observer, or joining a different game.
                            """;
                }
                return "Input data is already taken.";
            } default -> {
                return "Unexpected error encountered. Sorry for the inconvenience :(";
            }
        }
    }

    public abstract String help();

    public abstract String parseCommand(String command) throws Exception;
}
