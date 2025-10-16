package service;

import dataaccess.*;
import datamodel.AuthData;
import datamodel.UserData;
import exception.RequestException;
import org.eclipse.jetty.server.Request;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private final DataAccess db = new MemoryDataAccess();
    private final UserService userService = new UserService(db);
    private final UserData goodUser = new UserData("joe", "toomanysecrets", "j@j.com");

    @Test
    void clear() throws Exception {
        userService.register(goodUser);
        assertEquals(db.getUser(goodUser.username()), goodUser);
        db.clear();
        assertNull(db.getUser(goodUser.username()));
        assertTrue(emptyDatabase());
    }

    @Test
    void registerSuccess() throws Exception {
        AuthData authData = userService.register(goodUser);
        assertNotNull(authData);
        assertEquals(goodUser.username(), authData.username());
        assertFalse(authData.authToken().isEmpty());
    }

    @Test
    void registerInvalidUsername() {
        var badUser = new UserData(null, "toomanysecrets", "j@j.com");
        assertThrows(RequestException.class, () -> userService.register(badUser));
    }

    @Test
    void registerInvalidPassword() {
        var badUser = new UserData("joe", null, "j@j.com");
        assertThrows(RequestException.class, () -> userService.register(badUser));
    }

    @Test
    void registerInvalidEmail() {
        var badUser = new UserData("joe", "toomanysecrets", null);
        assertThrows(RequestException.class, () -> userService.register(badUser));
    }

    @Test
    void registerTwice() throws Exception {
        userService.register(goodUser);
        assertThrows(RequestException.class, () -> userService.register(goodUser));
    }

    @Test
    void loginSuccess() throws RequestException {
        userService.register(goodUser);
        AuthData authData = userService.login(goodUser);
        assertEquals(authData.username(), goodUser.username());
        assertNotNull(authData.authToken());
    }

    @Test
    void loginBadRequest() throws RequestException {
        userService.register(goodUser);
        assertThrows(RequestException.class, () -> userService.login(new UserData(null, null, null)));
    }

    @Test
    void loginUnauthorized() throws RequestException {
        userService.register(goodUser);
        assertThrows(RequestException.class, () -> userService.login(new UserData("jill", "home", null)));
        assertThrows(RequestException.class, () -> userService.login(new UserData("joe", "idk", null)));
    }

    @Test
    void logoutSuccess() throws RequestException {
        userService.register(goodUser);
        AuthData loginResult = userService.login(goodUser);
        userService.logout(loginResult.authToken());
        assertNull(db.getAuth(loginResult.authToken()));
    }

    @Test
    void logoutNotLoggedIn() throws RequestException {
        AuthData authData = userService.register(goodUser);
        assertThrows(RequestException.class, () -> userService.logout(authData.authToken()));
    }

    @Test
    void logoutWrongToken() throws RequestException {
        userService.register(goodUser);
        AuthData loginResult = userService.login(goodUser);
        assertThrows(RequestException.class, () -> userService.logout(loginResult.authToken() + "a"));
    }

    private boolean emptyDatabase() {
        DataAccess emptyDb = new MemoryDataAccess();
        return emptyDb.equals(db);
    }
}