package service;

import chess.ChessGame;
import dataaccess.*;
import datamodel.*;
import exception.RequestException;
import org.junit.jupiter.api.Test;

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
        int gameID = gameService.createGame(authData.authToken(), gameName);
        var newGame = new GameData(gameID, null, null, gameName, new ChessGame());
        assertEquals(db.getGame(gameID), newGame);
    }

    @Test
    void createGameSameName() throws RequestException {
        AuthData authData = userService.register(goodUser);
        String gameName = "my_game";
        int gameID_01 = gameService.createGame(authData.authToken(), gameName);
        int gameID_02 = gameService.createGame(authData.authToken(), gameName);
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
        assertThrows(RequestException.class, () -> gameService.createGame(authData.authToken(), null));
    }

    @Test
    void createGameUnauthorized() throws RequestException {
        AuthData authData = userService.register(goodUser);
        assertThrows(RequestException.class, () -> gameService.createGame(authData.authToken() + "b", "my_game"));
    }
}