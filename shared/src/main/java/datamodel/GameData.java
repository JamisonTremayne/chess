package datamodel;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game, GameState state) {
    public enum GameState {
        READY,
        IN_PROGRESS,
        UNFINISHED,
        COMPLETE
    }
}
