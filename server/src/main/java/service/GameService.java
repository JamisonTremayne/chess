package service;

import chess.ChessGame;
import dataaccess.*;
import datamodel.*;
import exception.RequestException;
import request.CreateGameRequest;

public class GameService {

    private final DataAccess dataAccess;
    private Integer gameCounter = 0;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public int createGame(CreateGameRequest createGameRequest) throws RequestException {
        if (createGameRequest.authToken() == null || createGameRequest.gameName() == null) {
            throw new RequestException("Error: bad request", RequestException.Code.BadRequestError);
        }
        AuthData authData = dataAccess.getAuth(createGameRequest.authToken());
        if (authData == null) {
            throw new RequestException("Error: unauthorized", RequestException.Code.UnauthorizedError);
        }
        int gameID = gameCounter++;
        if (dataAccess.getGame(gameID) != null) {
            throw new RequestException("Error: bad request", RequestException.Code.BadRequestError);
        }
        GameData newGame = new GameData(gameID, null, null, createGameRequest.gameName(), new ChessGame());
        dataAccess.createGame(newGame);
        return gameID;
    }
}
