package ui;

import chess.*;
import com.google.gson.Gson;
import exception.RequestException;
import serverfacade.ServerFacade;
import serverfacade.WebsocketFacade;
import websocket.messages.ServerMessage;

import java.util.HashSet;

public class GameplayUI extends ClientUI {

    private final WebsocketFacade ws;
    private final String authToken;
    private final ChessGame.TeamColor teamColor;
    private final int gameID;
    private ChessGame currentGame = null;

    public GameplayUI(ServerFacade serverFacade, String authToken, int gameID, ChessGame.TeamColor teamColor) throws RequestException {
        super(serverFacade, "IN-GAME");
        this.authToken = authToken;
        this.gameID = gameID;
        this.teamColor = teamColor;

        ws = new WebsocketFacade(serverFacade.getServerUrl(), this);
        ws.connectToGame(authToken, gameID, teamColor);
    }

    @Override
    public String parseCommand(String command) throws Exception {
        String[] commandWords = command.split(" ");
        if (commandWords.length == 0) {
            throw new Exception();
        }
        String commandHead = commandWords[0].toLowerCase(); //NOT CASE SENSITIVE
        switch (commandHead) {
            case "help", "?" -> {
                return help();
            } case "redraw", "draw", "redraw_board", "redraw_chess_board", "redraw_chessboard", "draw_board" -> {
                return redrawBoard();
            } case "leave" -> {
                return leave();
            } case "move", "make_move" -> {
                return makeMove(commandWords);
            } case "resign" -> {
                return "";
            } case "highlight", "highlight_moves", "select", "select_piece", "show_moves", "show" -> {
                return "";
            } default -> {
                return invalidCommand(commandHead);
            }
        }
    }

    public void handleMessage(ServerMessage serverMessage) {
        if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
            String string = EscapeSequences.SET_TEXT_COLOR_GREEN + serverMessage.getMessage();
            System.out.println(string + EscapeSequences.RESET_TEXT_COLOR);
        } else if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            System.out.println(formatError(serverMessage.getMessage()));
        } else if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            ChessGame game = new Gson().fromJson(serverMessage.getMessage(), ChessGame.class);
            displayBoard(game);
        }
    }

    @Override
    public String help() {
        String helpString = "";
        helpString += formatHelp("help", "List available commands.");
        helpString += formatHelp("draw", "Redraw the chess board.");
        helpString += formatHelp("leave", "Leave your current game.");
        helpString += formatHelp("move <POSITION> to <POSITION>",
                "Move a piece at one position to another position. Use LetterNumber (A2, E3, etc) for positions.");
        helpString += formatHelp("resign",
                "Resign from the game, letting your opponent win (why would you do that?).");
        helpString += formatHelp("select <POSITION>",
                "Select a piece to view its available moves. Use LetterNumber (A2, E3, etc) for positions.");
        return helpString;
    }

    private String redrawBoard() {
        displayBoard(currentGame);
        return "";
    }

    private String leave() throws RequestException {
        changeUITo(new PostloginUI(serverFacade, authToken));
        ws.leaveGame(authToken, gameID, teamColor);
        return EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully left the game.";
    }

    private String makeMove(String[] args) throws RequestException {
        if (args.length < 3) {
            return formatError("""
                    You did not give enough arguments.
                    To move a piece, please give the <POSITION> of the piece you want to move, then the <POSITION> where to move to.
                    Use LetterNumber format (A2, E3, etc) for positions.
                    To check a piece's available moves, use the select command.
                    """);
        } else if (args.length > 4) {
            return formatError("""
                    You gave too many arguments.
                    To move a piece, please give the <POSITION> of the piece you want to move, then the <POSITION> where to move to.
                    Use LetterNumber format (A2, E3, etc) for positions.
                    To check a piece's available moves, use the select command.
                    """);
        }
        ChessPosition startPos = stringToPosition(args[1]);
        ChessPosition endPos;
        try {
            endPos = stringToPosition(args[3]);
        } catch (Exception ex) {
            endPos = stringToPosition(args[2]);
        }
        HashSet<ChessMove> pieceMoves = (HashSet<ChessMove>) currentGame.validMoves(startPos);
        if (!pieceMoves.contains(new ChessMove(startPos, endPos, null))) {
            return formatError("""
                    That move is not valid!
                    Try making a valid move instead.
                    To check a piece's available moves, use the select command.
                    """);
        } else {
            ws.makeMove(authToken, gameID, new ChessMove(startPos, endPos, null), teamColor);
            return "";
        }
    }

    private ChessPosition stringToPosition(String string) throws RequestException {
        String errorMessage = """
                Error: Piece position was improperly formatted.
                Please use LetterNumber format (A2, E3, etc) for positions.
                Note that letters must be from A-H, and numbers must be from 1-8.
                """;
        RequestException error = new RequestException(errorMessage, RequestException.Code.BadRequestError);
        if (string.length() != 2) {
            throw error;
        }
        string = string.toUpperCase();
        char firstChar = string.charAt(0);
        char secondChar = string.charAt(1);
        int row = -1;
        int col = -1;
        boolean firstIsLetter = true;
        switch (firstChar) {
            case 'A' -> col = 1;
            case 'B' -> col = 2;
            case 'C' -> col = 3;
            case 'D' -> col = 4;
            case 'E' -> col = 5;
            case 'F' -> col = 6;
            case 'G' -> col = 7;
            case 'H' -> col = 8;
            default -> firstIsLetter = false;
        }
        if (firstIsLetter) {
            switch (secondChar) {
                case '1' -> row = 1;
                case '2' -> row = 2;
                case '3' -> row = 3;
                case '4' -> row = 4;
                case '5' -> row = 5;
                case '6' -> row = 6;
                case '7' -> row = 7;
                case '8' -> row = 8;
                default -> throw error;
            }
        } else {
            switch (secondChar) {
                case 'A' -> col = 1;
                case 'B' -> col = 2;
                case 'C' -> col = 3;
                case 'D' -> col = 4;
                case 'E' -> col = 5;
                case 'F' -> col = 6;
                case 'G' -> col = 7;
                case 'H' -> col = 8;
                default -> throw error;
            }
            switch (firstChar) {
                case '1' -> row = 1;
                case '2' -> row = 2;
                case '3' -> row = 3;
                case '4' -> row = 4;
                case '5' -> row = 5;
                case '6' -> row = 6;
                case '7' -> row = 7;
                case '8' -> row = 8;
                default -> throw error;
            }
        }
        return new ChessPosition(row, col);
    }

    private void displayBoard(ChessGame game) {
        currentGame = game;
        System.out.println();
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
