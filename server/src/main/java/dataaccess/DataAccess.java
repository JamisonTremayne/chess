package dataaccess;

import datamodel.*;
import exception.RequestException;

import java.util.ArrayList;

public interface DataAccess {
    void clear() throws RequestException;
    void createUser(UserData user) throws RequestException;
    UserData getUser(String username) throws RequestException;
    void createGame(GameData gameData) throws RequestException;
    GameData getGame(Integer gameID) throws RequestException;
    ArrayList<GameData> listGames() throws RequestException;
    void updateGame(Integer gameID, GameData gameData) throws RequestException;
    void createAuth(AuthData authData) throws RequestException;
    AuthData getAuth(String authToken) throws RequestException;
    void deleteAuth(AuthData authData) throws RequestException;
}
