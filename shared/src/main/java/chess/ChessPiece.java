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

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
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

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (type) {
            case KING -> kingMoves(board, myPosition);
            case QUEEN -> queenMoves(board, myPosition);
            case BISHOP -> bishopMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case ROOK -> rookMoves(board, myPosition);
            case PAWN -> pawnMoves(board, myPosition);
        };
    }


    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();

        for (int i = 0; i < 8; i++) {
            int rowD = (i < 3 ? -1 : i < 6 ? 1 : 0);
            int colD = (i < 7 ? (i % 3) - 1 : 1);

            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + rowD, myPosition.getColumn() + colD);
            if (isMoveValid(board, newPosition, true)) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }

        return moves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();

        moves.addAll(bishopMoves(board, myPosition));
        moves.addAll(rookMoves(board, myPosition));

        return moves;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();

        boolean validMove = true;

        for (int i = 0; i < 4; i++) {
            int currentRow = myPosition.getRow();
            int currentCol = myPosition.getColumn();
            validMove = true;

            int rowD = (i < 2 ? -1 : 1);
            int colD = (i % 2 == 0 ? -1 : 1);

            while (validMove) {
                currentRow += rowD;
                currentCol += colD;
                ChessPosition bishopPosition = new ChessPosition(currentRow, currentCol);
                validMove = isMoveValid(board, bishopPosition, true);
                if (validMove) {
                    moves.add(new ChessMove(myPosition, bishopPosition, null));
                    validMove = !hasEnemy(board, bishopPosition);
                }
            }
        }

        return moves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();

        for (int i = 0; i < 8; i++) {
            int rowD = (i < 2 ? -2 : i < 4 ? -1 : i < 6 ? 1 : 2);
            int colD = (i < 2 || i >= 6 ? (i % 2 == 0 ? 1 : -1) : 2 * (i % 2 == 0 ? 1 : -1));

            var newPosition = new ChessPosition(myPosition.getRow() + rowD, myPosition.getColumn() + colD);
            if (isMoveValid(board, newPosition, true)) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }

        return moves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();

        boolean validMove = true;

        for (int i = 0; i < 4; i++) {
            int currentRow = myPosition.getRow();
            int currentCol = myPosition.getColumn();
            validMove = true;

            int rowD = (i == 0 ? -1 : i == 1 ? 1 : 0);
            int colD = (i < 2 ? 0 : i == 2 ? -1 : 1);

            while (validMove) {
                currentRow += rowD;
                currentCol += colD;
                ChessPosition rookPosition = new ChessPosition(currentRow, currentCol);
                validMove = isMoveValid(board, rookPosition, true);
                if (validMove) {
                    moves.add(new ChessMove(myPosition, rookPosition, null));
                    validMove = !hasEnemy(board, rookPosition);
                }
            }
        }

        return moves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();

        int direction = (pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1);
        boolean promotion = (direction == 1 && myPosition.getRow() == 7) ||
                (direction == -1 && myPosition.getRow() == 2);
        //Front movement
        var frontPosition = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if (isMoveValid(board, frontPosition, false)) {
            if (promotion) {
                moves.add(new ChessMove(myPosition, frontPosition, PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, frontPosition, PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, frontPosition, PieceType.KNIGHT));
                moves.add(new ChessMove(myPosition, frontPosition, PieceType.ROOK));
            } else {
                moves.add(new ChessMove(myPosition, frontPosition, null));
            }
            if ((direction == 1 && myPosition.getRow() == 2) || (direction == -1 && myPosition.getRow() == 7)) {
                var twoFrontPosition = new ChessPosition(myPosition.getRow() + (direction * 2), myPosition.getColumn());
                if (isMoveValid(board, twoFrontPosition, false)) {
                    moves.add(new ChessMove(myPosition, twoFrontPosition, null));
                }
            }
        }
        //Enemy diagonals
        var enemyPositionLeft = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() - 1);
        var enemyPositionRight = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + 1);
        if (hasEnemy(board, enemyPositionLeft)) {
            if (promotion) {
                moves.add(new ChessMove(myPosition, enemyPositionLeft, PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, enemyPositionLeft, PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, enemyPositionLeft, PieceType.KNIGHT));
                moves.add(new ChessMove(myPosition, enemyPositionLeft, PieceType.ROOK));
            } else {
                moves.add(new ChessMove(myPosition, enemyPositionLeft, null));
            }
        }
        if (hasEnemy(board, enemyPositionRight)) {
            if (promotion) {
                moves.add(new ChessMove(myPosition, enemyPositionRight, PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, enemyPositionRight, PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, enemyPositionRight, PieceType.KNIGHT));
                moves.add(new ChessMove(myPosition, enemyPositionRight, PieceType.ROOK));
            } else {
                moves.add(new ChessMove(myPosition, enemyPositionRight, null));
            }
        }

        return moves;
    }

    private boolean isMoveValid(ChessBoard board, ChessPosition position, boolean canTake) {
        if (position.getRow() < 1 || position.getRow() > 8) {
            return false;
        } else if (position.getColumn() < 1 || position.getColumn() > 8) {
            return false;
        }
        var piece = board.getPiece(position);
        if (piece != null && (piece.getTeamColor() == getTeamColor() || !canTake)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean hasEnemy(ChessBoard board, ChessPosition position) {
        if (position.getRow() < 1 || position.getRow() > 8) {
            return false;
        } else if (position.getColumn() < 1 || position.getColumn() > 8) {
            return false;
        }

        var piece = board.getPiece(position);
        return (piece != null && piece.getTeamColor() != getTeamColor());
    }
}
