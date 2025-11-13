package ui;

import request.LogoutRequest;
import serverfacade.ServerFacade;

import java.util.Random;

public class PostloginUI extends ClientUI {

    private final String authToken;

    public PostloginUI(ServerFacade serverFacade, String authToken) {
        super(serverFacade, "LOGGED IN");

        this.authToken = authToken;
    }

    @Override
    public String parseCommand(String command) throws Exception {
        String[] commandWords = command.split(" ");
        if (commandWords.length == 0) {
            throw new Exception();
        }
        String commandHead = commandWords[0].toLowerCase(); //NOT CASE SENSITIVE
        switch (commandHead) {
            case "help" -> {
                return help();
            } case "logout" -> {
                return logout();
            } case "create", "create_game", "creategame" -> {
                return createGame(commandWords);
            } case "list", "list_games", "listgames" -> {
                return listGames();
            } case "join", "join_game", "joingame" -> {
                return joinGame(commandWords);
            } case "observe", "observe_game", "observegame" -> {
                return observeGame(commandWords);
            } default -> {
                return invalidCommand(commandHead);
            }
        }
    }

    public String help() {
        String helpString = formatHelp("help", "List available commands.");
        helpString += formatHelp("logout", "Log out of your account.");
        helpString += formatHelp("create <GAME NAME>", "Create a new game.");
        helpString += formatHelp("list", "List all existing games.");
        helpString += formatHelp("join <ID> [WHITE|BLACK]", "Join a game with its given ID as the specified team.");
        helpString += formatHelp("observe <ID>", "Join a game with its given ID as an observer.");
        return helpString;
    }

    private String logout() throws Exception {
        serverFacade.logout(new LogoutRequest(authToken));
        changeUITo(new PreloginUI(serverFacade));
        return EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully logged out.";
    }

    private String createGame(String[] args) {
        return "";
    }

    private String listGames() {
        return "";
    }

    private String joinGame(String[] args) {
        return "";
    }

    private String observeGame(String[] args) {
        return "";
    }
}
