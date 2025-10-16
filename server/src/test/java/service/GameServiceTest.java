package service;

import chess.ChessGame;
import dataaccess.*;
import datamodel.*;
import exception.RequestException;
import org.junit.jupiter.api.Test;
import request.CreateGameRequest;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private final DataAccess db = new MemoryDataAccess();
    private final UserService userService = new UserService(db);
    private final UserData goodUser = new UserData("joe", "toomanysecrets", "j@j.com");
    private final GameService gameService = new GameService(db);

    @Test
    void createGameSuccessful() throws RequestException {
        AuthData authData = userService.register(goodUser);
        String gameName = "my_game";
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName, authData.authToken());
        int gameID = gameService.createGame(createGameRequest);
        var newGame = new GameData(gameID, null, null, gameName, new ChessGame());
        assertEquals(db.getGame(gameID), newGame);
    }

    @Test
    void createGameSameName() throws RequestException {
        AuthData authData = userService.register(goodUser);
        String gameName = "my_game";
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName, authData.authToken());
        int gameID_01 = gameService.createGame(createGameRequest);
        int gameID_02 = gameService.createGame(createGameRequest);
        var game_01 = new GameData(gameID_01, null, null, gameName, new ChessGame());
        var game_02 = new GameData(gameID_02, null, null, gameName, new ChessGame());
        assertEquals(db.getGame(gameID_01), game_01);
        assertEquals(db.getGame(gameID_02), game_02);
        assertEquals(db.getGame(gameID_01).gameName(), db.getGame(gameID_02).gameName());
        assertNotEquals(gameID_01, gameID_02);
    }

    @Test
    void createGameBadRequest() throws RequestException {
        AuthData authData = userService.register(goodUser);
        CreateGameRequest createGameRequest = new CreateGameRequest(null, authData.authToken());
        assertThrows(RequestException.class, () -> gameService.createGame(createGameRequest));
    }

    @Test
    void createGameUnauthorized() throws RequestException {
        AuthData authData = userService.register(goodUser);
        CreateGameRequest createGameRequest = new CreateGameRequest("my_game", authData.authToken() + "b");
        assertThrows(RequestException.class, () -> gameService.createGame(createGameRequest));
    }
}