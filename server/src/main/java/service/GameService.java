package service;

import chess.ChessGame;
import dataaccess.*;
import datamodel.*;
import exception.RequestException;
import response.*;
import request.*;

import java.util.ArrayList;

public class GameService {

    private final DataAccess dataAccess;
    private Integer gameCounter = 1000;
    private final RequestException gameBadRequestEx;
    private final RequestException gameUnauthorizedEx;
    private final RequestException gameAlreadyTakenEx;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        gameBadRequestEx = new RequestException("Error: bad request", RequestException.Code.BadRequestError);
        gameUnauthorizedEx = new RequestException("Error: unauthorized", RequestException.Code.UnauthorizedError);
        gameAlreadyTakenEx = new RequestException("Error: already taken", RequestException.Code.AlreadyTakenError);
    }

    public ListGamesResponse listGames(ListGamesRequest request) throws RequestException {
        if (request.authToken() == null || dataAccess.getAuth(request.authToken()) == null) {
            throw gameUnauthorizedEx;
        }
        ArrayList<GameData> games = dataAccess.listGames();
        return new ListGamesResponse(games);
    }


    public CreateGameResponse createGame(CreateGameRequest createGameRequest) throws RequestException {
        if (createGameRequest.authToken() == null || createGameRequest.gameName() == null) {
            throw gameBadRequestEx;
        }
        AuthData authData = dataAccess.getAuth(createGameRequest.authToken());
        if (authData == null) {
            throw gameUnauthorizedEx;
        }
        int gameID = gameCounter++;
        while (dataAccess.getGame(gameID) != null) {
            gameID = gameCounter++;
        }
        GameData newGame = new GameData(gameID, null, null, createGameRequest.gameName(), new ChessGame());
        dataAccess.createGame(newGame);
        return new CreateGameResponse(gameID);
    }

    public void joinGame(JoinGameRequest request) throws RequestException {
        if (request.playerColor() == null || request.authToken() == null) {
            throw gameBadRequestEx;
        }
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw gameUnauthorizedEx;
        }
        GameData game = dataAccess.getGame(request.gameID());
        if (game == null) {
            throw gameBadRequestEx;
        }
        String whiteUser = game.whiteUsername();
        String blackUser = game.blackUsername();
        if (request.playerColor() == ChessGame.TeamColor.WHITE) {
            if (whiteUser != null) {
                throw gameAlreadyTakenEx;
            } else {
                whiteUser = authData.username();
            }
        } else {
            if (blackUser != null) {
                throw gameAlreadyTakenEx;
            } else {
                blackUser = authData.username();
            }
        }
        GameData updatedGame = new GameData(game.gameID(), whiteUser, blackUser, game.gameName(), game.game());
        dataAccess.updateGame(request.gameID(), updatedGame);
    }
}
