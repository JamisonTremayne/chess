package dataaccess;

import datamodel.UserData;
import exception.RequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    private final UserData userExample = new UserData("joe", "toomanysecrets", "j@j.com");

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
    void getUser() {
    }
}