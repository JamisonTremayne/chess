package dataaccess;

import chess.ChessGame;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;
import exception.RequestException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    private final UserData userExample = new UserData("joe", "toomanysecrets", "j@j.com");
    private final GameData gameExample = new GameData(1, null, null, "game", new ChessGame());
    private final AuthData authExample = new AuthData("joe", "my-authtoken");

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
    void createGame(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createGame(gameExample);
        assertEquals(gameExample.gameID(), db.getGame(gameExample.gameID()).gameID());
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLDataAccess.class, MemoryDataAccess.class})
    void getGame(Class<? extends DataAccess> dbClass) throws RequestException {
        DataAccess db = getDataAccess(dbClass);

        db.createGame(gameExample);
        GameData gotGame = db.getGame(gameExample.gameID());
        assertEquals(gotGame, gameExample);
    }
}