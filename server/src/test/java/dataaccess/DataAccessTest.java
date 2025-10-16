package dataaccess;

import datamodel.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    @Test
    void clear() {
        DataAccess db = new MemoryDataAccess();
        db.createUser(new UserData("joe", "j@j.com", "toomanysecrets"));
        db.clear();
        assertNull(db.getUser("joe"));
    }

    @Test
    void createUser() {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "j@j.com", "toomanysecrets");
        db.createUser(user);
        assertEquals(user, db.getUser(user.username()));
    }

    @Test
    void getUser() {
    }
}