package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import serverfacade.ServerFacade;

public class GameplayUI extends ClientUI {

    private final ChessGame.TeamColor teamColor;
    private final int gameID;

    public GameplayUI(ServerFacade serverFacade, int gameID, ChessGame.TeamColor teamColor) {
        super(serverFacade, "IN-GAME");
        this.gameID = gameID;
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
        // HARD-CODE DISPLAY GAME
        ChessGame game = new ChessGame();
        ChessBoard board = game.getBoard();
        int start = teamColor == ChessGame.TeamColor.BLACK? 0: 9;
        int increment = teamColor == ChessGame.TeamColor.BLACK? 1: -1;
        for (int i = start; i < 10 && i >= 0; i += increment) {
            for (int j = start; j < 10 && j >= 0; j += increment) {
                String ln;
                if (i == 0 || i == 9) {
                    switch (j) {
                        case 8 -> ln = formatBorder() + " A ";
                        case 7 -> ln = formatBorder() + "  B ";
                        case 6 -> ln = formatBorder() + " C ";
                        case 5 -> ln = formatBorder() + "  D ";
                        case 4 -> ln = formatBorder() + "  E ";
                        case 3 -> ln = formatBorder() + " F ";
                        case 2 -> ln = formatBorder() + "  G ";
                        case 1 -> ln = formatBorder() + " H ";
                        default -> ln = formatBorder() + "   ";
                    }
                } else if (j == 0 || j == 9) {
                    ln = formatBorder() + " " + i + " ";
                } else {
                    int square = (i + j) % 2;
                    if (square == 1) {
                        ln = formatDarkSquare();
                    } else {
                        ln = formatLightSquare();
                    }
                    ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                    if (piece != null) {
                        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            ln += formatWhite();
                        } else {
                            ln += formatBlack();
                        }
                        switch (piece.getPieceType()) {
                            case ChessPiece.PieceType.PAWN -> ln += EscapeSequences.BLACK_PAWN;
                            case ChessPiece.PieceType.KING -> ln += EscapeSequences.BLACK_KING;
                            case ChessPiece.PieceType.QUEEN -> ln += EscapeSequences.BLACK_QUEEN;
                            case ChessPiece.PieceType.BISHOP -> ln += EscapeSequences.BLACK_BISHOP;
                            case ChessPiece.PieceType.KNIGHT -> ln += EscapeSequences.BLACK_KNIGHT;
                            case ChessPiece.PieceType.ROOK -> ln += EscapeSequences.BLACK_ROOK;
                            default -> ln += EscapeSequences.EMPTY;
                        }
                    } else {
                        ln += EscapeSequences.EMPTY;
                    }
                }
                System.out.print(ln);
            }
            System.out.println(EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR);
        }
        System.out.println(EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR);
    }

    private String formatBorder() {
        return EscapeSequences.SET_BG_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
    }

    private String formatLightSquare() {
        return EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
    }

    private String formatDarkSquare() {
        return EscapeSequences.SET_BG_COLOR_DARK_GREEN;
    }

    private String formatWhite() {
        return EscapeSequences.SET_TEXT_COLOR_WHITE;
    }

    private String formatBlack() {
        return EscapeSequences.SET_TEXT_COLOR_BLACK;
    }
}
