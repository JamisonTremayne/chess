package ui;

import datamodel.GameData;
import exception.RequestException;
import request.CreateGameRequest;
import request.ListGamesRequest;
import request.LogoutRequest;
import response.CreateGameResponse;
import response.ListGamesResponse;
import serverfacade.ServerFacade;

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
            throw new Exception("Invalid commands.");
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

    private String logout() throws RequestException {
        serverFacade.logout(new LogoutRequest(authToken));
        changeUITo(new PreloginUI(serverFacade));
        return EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully logged out.";
    }

    private String createGame(String[] args) throws RequestException {
        if (args.length < 2) {
            return formatError("""
                    You did not give enough arguments.
                    To create a game, please give the <GAME NAME>.
                    """);
        } else if (args.length > 2) {
            return formatError("""
                    You gave too many arguments.
                    To create a game, please give the <GAME NAME>.
                    """);
        }
        CreateGameRequest createGameRequest = new CreateGameRequest(args[1], authToken);
        CreateGameResponse response = serverFacade.createGame(createGameRequest);
        String responseString = EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully created a new game: \n";
        responseString += EscapeSequences.SET_TEXT_COLOR_BLUE + "Game Name: " + args[1];
        responseString += ", Game ID: " + response.gameID();
        return responseString;
    }

    private String listGames() throws RequestException {
        ListGamesRequest request = new ListGamesRequest(authToken);
        ListGamesResponse response = serverFacade.listGames(request);
        StringBuilder responseString = new StringBuilder(EscapeSequences.SET_TEXT_COLOR_GREEN + EscapeSequences.SET_TEXT_BOLD);
        responseString.append("Found ").append(response.games().size()).append(" available games:\n");
        responseString.append(EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.RESET_TEXT_BOLD_FAINT);
        for (GameData game : response.games()) {
            responseString.append("     - Game Name: ").append(game.gameName());
            responseString.append(", ID: ").append(game.gameID());
            String whiteUser = game.whiteUsername();
            if (whiteUser == null) {
                whiteUser = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + "None" + EscapeSequences.SET_TEXT_COLOR_BLUE;
            }
            responseString.append(", White Player: ").append(whiteUser);
            String blackUser = game.blackUsername();
            if (blackUser == null) {
                blackUser = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + "None" + EscapeSequences.SET_TEXT_COLOR_BLUE;
            }
            responseString.append(", Black Player: ").append(blackUser);
            responseString.append("\n");
        }

        return responseString.toString();
    }

    private String joinGame(String[] args) {
        return "";
    }

    private String observeGame(String[] args) {
        return "";
    }
}
