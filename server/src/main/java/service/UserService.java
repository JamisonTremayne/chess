package service;

import dataaccess.DataAccess;
import datamodel.*;
import exception.RequestException;
import request.*;
import response.*;

import java.util.UUID;

public class UserService {

    private final DataAccess dataAccess;
    private final RequestException badRequestException;
    private final RequestException unauthorizedException;
    private final RequestException alreadyTakenException;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        badRequestException = new RequestException("Error: bad request", RequestException.Code.BadRequestError);
        unauthorizedException = new RequestException("Error: unauthorized", RequestException.Code.UnauthorizedError);
        alreadyTakenException = new RequestException("Error: already taken", RequestException.Code.AlreadyTakenError);
    }

    public void clear() {
        dataAccess.clear();
    }

    public LoginResponse register(UserData user) throws RequestException {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw badRequestException;
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw alreadyTakenException;
        }
        dataAccess.createUser(user);
        LoginRequest loginRequest = new LoginRequest(user.username(), user.password());

        return login(loginRequest);
    }

    public LoginResponse login(LoginRequest loginRequest) throws RequestException {
        UserData user = dataAccess.getUser(loginRequest.username());
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw badRequestException;
        }
        if (user == null || !loginRequest.password().equals(user.password())) {
            throw unauthorizedException;
        }
        String authToken = generateToken();
        AuthData authData = new AuthData(user.username(), authToken);
        dataAccess.createAuth(authData);
        return new LoginResponse(authData.username(), authData.authToken());
    }

    public void logout(LogoutRequest logoutRequest) throws RequestException {
        AuthData authData = dataAccess.getAuth(logoutRequest.authToken());
        if (authData == null) {
            throw unauthorizedException;
        }
        dataAccess.deleteAuth(authData);
    }


    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
