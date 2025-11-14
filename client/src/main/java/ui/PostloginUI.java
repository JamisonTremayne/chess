package ui;

import chess.ChessGame;
import datamodel.GameData;
import exception.RequestException;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.ListGamesRequest;
import request.LogoutRequest;
import response.ListGamesResponse;
import serverfacade.ServerFacade;

import java.util.HashMap;
import java.util.Objects;

public class PostloginUI extends ClientUI {

    private final HashMap<Integer, Integer> gameMap = new HashMap<>();
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
        serverFacade.createGame(createGameRequest);
        String responseString = EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully created a new game!\n";
        responseString += EscapeSequences.SET_TEXT_COLOR_BLUE + "Type ";
        responseString += EscapeSequences.SET_TEXT_ITALIC + "list" + EscapeSequences.RESET_TEXT_ITALIC;
        responseString += " to find the number for your game to join!";
        return responseString;
    }

    private String listGames() throws RequestException {
        ListGamesRequest request = new ListGamesRequest(authToken);
        ListGamesResponse response = serverFacade.listGames(request);
        gameMap.clear();
        int counter = 0;
        final int gameNameLength = 24;
        final int playerNameLength = 16;
        StringBuilder responseString = new StringBuilder(EscapeSequences.SET_TEXT_COLOR_GREEN + EscapeSequences.SET_TEXT_BOLD);
        responseString.append("Found ").append(response.games().size()).append(" available games:\n");
        responseString.append(EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.RESET_TEXT_BOLD_FAINT);
        for (GameData game : response.games()) {
            counter++;
            gameMap.put(counter, game.gameID());
            responseString.append("     ").append(counter).append(" - ");

            responseString.append("Game Name: ").append(clampString(game.gameName(), gameNameLength));
            String whiteUser = clampString(game.whiteUsername(), playerNameLength);
            if (whiteUser == null) {
                whiteUser = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY +
                        clampString("None", playerNameLength) + EscapeSequences.SET_TEXT_COLOR_BLUE;
            }
            responseString.append(", White Player: ").append(whiteUser);
            String blackUser = clampString(game.blackUsername(), playerNameLength);
            if (blackUser == null) {
                blackUser = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY +
                        clampString("None", playerNameLength) + EscapeSequences.SET_TEXT_COLOR_BLUE;
            }
            responseString.append(", Black Player: ").append(blackUser);
            responseString.append("\n");
        }

        return responseString.toString();
    }

    private String joinGame(String[] args) throws RequestException {
        if (args.length < 3) {
            return formatError("""
                    You did not give enough arguments.
                    To join a game, please give the <GAME ID> and TEAM (WHITE or BLACK), separated by spaces.
                    To get a GAME ID, type list to get currently available games, or type create <GAME NAME> to create a new game.
                    """);
        } else if (args.length > 3) {
            return formatError("""
                    You gave too many arguments.
                    To join a game, please give the <GAME ID> and TEAM (WHITE or BLACK), separated by spaces.
                    To get a GAME ID, type list to get currently available games, or type create <GAME NAME> to create a new game.
                    """);
        }
        String teamString = args[2].toLowerCase();
        if (!Objects.equals(teamString, "white") && !Objects.equals(teamString, "black")) {
            return formatError("""
                    Invalid team name given.
                    Please type either WHITE or BLACK to join the specified team.
                    """);
        }
        ChessGame.TeamColor team = (teamString.equals("white") ? ChessGame.TeamColor.WHITE: ChessGame.TeamColor.BLACK);
        try {
            Integer id = gameMap.get(Integer.parseInt(args[1]));
            if (id == null) {
                return formatError("""
                        Invalid game ID given.
                        Please type a valid number to join as the game's ID.
                        To get a GAME ID, type list to get currently available games, or type create <GAME NAME> to create a new game.
                        """);
            }
            JoinGameRequest request = new JoinGameRequest(team, id, authToken);
            serverFacade.joinGame(request);
            String responseString = EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully joined game ";
            responseString += args[1] + " as " + teamString.toUpperCase() + "!";
            changeUITo(new GameplayUI(serverFacade, authToken, id, team));
            return responseString;
        } catch (NumberFormatException ex) {
            return formatError("""
                    Invalid game ID given.
                    Please type a valid number to join as the game's ID.
                    To get a GAME ID, type list to get currently available games, or type create <GAME NAME> to create a new game.
                    """);
        }
    }

    private String observeGame(String[] args) {
        if (args.length < 2) {
            return formatError("""
                    You did not give enough arguments.
                    To observe a game, please give the <GAME ID>.
                    To get a GAME ID, type list to get currently available games, or type create <GAME NAME> to create a new game.
                    """);
        } else if (args.length > 2) {
            return formatError("""
                    You gave too many arguments.
                    To observe a game, please give the <GAME ID>.
                    To get a GAME ID, type list to get currently available games, or type create <GAME NAME> to create a new game.
                    """);
        }
        try {
            Integer id = gameMap.get(Integer.parseInt(args[1]));
            if (id == null) {
                return formatError("""
                        Invalid game ID given.
                        Please type a valid number to join as the game's ID.
                        To get a GAME ID, type list to get currently available games, or type create <GAME NAME> to create a new game.
                        """);
            }
            changeUITo(new GameplayUI(serverFacade, authToken, id, null));
            return EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully observing game " + args[1] + "!";
        } catch (NumberFormatException ex) {
            return formatError("""
                        Invalid game ID given.
                        Please type a valid number to join as the game's ID.
                        To get a GAME ID, type list to get currently available games, or type create <GAME NAME> to create a new game.
                        """);
        }
    }

    private String clampString(String string, int length) {
        if (string == null) {
            return null;
        }
        int currLength = string.length();
        if (currLength < length) {
            return string + " ".repeat(length - currLength);
        } else {
            String newString = string.substring(0, length - 3);
            newString += "...";
            return newString;
        }
    }
}
