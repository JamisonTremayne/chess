package service;

import chess.ChessGame;
import dataaccess.*;
import datamodel.*;
import exception.RequestException;
import org.junit.jupiter.api.Test;
import request.*;
import response.*;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private final DataAccess db = new MemoryDataAccess();
    private final UserService userService = new UserService(db);
    private final UserData goodUser = new UserData("joe", "toomanysecrets", "j@j.com");
    private final GameService gameService = new GameService(db);

    @Test
    void createGameSuccessful() throws RequestException {
        LoginResponse authData = userService.register(goodUser);
        String gameName = "my_game";
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName, authData.authToken());
        CreateGameResponse response = gameService.createGame(createGameRequest);
        var newGame = new GameData(response.gameID(), null, null, gameName, new ChessGame());
        assertEquals(db.getGame(response.gameID()), newGame);
    }

    @Test
    void createGameSameName() throws RequestException {
        LoginResponse authData = userService.register(goodUser);
        String gameName = "my_game";
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName, authData.authToken());
        CreateGameResponse gameID_01 = gameService.createGame(createGameRequest);
        CreateGameResponse gameID_02 = gameService.createGame(createGameRequest);
        var game_01 = new GameData(gameID_01.gameID(), null, null, gameName, new ChessGame());
        var game_02 = new GameData(gameID_02.gameID(), null, null, gameName, new ChessGame());
        assertEquals(db.getGame(gameID_01.gameID()), game_01);
        assertEquals(db.getGame(gameID_02.gameID()), game_02);
        assertEquals(db.getGame(gameID_01.gameID()).gameName(), db.getGame(gameID_02.gameID()).gameName());
        assertNotEquals(gameID_01.gameID(), gameID_02.gameID());
    }

    @Test
    void createGameBadRequest() throws RequestException {
        LoginResponse authData = userService.register(goodUser);
        CreateGameRequest createGameRequest = new CreateGameRequest(null, authData.authToken());
        assertThrows(RequestException.class, () -> gameService.createGame(createGameRequest));
    }

    @Test
    void createGameUnauthorized() throws RequestException {
        LoginResponse authData = userService.register(goodUser);
        CreateGameRequest createGameRequest = new CreateGameRequest("my_game", authData.authToken() + "b");
        assertThrows(RequestException.class, () -> gameService.createGame(createGameRequest));
    }

    @Test
    void joinGameSuccess() throws RequestException {
        LoginResponse authData = userService.register(goodUser);
        String gameName = "my_game";
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName, authData.authToken());
        CreateGameResponse response = gameService.createGame(createGameRequest);
        JoinGameRequest joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.WHITE, response.gameID(), authData.authToken());
        gameService.joinGame(joinGameRequest);
        var newGame = db.getGame(response.gameID());
        assertEquals(newGame.whiteUsername(), goodUser.username());
        assertNull(newGame.blackUsername());
    }

    @Test
    void joinGameBadRequest() throws RequestException {
        LoginResponse authData = userService.register(goodUser);
        String gameName = "my_game";
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName, authData.authToken());
        CreateGameResponse response = gameService.createGame(createGameRequest);
        JoinGameRequest joinGameRequest = new JoinGameRequest(null, response.gameID(), authData.authToken());
        assertThrows(RequestException.class, () -> gameService.joinGame(joinGameRequest));
    }

    @Test
    void joinGameUnauthorized() throws RequestException {
        LoginResponse authData = userService.register(goodUser);
        String gameName = "my_game";
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName, authData.authToken());
        CreateGameResponse response = gameService.createGame(createGameRequest);
        JoinGameRequest joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.WHITE, response.gameID(), authData.authToken() + "a");
        assertThrows(RequestException.class, () -> gameService.joinGame(joinGameRequest));
    }

    @Test
    void joinGameAlreadyTaken() throws RequestException {
        LoginResponse authData = userService.register(goodUser);
        String gameName = "my_game";
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName, authData.authToken());
        CreateGameResponse response = gameService.createGame(createGameRequest);
        JoinGameRequest joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.WHITE, response.gameID(), authData.authToken());
        gameService.joinGame(joinGameRequest);
        assertThrows(RequestException.class, () -> gameService.joinGame(joinGameRequest));
    }
}