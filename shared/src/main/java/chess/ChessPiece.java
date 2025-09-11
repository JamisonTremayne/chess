package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return moveCalculator(board, myPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }


    private Collection<ChessMove> moveCalculator(ChessBoard board, ChessPosition myPosition) {
        if (type == PieceType.BISHOP) {
            return bishopMoves(board, myPosition);
        } else {
            return new HashSet<ChessMove>();
        }
    }


    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new HashSet<ChessMove>();
        boolean validMove = true;

        //Up Right
        int currRow = myPosition.getRow();
        int currCol = myPosition.getColumn();
        while (validMove) {
            currRow++; currCol++;
            var newPosition = new ChessPosition(currRow, currCol);
            validMove = getPositionValid(board, newPosition);
            if (validMove) {
                moves.add(new ChessMove(myPosition, newPosition, type));
                if (getPositionEnemy(board, newPosition)) {
                    validMove = false;
                }
            }
        }
        //Up Left
        validMove = true; currRow = myPosition.getRow(); currCol = myPosition.getColumn();
        while(validMove) {
            currRow++; currCol--;
            var newPosition = new ChessPosition(currRow, currCol);
            validMove = getPositionValid(board, newPosition);
            if (validMove) {
                moves.add(new ChessMove(myPosition, newPosition, type));
                if (getPositionEnemy(board, newPosition)) {
                    validMove = false;
                }
            }
        }
        //Down Right
        validMove = true; currRow = myPosition.getRow(); currCol = myPosition.getColumn();
        while(validMove) {
            currRow--; currCol++;
            var newPosition = new ChessPosition(currRow, currCol);
            validMove = getPositionValid(board, newPosition);
            if (validMove) {
                moves.add(new ChessMove(myPosition, newPosition, type));
                if (getPositionEnemy(board, newPosition)) {
                    validMove = false;
                }
            }
        }
        //Down Left
        validMove = true; currRow = myPosition.getRow(); currCol = myPosition.getColumn();
        while(validMove) {
            currRow--; currCol--;
            var newPosition = new ChessPosition(currRow, currCol);
            validMove = getPositionValid(board, newPosition);
            if (validMove) {
                moves.add(new ChessMove(myPosition, newPosition, type));
                if (getPositionEnemy(board, newPosition)) {
                    validMove = false;
                }
            }
        }
        return moves;
    }

    //Returns true if the position is a valid spot, or false otherwise
    private boolean getPositionValid(ChessBoard board, ChessPosition position) {
        if (position.getRow() < 1 || position.getColumn() < 1) {
            return false;
        } else if (position.getRow() > 8 || position.getColumn() > 8) {
            return false;
        } else if (board.getPiece(position) != null && board.getPiece(position).getTeamColor() == pieceColor) {
            return false;
        }
        return true;
    }

    //Returns true if the position has an enemy piece, or false otherwise
    private boolean getPositionEnemy(ChessBoard board, ChessPosition position) {
        var piece = board.getPiece(position);
        return piece != null && piece.getTeamColor() != pieceColor;
    }
}
