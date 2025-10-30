package dataaccess;

import datamodel.*;
import exception.RequestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private final HashMap<String, AuthData> auths = new HashMap<>();
    @Override
    public void clear() {
        users.clear();
        games.clear();
        auths.clear();
    }

    @Override
    public void createUser(UserData user) throws RequestException {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new RequestException("data access error", RequestException.Code.DataAccessError);
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createGame(GameData gameData) throws RequestException {
        if (games.containsKey(gameData.gameID()) || gameData.gameName() == null || gameData.game() == null) {
            throw new RequestException("data access error", RequestException.Code.DataAccessError);
        }
        games.put(gameData.gameID(), gameData);
    }

    @Override
    public GameData getGame(Integer gameID) throws RequestException {
        return games.get(gameID);
    }

    @Override
    public ArrayList<GameData> listGames() {
        ArrayList<GameData> allGames = new ArrayList<>();
        for (int id : games.keySet()) {
            allGames.add(games.get(id));
        }
        return allGames;
    }

    @Override
    public void updateGame(Integer gameID, GameData gameData) throws RequestException {
        if (gameData.gameName() == null || gameData.game() == null) {
            throw new RequestException("data access error", RequestException.Code.DataAccessError);
        }
        games.remove(gameID);
        games.put(gameData.gameID(), gameData);
    }

    @Override
    public void createAuth(AuthData authData) throws RequestException {
        if (authData.authToken() == null || authData.username() == null) {
            throw new RequestException("data access error", RequestException.Code.DataAccessError);
        }
        auths.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuth(String authToken) throws RequestException {
        if (authToken == null) {
            throw new RequestException("data access error", RequestException.Code.DataAccessError);
        }
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(AuthData authData) {
        auths.remove(authData.authToken());
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemoryDataAccess that = (MemoryDataAccess) o;
        return Objects.equals(users, that.users) && Objects.equals(games, that.games) && Objects.equals(auths, that.auths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users, games, auths);
    }
}
