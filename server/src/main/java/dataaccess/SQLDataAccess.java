package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import datamodel.*;
import exception.RequestException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SQLDataAccess implements DataAccess {

    private final RequestException dataAccessException = new RequestException("Error: data access error", RequestException.Code.DataAccessError);
    
    public SQLDataAccess() throws RequestException {
        configureDatabase();
    }

    @Override
    public void clear() throws RequestException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement1 = "DELETE FROM auth";
            String statement2 = "DELETE FROM user";
            String statement3 = "DELETE FROM game";
            conn.prepareStatement(statement1).executeUpdate();
            conn.prepareStatement(statement2).executeUpdate();
            conn.prepareStatement(statement3).executeUpdate();
        } catch (DataAccessException | SQLException ex) {
            throw dataAccessException;
        }
    }

    @Override
    public void createUser(UserData user) throws RequestException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "INSERT INTO `user`(username, password, email) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, user.username());
                ps.setString(2, user.password());
                ps.setString(3, user.email());
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException ex) {
            throw dataAccessException;
        }
    }

    @Override
    public UserData getUser(String username) throws RequestException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT username, password, email FROM `user`WHERE username=?";
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
            throw dataAccessException;
        }
        return null;
    }

    @Override
    public void createGame(GameData gameData) throws RequestException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "INSERT INTO `game`(gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
            String json = gameData.game().toString();
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameData.gameID());
                ps.setString(2, gameData.whiteUsername());
                ps.setString(3, gameData.blackUsername());
                ps.setString(4, gameData.gameName());
                ps.setString(5, json);
                ps.executeUpdate();
            }
        } catch (Exception ex) {
            throw dataAccessException;
        }
    }

    @Override
    public GameData getGame(Integer gameID) throws RequestException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT * FROM `game`WHERE gameID=?";
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
            throw dataAccessException;
        }
        return null;
    }

    @Override
    public ArrayList<GameData> listGames() throws RequestException {
       ArrayList<GameData> gameList = new ArrayList<>();
       try (Connection conn = DatabaseManager.getConnection()) {
           String statement = "SELECT gameID FROM game";
           try (PreparedStatement ps = conn.prepareStatement(statement)) {
               ResultSet rs = ps.executeQuery();
               while (rs.next()) {
                   int gameID = rs.getInt("gameID");
                   gameList.add(getGame(gameID));
               }
           }
       } catch (DataAccessException | SQLException ex) {
           throw dataAccessException;
       }
        return gameList;
    }

    @Override
    public void updateGame(Integer gameID, GameData gameData) throws RequestException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String deleteStatement = "UPDATE `game`" +
                    "SET gameID=?, whiteUsername=?, blackUsername=?, gameName=?, game=? " +
                    "WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(deleteStatement)) {
                ps.setInt(1, gameData.gameID());
                ps.setString(2, gameData.whiteUsername());
                ps.setString(3, gameData.blackUsername());
                ps.setString(4, gameData.gameName());
                String jsonGame = gameData.game().toString();
                ps.setString(5, jsonGame);
                ps.setInt(6, gameID);
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException ex) {
            throw dataAccessException;
        }
    }

    @Override
    public void createAuth(AuthData authData) throws RequestException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "INSERT INTO `auth`(username, authToken) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authData.username());
                ps.setString(2, authData.authToken());
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException ex) {
            throw dataAccessException;
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws RequestException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT * FROM `auth`WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new AuthData(rs.getString("username"), authToken);
                }
            }
        } catch (DataAccessException | SQLException ex) {
            throw dataAccessException;
        }
        return null;
    }

    @Override
    public void deleteAuth(AuthData authData) throws RequestException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "DELETE FROM `auth`WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authData.authToken());
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException ex) {
            throw dataAccessException;
        }
    }


    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  `user` (
                `username` VARCHAR(255) NOT NULL,
                `password` VARCHAR(255) NOT NULL,
                `email` VARCHAR(255) DEFAULT NULL,
                PRIMARY KEY (`username`),
                INDEX(`email`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """,
            """
            CREATE TABLE IF NOT EXISTS `auth` (
                `username` VARCHAR(255) NOT NULL,
                `authToken` VARCHAR(512) NOT NULL,
                PRIMARY KEY (`authToken`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """,
            """
            CREATE TABLE IF NOT EXISTS `game` (
                `gameID` INT NOT NULL,
                `whiteUsername` VARCHAR(255),
                `blackUsername` VARCHAR(255),
                `gameName` VARCHAR(255) NOT NULL,
                `game` LONGTEXT NOT NULL,
                PRIMARY KEY (`gameID`),
                INDEX(`gameName`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """
    };


    private void configureDatabase() throws RequestException {
        try {
            DatabaseManager.createDatabase();
            try (Connection conn = DatabaseManager.getConnection()) {
                for (String statement : createStatements) {
                    try (var preparedStatement = conn.prepareStatement(statement)) {
                        preparedStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException | DataAccessException ex) {
            throw new RequestException(String.format("Error: Unable to configure database: %s", ex.getMessage()), RequestException.Code.DataAccessError);
        }
    }
}
