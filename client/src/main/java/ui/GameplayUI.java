package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import serverfacade.ServerFacade;
import serverfacade.WebsocketFacade;
import websocket.messages.ServerMessage;

public class GameplayUI extends ClientUI {

    private final WebsocketFacade ws;
    private final String authToken;
    private final ChessGame.TeamColor teamColor;
    private final int gameID;

    public GameplayUI(ServerFacade serverFacade, String authToken, int gameID, ChessGame.TeamColor teamColor) {
        super(serverFacade, "IN-GAME");
        this.authToken = authToken;
        this.gameID = gameID;
        this.teamColor = teamColor;

        ws = new WebsocketFacade(serverFacade.getServerUrl(), this);

        displayBoard();
    }

    @Override
    public String parseCommand(String command) throws Exception {
        String[] commandWords = command.split(" ");
        if (commandWords.length == 0) {
            throw new Exception();
        }
        String commandHead = commandWords[0].toLowerCase(); //NOT CASE SENSITIVE
        switch (commandHead) {
            case "help" -> {
                return help();
            } case "leave" -> {
                return leave();
            } default -> {
                return invalidCommand(commandHead);
            }
        }
    }

    public void printMessage(ServerMessage serverMessage) {

    }

    @Override
    public String help() {
        String helpString = "";
        helpString += formatHelp("help", "List available commands.");
        helpString += formatHelp("leave", "Leave your current game.");
        return helpString;
    }

    private String leave() {
        changeUITo(new PostloginUI(serverFacade, authToken));
        return EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully left the game.";
    }

    private void displayBoard() {
        // HARD-CODE DISPLAY GAME
        ChessGame game = new ChessGame();
        ChessBoard board = game.getBoard();
        int startI = teamColor == ChessGame.TeamColor.BLACK? 0: 9;
        int startJ = teamColor == ChessGame.TeamColor.BLACK? 9: 0;
        int incrementI = teamColor == ChessGame.TeamColor.BLACK? 1: -1;
        int incrementJ = teamColor == ChessGame.TeamColor.BLACK? -1: 1;
        for (int i = startI; i < 10 && i >= 0; i += incrementI) {
            for (int j = startJ; j < 10 && j >= 0; j += incrementJ) {
                String ln;
                if (i == 0 || i == 9) {
                    switch (j) {
                        case 1 -> ln = formatBorder() + EscapeSequences.EMPTY + "A ";
                        case 2 -> ln = formatBorder() + EscapeSequences.EMPTY + "B ";
                        case 3 -> ln = formatBorder() + EscapeSequences.EMPTY + "C ";
                        case 4 -> ln = formatBorder() + EscapeSequences.EMPTY + "D ";
                        case 5 -> ln = formatBorder() + EscapeSequences.EMPTY + "E ";
                        case 6 -> ln = formatBorder() + EscapeSequences.EMPTY + "F ";
                        case 7 -> ln = formatBorder() + EscapeSequences.EMPTY + "G ";
                        case 8 -> ln = formatBorder() + EscapeSequences.EMPTY + "H ";
                        default -> ln = formatBorder() + EscapeSequences.EMPTY + "  ";
                    }
                } else if (j == 0 || j == 9) {
                    ln = formatBorder() + EscapeSequences.EMPTY + i + " ";
                } else {
                    int square = (i + j) % 2;
                    if (square == 0) {
                        ln = formatDarkSquare();
                    } else {
                        ln = formatLightSquare();
                    }
                    ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                    if (piece == null) {
                        ln += EscapeSequences.EMPTY + "  ";
                        System.out.print(ln);
                        continue;
                    }
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
                        default -> ln += EscapeSequences.EMPTY + "  ";
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
