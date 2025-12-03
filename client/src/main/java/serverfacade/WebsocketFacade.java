package serverfacade;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import exception.RequestException;
import ui.GameplayUI;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebsocketFacade extends Endpoint {

    Session session;
    GameplayUI client;

    public WebsocketFacade(String url, GameplayUI client) {
        this.client = client;
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                client.handleMessage(serverMessage);
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            String message = "Error: Something went wrong setting up the Websocket Facade.\n" + ex.getMessage();
            client.handleMessage(new ServerMessage(ServerMessage.ServerMessageType.ERROR, message));
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connectToGame(String authToken, Integer gameID, ChessGame.TeamColor team) throws RequestException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, team);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new RequestException(ex.getMessage(), RequestException.Code.ServerError);
        }
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move, ChessGame.TeamColor team) throws RequestException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move, team);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new RequestException(ex.getMessage(), RequestException.Code.ServerError);
        }
    }

    public void leaveGame(String authToken, Integer gameID, ChessGame.TeamColor team) throws RequestException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID, team);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new RequestException(ex.getMessage(), RequestException.Code.ServerError);
        }
    }

    public void resign(String authToken, Integer gameID, ChessGame.TeamColor team) throws RequestException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID, team);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new RequestException(ex.getMessage(), RequestException.Code.ServerError);
        }
    }
}
