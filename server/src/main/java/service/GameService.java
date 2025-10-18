package service;

import chess.ChessGame;
import dataaccess.*;
import datamodel.*;
import exception.RequestException;
import request.*;
import response.*;

import java.util.ArrayList;

public class GameService {

    private final DataAccess dataAccess;
    private Integer gameCounter = 1000;
    private final RequestException badRequestException;
    private final RequestException unauthorizedException;
    private final RequestException alreadyTakenException;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        badRequestException = new RequestException("Error: bad request", RequestException.Code.BadRequestError);
        unauthorizedException = new RequestException("Error: unauthorized", RequestException.Code.UnauthorizedError);
        alreadyTakenException = new RequestException("Error: already taken", RequestException.Code.AlreadyTakenError);
    }

    public ListGamesResponse listGames(ListGamesRequest request) throws RequestException {
        if (request.authToken() == null || dataAccess.getAuth(request.authToken()) == null) {
            throw unauthorizedException;
        }
        ArrayList<GameData> games = dataAccess.listGames();
        return new ListGamesResponse(games);
    }


    public CreateGameResponse createGame(CreateGameRequest createGameRequest) throws RequestException {
        if (createGameRequest.authToken() == null || createGameRequest.gameName() == null) {
            throw badRequestException;
        }
        AuthData authData = dataAccess.getAuth(createGameRequest.authToken());
        if (authData == null) {
            throw unauthorizedException;
        }
        int gameID = gameCounter++;
        if (dataAccess.getGame(gameID) != null) {
            throw badRequestException;
        }
        GameData newGame = new GameData(gameID, null, null, createGameRequest.gameName(), new ChessGame());
        dataAccess.createGame(newGame);
        return new CreateGameResponse(gameID);
    }

    public void joinGame(JoinGameRequest request) throws RequestException {
        if (request.playerColor() == null || request.authToken() == null) {
            throw badRequestException;
        }
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw unauthorizedException;
        }
        GameData game = dataAccess.getGame(request.gameID());
        if (game == null) {
            throw badRequestException;
        }
        String whiteUser = game.whiteUsername();
        String blackUser = game.blackUsername();
        if (request.playerColor() == ChessGame.TeamColor.WHITE) {
            if (whiteUser != null) {
                throw alreadyTakenException;
            } else {
                whiteUser = authData.username();
            }
        } else {
            if (blackUser != null) {
                throw alreadyTakenException;
            } else {
                blackUser = authData.username();
            }
        }
        GameData updatedGame = new GameData(game.gameID(), whiteUser, blackUser, game.gameName(), game.game());
        dataAccess.updateGame(request.gameID(), updatedGame);
    }
}
