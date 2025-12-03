
package server;

import chess.ChessGame;
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
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    public final ConcurrentHashMap<Session, Session> connections = new ConcurrentHashMap<>();
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
            ServerMessage serverError = new ServerMessage(ServerMessage.ServerMessageType.ERROR, ex.getMessage());
            directMessage(ctx.session, serverError);
        } catch (InvalidMoveException ex) {
            String message = "Sorry, that move is not valid. Try again to make a valid move.";
            ServerMessage serverError = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            directMessage(ctx.session, serverError);
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connectToGame(UserGameCommand command, Session session) throws RequestException {
        add(session);
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            String errorMessage = "Sorry, you are not authorized to make a move.";
            throw new RequestException(errorMessage, RequestException.Code.UnauthorizedError);
        }
        String message = String.format("%s has joined the game as %s!", auth.username(), teamToString(command.getTeam()));
        ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        broadcast(session, serverMessage);

        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            String errorMessage = "Sorry, for some reason your game could not be found. Try loading up a new game.";
            throw new RequestException(errorMessage, RequestException.Code.BadRequestError);
        }
        String gameJson = new Gson().toJson(gameData.game());
        ServerMessage gameLoad = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameJson);
        directMessage(session, gameLoad);
    }

    private void makeMove(UserGameCommand command, Session session) throws RequestException, InvalidMoveException {
        if (command.getTeam() == null) {
            String message = """ 
                    Sorry, you cannot make a move as an observer!
                    Join a game as either WHITE or BLACK to play the game.
                    """;
            ServerMessage serverError = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            directMessage(session, serverError);
            return;
        }
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
            ServerMessage serverError = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            directMessage(session, serverError);
            return;
        }
        ChessGame game = gameData.game();
        if (game.getTeamTurn() != command.getTeam()) {
            String message = """ 
                    Sorry, it is not your turn!
                    Wait for the other team to go first, and then try again.
                    """;
            ServerMessage serverError = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            directMessage(session, serverError);
            return;
        }
        game.makeMove(command.getMove());
        GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                gameData.gameName(), game, gameData.state());
        dataAccess.updateGame(command.getGameID(), newGameData);
        String gameJson = new Gson().toJson(game);
        ServerMessage gameLoad = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameJson);
        broadcast(null, gameLoad);
        String startPos = command.getMove().getStartPosition().toString();
        String endPos = command.getMove().getEndPosition().toString();
        String message = String.format("%s moved %s to %s!", auth.username(), startPos, endPos);
        ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        broadcast(session, serverMessage);
        checkGameConditions(newGameData);
    }

    private void leaveGame(UserGameCommand command, Session session) throws RequestException {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        GameData gd = dataAccess.getGame(command.getGameID());
        if (gd != null) {
            GameData.GameState state = gd.state();
            if (state == GameData.GameState.IN_PROGRESS) {
                state = GameData.GameState.UNFINISHED;
            }
            GameData newGameData = (command.getTeam() == ChessGame.TeamColor.WHITE) ?
                    new GameData(gd.gameID(), null, gd.blackUsername(), gd.gameName(), gd.game(), state) :
                    (command.getTeam() == ChessGame.TeamColor.BLACK) ?
                            new GameData(gd.gameID(), gd.whiteUsername(), null, gd.gameName(), gd.game(), state) :
                            gd;
            dataAccess.updateGame(newGameData.gameID(), newGameData);
        }
        String message = String.format("%s (%s) has left the game.", auth.username(), teamToString(command.getTeam()));
        ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        broadcast(session, serverMessage);
        remove(session);
    }

    private void resignFromGame(UserGameCommand command, Session session) throws RequestException {
        if (command.getTeam() == null) {
            String message = """ 
                    Sorry, you cannot resign as an observer!
                    You can still leave if you want with the LEAVE command.
                    """;
            ServerMessage serverError = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            directMessage(session, serverError);
            return;
        }
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
            ServerMessage serverError = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            directMessage(session, serverError);
            return;
        }
        ChessGame.TeamColor otherTeam = command.getTeam() == ChessGame.TeamColor.WHITE? ChessGame.TeamColor.BLACK:
                ChessGame.TeamColor.WHITE;
        String message = String.format("%s (%s) has resigned!", auth.username(), teamToString(command.getTeam()));
        ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        broadcast(session, serverMessage);
        gameEnd(gameData, otherTeam);
    }

    private void checkGameConditions(GameData gameData) throws RequestException {
        if (gameData == null) {
            String message = "Sorry, for some reason the game could not be found. Unable to check game conditions.";
            throw new RequestException(message, RequestException.Code.BadRequestError);
        }
        ChessGame game = gameData.game();
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) { // Check for Black Team Win
            String message = String.format("%s is in Checkmate!", teamToString(ChessGame.TeamColor.WHITE));
            ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            broadcast(null, serverMessage);
            gameEnd(gameData, ChessGame.TeamColor.BLACK);
        } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) { // Check for White Team Win
            String message = String.format("%s is in Checkmate!", teamToString(ChessGame.TeamColor.WHITE));
            ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            broadcast(null, serverMessage);
            gameEnd(gameData, ChessGame.TeamColor.WHITE);
        } else if (game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            gameEnd(gameData, null); // Check for Stalemate
        } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) { // Check for White in Check (but not Checkmate)
            String message = String.format("%s is in Check!", teamToString(ChessGame.TeamColor.WHITE));
            ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            broadcast(null, serverMessage);
        } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) { // Check for Black in Check (but not Checkmate)
            String message = String.format("%s is in Check!", teamToString(ChessGame.TeamColor.BLACK));
            ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            broadcast(null, serverMessage);
        }
    }

    private void gameEnd(GameData gameData, ChessGame.TeamColor winningTeam) throws RequestException {
        if (gameData == null) {
            String message = "Sorry, for some reason the game could not be found. Unable to end the game.";
            throw new RequestException(message, RequestException.Code.BadRequestError);
        }
        String message;
        if (winningTeam != null) {
            message = String.format("%s HAS WON!", teamToString(winningTeam));
        } else {
            message = "STALEMATE!";
        }
        ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        broadcast(null, serverMessage);
        GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                gameData.gameName(), gameData.game(), GameData.GameState.COMPLETE);
        dataAccess.updateGame(newGameData.gameID(), newGameData);
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
    
    public void add(Session session) {
        connections.put(session, session);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void broadcast(Session excludeSession, ServerMessage serverMessage) {
        try {
            String msg = serverMessage.getMessage();
            for (Session c : connections.values()) {
                if (c.isOpen() && !c.equals(excludeSession)) {
                    c.getRemote().sendString(msg);
                }
            }
        } catch (IOException ex) {
            System.out.println("Sorry, something went wrong and was unable to process the request.");
        }
    }

    public void directMessage(Session session, ServerMessage serverMessage) {
        try {
            String msg = serverMessage.getMessage();
            if (session.isOpen()) {
                session.getRemote().sendString(msg);
            }
        } catch (IOException ex) {
            System.out.println("Sorry, something went wrong and was unable to process the request.");
        }
    }
}
