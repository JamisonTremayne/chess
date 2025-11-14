package ui;

import chess.ChessGame;
import serverfacade.ServerFacade;

public class GameplayUI extends ClientUI {

    private final ChessGame.TeamColor teamColor;

    public GameplayUI(ServerFacade serverFacade, ChessGame.TeamColor teamColor) {
        super(serverFacade, "IN-GAME");
        this.teamColor = teamColor;

        displayBoard();
    }

    @Override
    public String parseCommand(String command) throws Exception {
        return "";
    }

    @Override
    public String help() {
        return "";
    }

    private void displayBoard() {

    }
}
