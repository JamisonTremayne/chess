package service;

import dataaccess.DataAccess;
import datamodel.*;
import exception.RequestException;

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
        return login(user);
    }

    public AuthData login(UserData userInfo) throws RequestException {
        UserData user = dataAccess.getUser(userInfo.username());
        if (userInfo.username() == null || userInfo.password() == null) {
            throw new RequestException("Error: bad request", RequestException.Code.BadRequestError);
        }
        if (user == null || !userInfo.password().equals(user.password())) {
            throw new RequestException("Error: unauthorized", RequestException.Code.UnauthorizedError);
        }
        String authToken = generateToken();
        AuthData authData = new AuthData(user.username(), authToken);
        dataAccess.createAuth(authData);
        return authData;
    }

    public void logout(String authToken) throws RequestException {
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new RequestException("Error: unauthorized", RequestException.Code.UnauthorizedError);
        }
        dataAccess.deleteAuth(authData);
    }


    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
