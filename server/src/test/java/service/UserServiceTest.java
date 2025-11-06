package service;

import dataaccess.*;
import datamodel.*;
import exception.RequestException;
import org.junit.jupiter.api.Test;
import response.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private final DataAccess db = new MemoryDataAccess();
    private final UserService userService = new UserService(db);
    private final UserData goodUser = new UserData("joe", "toomanysecrets", "j@j.com");

    @Test
    void clear() throws Exception {
        userService.register(goodUser);
        db.clear();
        assertNull(db.getUser(goodUser.username()));
        assertTrue(emptyDatabase());
    }

    @Test
    void registerSuccess() throws Exception {
        LoginResponse authData = userService.register(goodUser);
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
        LoginResponse authData = userService.register(goodUser);
        assertEquals(authData.username(), goodUser.username());
        assertNotNull(authData.authToken());
    }

    @Test
    void loginBadRequest() throws RequestException {
        userService.register(goodUser);
        assertThrows(RequestException.class, () -> userService.login(new LoginRequest(null, null)));
    }

    @Test
    void loginUnauthorized() throws RequestException {
        userService.register(goodUser);
        assertThrows(RequestException.class, () -> userService.login(new LoginRequest("jill", "home")));
        assertThrows(RequestException.class, () -> userService.login(new LoginRequest("joe", "idk")));
    }

    @Test
    void logoutSuccess() throws RequestException {
        LoginResponse loginResult = userService.register(goodUser);
        userService.logout(new LogoutRequest(loginResult.authToken()));
        assertNull(db.getAuth(loginResult.authToken()));
    }

    @Test
    void logoutTwice() throws RequestException {
        LoginResponse authData = userService.register(goodUser);
        LogoutRequest logoutRequest = new LogoutRequest(authData.authToken());
        userService.logout(logoutRequest);
        assertThrows(RequestException.class, () -> userService.logout(logoutRequest));
    }

    @Test
    void logoutWrongToken() throws RequestException {
        LoginResponse loginResult = userService.register(goodUser);
        LogoutRequest logoutRequest = new LogoutRequest(loginResult.authToken() + "a");
        assertThrows(RequestException.class, () -> userService.logout(logoutRequest));
    }

    private boolean emptyDatabase() {
        DataAccess emptyDb = new MemoryDataAccess();
        return emptyDb.equals(db);
    }
}