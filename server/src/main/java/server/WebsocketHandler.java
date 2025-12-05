
package server;

import chess.ChessGame;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import datamodel.AuthData;
import datamodel.GameData;
import exception.RequestException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    public final ConcurrentHashMap<Integer, ConcurrentHashMap<Session, Session>>
            connections = new ConcurrentHashMap<>();
    private final DataAccess dataAccess;

    public WebsocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand userCommand = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (userCommand.getCommandType()) {
                case CONNECT -> connectToGame(userCommand, ctx.session);
                case MAKE_MOVE -> makeMove(userCommand, ctx.session);
                case LEAVE -> leaveGame(userCommand, ctx.session);
                case RESIGN -> resignFromGame(userCommand, ctx.session);
            }
        } catch (RequestException ex) {
            ErrorMessage serverError = new ErrorMessage(ex.getMessage());
            directMessage(ctx.session, serverError);
        } catch (InvalidMoveException ex) {
            String message = "Sorry, that move is not valid. Try again to make a valid move.";
            ErrorMessage serverError = new ErrorMessage(message);
            directMessage(ctx.session, serverError);
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connectToGame(UserGameCommand command, Session session) throws RequestException {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            String errorMessage = "Sorry, you are not authorized to make a move.";
            throw new RequestException(errorMessage, RequestException.Code.UnauthorizedError);
        }
        ChessGame.TeamColor team = getTeam(command);
        String message = String.format("%s has joined the game as %s!", auth.username(), teamToString(team));
        Notification serverMessage = new Notification(message);
        broadcast(session, serverMessage, command.getGameID());

        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            String errorMessage = "Sorry, for some reason your game could not be found. Try loading up a new game.";
            throw new RequestException(errorMessage, RequestException.Code.BadRequestError);
        }
        LoadGame gameLoad = new LoadGame(gameData.game());
        directMessage(session, gameLoad);
        add(session, command.getGameID());
    }

    private void makeMove(UserGameCommand command, Session session) throws RequestException, InvalidMoveException {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            String message = "Sorry, you are not authorized to make a move.";
            throw new RequestException(message, RequestException.Code.UnauthorizedError);
        }
        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            String message = "Sorry, for some reason your game could not be found. Try loading up a new game.";
            throw new RequestException(message, RequestException.Code.BadRequestError);
        } else if (gameData.state() == GameData.GameState.COMPLETE) {
            String message = """ 
                    This game has already been completed!
                    Join a new game to play!
                    """;
            ErrorMessage serverError = new ErrorMessage(message);
            directMessage(session, serverError);
            return;
        }

        ChessGame.TeamColor team = getTeam(command);
        if (team == null) {
            String message = """ 
                    Sorry, you cannot make a move as an observer!
                    Join a game as either WHITE or BLACK to play the game.
                    """;
            ErrorMessage serverError = new ErrorMessage(message);
            directMessage(session, serverError);
            return;
        }
        ChessGame game = gameData.game();
        if (game.getTeamTurn() != team) {
            String message = """ 
                    Sorry, it is not your turn!
                    Wait for the other team to go first, and then try again.
                    """;
            ErrorMessage serverError = new ErrorMessage(message);
            directMessage(session, serverError);
            return;
        }
        game.makeMove(command.getMove());
        GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                gameData.gameName(), game, gameData.state());
        dataAccess.updateGame(command.getGameID(), newGameData);
        LoadGame gameLoad = new LoadGame(game);
        broadcast(null, gameLoad, command.getGameID());
        String startPos = formatPosition(command.getMove().getStartPosition());
        String endPos = formatPosition(command.getMove().getEndPosition());
        String message = String.format("%s has made a move %s to %s!", auth.username(), startPos, endPos);
        Notification notification = new Notification(message);
        broadcast(session, notification, command.getGameID());
        checkGameConditions(newGameData);
    }

    private String formatPosition(ChessPosition pos) {
        String positionString = "";
        switch (pos.getColumn()) {
            case 1 -> positionString = "A";
            case 2 -> positionString = "B";
            case 3 -> positionString = "C";
            case 4 -> positionString = "D";
            case 5 -> positionString = "E";
            case 6 -> positionString = "F";
            case 7 -> positionString = "G";
            case 8 -> positionString = "H";
        }
        positionString += pos.getRow();
        return positionString;
    }

    private void leaveGame(UserGameCommand command, Session session) throws RequestException {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        GameData gd = dataAccess.getGame(command.getGameID());
        ChessGame.TeamColor team = getTeam(command);
        if (gd != null) {
            GameData.GameState state = gd.state();
            if (state == GameData.GameState.IN_PROGRESS) {
                state = GameData.GameState.UNFINISHED;
            }
            GameData newGameData = (team == ChessGame.TeamColor.WHITE) ?
                    new GameData(gd.gameID(), null, gd.blackUsername(), gd.gameName(), gd.game(), state) :
                    (team == ChessGame.TeamColor.BLACK) ?
                            new GameData(gd.gameID(), gd.whiteUsername(), null, gd.gameName(), gd.game(), state) :
                            gd;
            dataAccess.updateGame(newGameData.gameID(), newGameData);
        }
        String message = String.format("%s (%s) has left the game.", auth.username(), teamToString(team));
        Notification notification = new Notification(message);
        broadcast(session, notification, command.getGameID());
        remove(session, command.getGameID());
    }

    private void resignFromGame(UserGameCommand command, Session session) throws RequestException {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            String message = "Sorry, you are not authorized to resign.";
            throw new RequestException(message, RequestException.Code.UnauthorizedError);
        }
        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            String message = "Sorry, for some reason your game could not be found. Try loading up a new game.";
            throw new RequestException(message, RequestException.Code.BadRequestError);
        } else if (gameData.state() == GameData.GameState.COMPLETE) {
            String message = """ 
                    This game has already been completed!
                    You can't resign from a game that's already over, silly!
                    Join another game to give up if you want.
                    """;
            ErrorMessage serverError = new ErrorMessage(message);
            directMessage(session, serverError);
            return;
        }
        ChessGame.TeamColor team = getTeam(command);
        if (team == null) {
            String message = """ 
                    Sorry, you cannot resign as an observer!
                    You can still leave if you want with the LEAVE command.
                    """;
            ErrorMessage serverError = new ErrorMessage(message);
            directMessage(session, serverError);
            return;
        }
        ChessGame.TeamColor otherTeam = team == ChessGame.TeamColor.WHITE? ChessGame.TeamColor.BLACK:
                ChessGame.TeamColor.WHITE;
        String message = String.format("%s (%s) has resigned!", auth.username(), teamToString(team));
        message += gameEnd(gameData, otherTeam);
        Notification notification = new Notification(message);
        broadcast(null, notification, command.getGameID());
    }

    private void checkGameConditions(GameData gameData) throws RequestException {
        if (gameData == null) {
            String message = "Sorry, for some reason the game could not be found. Unable to check game conditions.";
            throw new RequestException(message, RequestException.Code.BadRequestError);
        }
        ChessGame game = gameData.game();
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) { // Check for Black Team Win
            String message = String.format("%s is in Checkmate!", teamToString(ChessGame.TeamColor.WHITE));
            message += gameEnd(gameData, ChessGame.TeamColor.BLACK);
            Notification notification = new Notification(message);
            broadcast(null, notification, gameData.gameID());
        } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) { // Check for White Team Win
            String message = String.format("%s is in Checkmate!", teamToString(ChessGame.TeamColor.WHITE));
            message += gameEnd(gameData, ChessGame.TeamColor.WHITE);
            Notification notification = new Notification(message);
            broadcast(null, notification, gameData.gameID());
        } else if (game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            String message = gameEnd(gameData, null); // Check for Stalemate
            Notification notification = new Notification(message);
            broadcast(null, notification, gameData.gameID());
        } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) { // Check for White in Check (but not Checkmate)
            String message = String.format("%s is in Check!", teamToString(ChessGame.TeamColor.WHITE));
            Notification notification = new Notification(message);
            broadcast(null, notification, gameData.gameID());
        } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) { // Check for Black in Check (but not Checkmate)
            String message = String.format("%s is in Check!", teamToString(ChessGame.TeamColor.BLACK));
            Notification notification = new Notification(message);
            broadcast(null, notification, gameData.gameID());
        }
    }

    private String gameEnd(GameData gameData, ChessGame.TeamColor winningTeam) throws RequestException {
        if (gameData == null) {
            String message = "Sorry, for some reason the game could not be found. Unable to end the game.";
            throw new RequestException(message, RequestException.Code.BadRequestError);
        }
        String message = "\n";
        if (winningTeam != null) {
            message += String.format("%s HAS WON!", teamToString(winningTeam));
        } else {
            message += "STALEMATE!";
        }
        GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                gameData.gameName(), gameData.game(), GameData.GameState.COMPLETE);
        dataAccess.updateGame(newGameData.gameID(), newGameData);
        return message;
    }

    private String teamToString(ChessGame.TeamColor teamColor) {
        switch (teamColor) {
            case WHITE -> {
                return "WHITE TEAM";
            }
            case BLACK -> {
                return "BLACK TEAM";
            }
            case null -> {
                return "OBSERVER";
            }

        }
    }

    private ChessGame.TeamColor getTeam(UserGameCommand command) throws RequestException {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        GameData gameData = dataAccess.getGame(command.getGameID());
        ChessGame.TeamColor team = command.getTeam();
        if (team == null && auth != null && gameData != null) {
            if (auth.username().equals(gameData.whiteUsername())) {
                team = ChessGame.TeamColor.WHITE;
            } else if (auth.username().equals(gameData.blackUsername())) {
                team = ChessGame.TeamColor.BLACK;
            }
        }
        return team;
    }
    
    public void add(Session session, Integer gameID) {
        ConcurrentHashMap<Session, Session> connectionGroup = connections.get(gameID);
        if (connectionGroup == null) {
            ConcurrentHashMap<Session, Session> newGroup = new ConcurrentHashMap<>();
            newGroup.put(session, session);
            connections.put(gameID, newGroup);
        } else {
            connectionGroup.put(session, session);
        }
    }

    public void remove(Session session, Integer gameID) {
        ConcurrentHashMap<Session, Session> connectionGroup = connections.get(gameID);
        if (connectionGroup != null) {
            connectionGroup.remove(session);
            if (connectionGroup.isEmpty()) {
                connections.remove(gameID);
            }
        }
    }

    public void broadcast(Session excludeSession, ServerMessage serverMessage, Integer gameID) {
        try {
            String msg = serverMessage.toString();
            for (Integer id : connections.keySet()) {
                ConcurrentHashMap<Session, Session> group = connections.get(id);
                for (Session c : group.values()) {
                    if (c.isOpen() && !c.equals(excludeSession) && gameID.equals(id)) {
                        c.getRemote().sendString(msg);
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Sorry, something went wrong and was unable to process the request.");
        }
    }

    public void directMessage(Session session, ServerMessage serverMessage) {
        try {
            String msg = serverMessage.toString();
            if (session.isOpen()) {
                session.getRemote().sendString(msg);
            }
        } catch (IOException ex) {
            System.out.println("Sorry, something went wrong and was unable to process the request.");
        }
    }
}
