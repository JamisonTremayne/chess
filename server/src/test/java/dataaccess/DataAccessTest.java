package dataaccess;

import chess.ChessGame;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;
import exception.RequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    private final UserData userExample = new UserData("joe", "toomanysecrets", "j@j.com");
    private final GameData gameExample = new GameData(1, null, null, "game", new ChessGame());
    private final GameData otherGameExample = new GameData(2, "bib", null, "nono", new ChessGame());
    private final AuthData authExample = new AuthData("joe", "my-authtoken");

    private final UserData badUser = new UserData(null, null, null);
    private final GameData badGame = new GameData(-1, null, null, null, null);
    private final AuthData badAuth = new AuthData(null, null);

    private DataAccess getDataAccess(Class<? extends DataAccess> databaseClass) throws RequestException {
        DataAccess db;
        if (databaseClass.equals(SQLDataAccess.class)) {
            db = new SQLDataAccess();
        } else {
            db = new MemoryDataAccess();
        }
        db.clear();
        return db;
    }

    @BeforeEach
    void clearDatabase() throws RequestException {
        DataAccess db = new SQLDataAccess();
        db.clear();
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void clear(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createUser(userExample);
        db.clear();
        assertNull(db.getUser("joe"));
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void createUser(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createUser(userExample);
        assertEquals(userExample.username(), db.getUser(userExample.username()).username());
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void createUserFail(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        assertThrows(RequestException.class, () -> db.createUser(badUser));
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void getUser(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createUser(userExample);
        UserData gotUser = db.getUser(userExample.username());
        assertEquals(gotUser.username(), userExample.username());
        assertEquals(gotUser.email(), userExample.email());
        assertEquals(gotUser.password(), userExample.password());
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void getUserFail(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createUser(userExample);
        assertNull(db.getUser(null));
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void createGame(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createGame(gameExample);
        assertEquals(gameExample.gameID(), db.getGame(gameExample.gameID()).gameID());
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void createGameFail(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        assertThrows(RequestException.class, () -> db.createGame(badGame));
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void getGame(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createGame(gameExample);
        GameData gotGame = db.getGame(gameExample.gameID());
        assertEquals(gotGame, gameExample);
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void getGameFail(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createGame(gameExample);
        assertNull(db.getGame(-1));
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void listGames(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createGame(gameExample);
        db.createGame((otherGameExample));
        ArrayList<GameData> gameList = db.listGames();
        assertEquals(2, gameList.size());
        assertNotEquals(gameList.get(0), gameList.get(1));
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void listGamesFail(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        assertEquals(new ArrayList<>(), db.listGames());
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void updateGame(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createGame(gameExample);
        db.updateGame(gameExample.gameID(), otherGameExample);
        GameData gotGame = db.getGame(otherGameExample.gameID());
        assertEquals(otherGameExample, gotGame);
        assertNotEquals(gameExample, gotGame);
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void updateGameFail(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createGame(gameExample);
        db.updateGame(-1, otherGameExample);
        assertNotEquals(otherGameExample, db.getGame(gameExample.gameID()));
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void createAuth(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createAuth(authExample);
        assertEquals(authExample.authToken(), db.getAuth(authExample.authToken()).authToken());
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void createAuthFail(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        assertThrows(RequestException.class, () -> db.createAuth(badAuth));

    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void getAuth(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createAuth(authExample);
        AuthData gotAuth = db.getAuth(authExample.authToken());
        assertEquals(gotAuth, authExample);
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void getAuthFail(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createAuth(authExample);
        assertNull(db.getAuth("random-authtoken"));
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void deleteAuth(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createAuth(authExample);
        db.deleteAuth(authExample);
        assertNull(db.getAuth(authExample.authToken()));
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void deleteAuthFail(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createAuth(authExample);
        db.deleteAuth(badAuth);
        assertEquals(authExample, db.getAuth(authExample.authToken()));
    }
}
