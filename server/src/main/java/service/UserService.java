package service;

import dataaccess.DataAccess;
import datamodel.*;
import exception.RequestException;
import request.*;

import java.util.UUID;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() {
        dataAccess.clear();
    }

    public AuthData register(UserData user) throws RequestException {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new RequestException("Error: bad request", RequestException.Code.BadRequestError);
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new RequestException("Error: already taken", RequestException.Code.AlreadyExistsError);
        }
        dataAccess.createUser(user);
        LoginRequest loginRequest = new LoginRequest(user.username(), user.password());
        return login(loginRequest);
    }

    public AuthData login(LoginRequest loginRequest) throws RequestException {
        UserData user = dataAccess.getUser(loginRequest.username());
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw new RequestException("Error: bad request", RequestException.Code.BadRequestError);
        }
        if (user == null || !loginRequest.password().equals(user.password())) {
            throw new RequestException("Error: unauthorized", RequestException.Code.UnauthorizedError);
        }
        String authToken = generateToken();
        AuthData authData = new AuthData(user.username(), authToken);
        dataAccess.createAuth(authData);
        return authData;
    }

    public void logout(LogoutRequest logoutRequest) throws RequestException {
        AuthData authData = dataAccess.getAuth(logoutRequest.authToken());
        if (authData == null) {
            throw new RequestException("Error: unauthorized", RequestException.Code.UnauthorizedError);
        }
        dataAccess.deleteAuth(authData);
    }


    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
