package dataaccess;

import datamodel.*;

import java.util.HashMap;
import java.util.Objects;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, GameData> games = new HashMap<>();
    private final HashMap<String, AuthData> auths = new HashMap<>();
    @Override
    public void clear() {
        users.clear();
        games.clear();
//        auths.clear();
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createAuth(AuthData authData) {
        auths.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuth(String authToken) {
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
