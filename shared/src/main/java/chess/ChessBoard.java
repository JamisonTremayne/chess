package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board = new ChessPiece[8][8];
    public ChessBoard() {
        resetBoard();
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow()-1][position.getColumn()-1] = piece;
    }

    public void removePiece(ChessPosition position) {
        board[position.getRow()-1][position.getColumn()-1] = null;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow()-1][position.getColumn()-1];
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                removePiece(new ChessPosition(i + 1, j + 1));
            }
        }
        //Make an individual piece for each part of the board, and place them there
        for (int i = 0; i < 2; i++) {
            ChessGame.TeamColor currentTeam;
            if (i == 0) { currentTeam = ChessGame.TeamColor.WHITE; }
            else { currentTeam = ChessGame.TeamColor.BLACK; }
            //Add each individual piece
            for (int j = 0; j < 8; j++) {
                ChessPiece.PieceType currentType;
                if (j == 0 || j == 7) { currentType = ChessPiece.PieceType.ROOK; }
                else if (j == 1 || j == 6) { currentType = ChessPiece.PieceType.KNIGHT; }
                else if (j == 2 || j == 5) { currentType = ChessPiece.PieceType.BISHOP; }
                else if (j == 3) { currentType = ChessPiece.PieceType.QUEEN; }
                else { currentType = ChessPiece.PieceType.KING; }

                var newPiece = new ChessPiece(currentTeam, currentType);
                int row = 1;
                if (currentTeam == ChessGame.TeamColor.BLACK) { row = 8; }
                addPiece(new ChessPosition(row, j + 1), newPiece);
            }
            //Add all the pawns
            for (int j = 0; j < 8; j++) {
                int row = 2;
                if (currentTeam == ChessGame.TeamColor.BLACK) { row = 7; }
                addPiece(new ChessPosition(row, j + 1), new ChessPiece(currentTeam, ChessPiece.PieceType.PAWN));
            }
        }


    }
}
