package service;

import dataaccess.DataAccess;
import datamodel.*;
import exception.RequestException;
import request.*;
import response.*;

import java.util.UUID;

public class UserService {

    private final DataAccess dataAccess;
    private final RequestException userBadRequestEx;
    private final RequestException userUnauthorizedEx;
    private final RequestException userAlreadyTakenEx;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        userBadRequestEx = new RequestException("Error: bad request", RequestException.Code.BadRequestError);
        userUnauthorizedEx = new RequestException("Error: unauthorized", RequestException.Code.UnauthorizedError);
        userAlreadyTakenEx = new RequestException("Error: already taken", RequestException.Code.AlreadyTakenError);
    }

    public void clear() throws RequestException {
        dataAccess.clear();
    }

    public LoginResponse register(UserData user) throws RequestException {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw userBadRequestEx;
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw userAlreadyTakenEx;
        }
        dataAccess.createUser(user);
        LoginRequest loginRequest = new LoginRequest(user.username(), user.password());

        return login(loginRequest);
    }

    public LoginResponse login(LoginRequest loginRequest) throws RequestException {
        UserData user = dataAccess.getUser(loginRequest.username());
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw userBadRequestEx;
        }
        if (user == null || !loginRequest.password().equals(user.password())) {
            throw userUnauthorizedEx;
        }
        String authToken = generateToken();
        AuthData authData = new AuthData(user.username(), authToken);
        dataAccess.createAuth(authData);
        return new LoginResponse(authData.username(), authData.authToken());
    }

    public void logout(LogoutRequest logoutRequest) throws RequestException {
        AuthData authData = dataAccess.getAuth(logoutRequest.authToken());
        if (authData == null) {
            throw userUnauthorizedEx;
        }
        dataAccess.deleteAuth(authData);
    }


    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
