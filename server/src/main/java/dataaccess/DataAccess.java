package dataaccess;

import datamodel.*;

import java.util.ArrayList;

public interface DataAccess {
    void clear();
    void createUser(UserData user);
    UserData getUser(String username);
    void createGame(GameData gameData);
    GameData getGame(Integer gameID);
    ArrayList<GameData> listGames();
    void updateGame(Integer gameID, GameData gameData);
    void createAuth(AuthData authData);
    AuthData getAuth(String authToken);
    void deleteAuth(AuthData authData);
}
