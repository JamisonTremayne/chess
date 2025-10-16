package service;

import chess.ChessGame;
import dataaccess.*;
import datamodel.*;
import exception.RequestException;

public class GameService {

    private final DataAccess dataAccess;
    private Integer gameCounter = 0;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public int createGame(String authToken, String gameName) throws RequestException {
        if (authToken == null || gameName == null) {
            throw new RequestException("Error: bad request", RequestException.Code.BadRequestError);
        }
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new RequestException("Error: unauthorized", RequestException.Code.UnauthorizedError);
        }
        int gameID = gameCounter++;
        if (dataAccess.getGame(gameID) != null) {
            throw new RequestException("Error: bad request", RequestException.Code.BadRequestError);
        }
        GameData newGame = new GameData(gameID, null, null, gameName, new ChessGame());
        dataAccess.createGame(newGame);
        return gameID;
    }
}
