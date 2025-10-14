package service;

import datamodel.*;

import java.util.UUID;

public class UserService {

    public AuthData register(UserData user) {
        return new AuthData(user.username(), generateToken());
    }


    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
