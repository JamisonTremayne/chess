package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import datamodel.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SQLDataAccess implements DataAccess {

    public SQLDataAccess() {
        try {
            configureDatabase();
        } catch (DataAccessException ex) {
            System.out.println("Lol doesn't work my brotha");
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "DROP DATABASE chess";
            conn.prepareStatement(statement).executeUpdate();
            configureDatabase();
        } catch (DataAccessException | SQLException ex) {
            //Do something
        }
    }

    @Override
    public void createUser(UserData user) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String username = user.username();
            String password = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            String email = user.email();
            String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, email);
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException ex) {
            //Do something
        }
    }

    @Override
    public UserData getUser(String username) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT username, password, email FROM user WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String password = rs.getString("password");
                    String email = rs.getString("email");
                    return new UserData(username, password, email);
                }
            }
        } catch (DataAccessException | SQLException ex) {
            //Do something
        }
        return null;
    }

    @Override
    public void createGame(GameData gameData) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
            String json = gameData.game().toString();
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameData.gameID());
                ps.setString(2, gameData.whiteUsername());
                ps.setString(3, gameData.blackUsername());
                ps.setString(4, gameData.gameName());
                ps.setString(5, json);
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException ex) {
            //Do something
        }
    }

    @Override
    public GameData getGame(Integer gameID) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    String jsonGame = rs.getString("game");
                    ChessGame game = new Gson().fromJson(jsonGame, ChessGame.class);
                    return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
                }
            }
        } catch (DataAccessException | SQLException ex) {
            //Do something
        }
        return null;
    }

    @Override
    public ArrayList<GameData> listGames() {
        return null;
    }

    @Override
    public void updateGame(Integer gameID, GameData gameData) {

    }

    @Override
    public void createAuth(AuthData authData) {

    }

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void deleteAuth(AuthData authData) {

    }


    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  `user` (
                `username` VARCHAR(256) NOT NULL,
                `password` VARCHAR(256) NOT NULL,
                `email` VARCHAR(256) DEFAULT NULL,
                PRIMARY KEY (`username`),
                INDEX(`email`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """,
            """
            CREATE TABLE IF NOT EXISTS `auth` (
                `username` VARCHAR(256) NOT NULL,
                `authToken` VARCHAR(256) NOT NULL,
                PRIMARY KEY (`username`),
                INDEX(`authToken`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """,
            """
            CREATE TABLE IF NOT EXISTS `game` (
                `gameID` INT NOT NULL,
                `whiteUsername` VARCHAR(256),
                `blackUsername` VARCHAR(256),
                `gameName` VARCHAR(256) NOT NULL,
                `game` TEXT NOT NULL,
                PRIMARY KEY (`gameID`),
                INDEX(`gameName`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """
    };


    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
