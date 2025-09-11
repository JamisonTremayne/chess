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
    private boolean enpassant = false;

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
        if (type == PieceType.KING) {
            return kingMoves(board, myPosition);
        } else if (type == PieceType.QUEEN) {
            return queenMoves(board, myPosition);
        } else if (type == PieceType.BISHOP) {
            return bishopMoves(board, myPosition);
        } else if (type == PieceType.KNIGHT) {
            return knightMoves(board, myPosition);
        } else if (type == PieceType.ROOK) {
            return rookMoves(board, myPosition);
        } else if (type == PieceType.PAWN) {
            return pawnMoves(board, myPosition);
        } else {
            return new HashSet<>();
        }
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new HashSet<ChessMove>();
        //Simply check each square directly around the king
        for (int i = 0; i < 8; i++) {
            int dRow = 0;
            int dCol = 0;
            if (i == 0) {dRow = 1;} //Up
            else if (i == 1) {dRow = 1; dCol = 1;} //Up-right
            else if (i == 2) {dCol = 1;} //Right
            else if (i == 3) {dRow = -1; dCol = 1;} //Down-right
            else if (i == 4) {dRow = -1;} //Down
            else if (i == 5) {dRow = -1; dCol = -1;} //Down-left
            else if (i == 6) {dCol = -1;} //Left
            else {dRow = 1; dCol = -1;} //Up-left

            var newPosition = new ChessPosition(myPosition.getRow() + dRow, myPosition.getColumn() + dCol);
            if (getPositionValid(board, newPosition)) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new HashSet<ChessMove>();

        var bishopMoveSet = bishopMoves(board, myPosition);
        var rookMoveSet = rookMoves(board, myPosition);
        moves.addAll(bishopMoveSet);
        moves.addAll(rookMoveSet);

        return moves;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new HashSet<ChessMove>();

        for (int i = 0; i < 4; i++) {
            boolean validMove = true;
            int currRow = myPosition.getRow();
            int currCol = myPosition.getColumn();

            var dRow = 0;
            var dCol = 0;
            if (i == 0) {dRow = 1; dCol = 1;} //Up-right
            else if (i == 1) {dRow = 1; dCol = -1;} //Up-left
            else if (i == 2) {dRow = -1; dCol = -1;} //Down-left
            else {dRow = -1; dCol = 1;} //Down-right

            while (validMove) {
                currRow += dRow;
                currCol += dCol;
                var newPosition = new ChessPosition(currRow, currCol);
                validMove = getPositionValid(board, newPosition);
                if (validMove) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    if (getPositionEnemy(board, newPosition)) {
                        validMove = false;
                    }
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new HashSet<ChessMove>();

        for (int i = 0; i < 8; i++) {
            int dRow = 0;
            int dCol = 0;
            if (i == 0) {dRow = 2; dCol = 1;} //^^>
            else if (i == 1) {dRow = 1; dCol = 2;} //>>^
            else if (i == 2) {dRow = -1; dCol = 2;} //>>v
            else if (i == 3) {dRow = -2; dCol = 1;} //vv>
            else if (i == 4) {dRow = -2; dCol = -1;} //vv<
            else if (i == 5) {dRow = -1; dCol = -2;} //<<v
            else if (i == 6) {dRow = 1; dCol = -2;} //<<^
            else {dRow = 2; dCol = -1;} //^^<

            var newPosition = new ChessPosition(myPosition.getRow() + dRow, myPosition.getColumn() + dCol);
            if (getPositionValid(board, newPosition)) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new HashSet<ChessMove>();

        for (int i = 0; i < 4; i++) {
            boolean validMove = true;
            int currRow = myPosition.getRow();
            int currCol = myPosition.getColumn();

            var dRow = 0;
            var dCol = 0;
            if (i == 0) {dRow = 1;} //Up
            else if (i == 1) {dCol = 1;} //Right
            else if (i == 2) {dRow = -1;} //Down
            else {dCol = -1;} //Left

            while (validMove) {
                currRow += dRow;
                currCol += dCol;
                var newPosition = new ChessPosition(currRow, currCol);
                validMove = getPositionValid(board, newPosition);
                if (validMove) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    if (getPositionEnemy(board, newPosition)) {
                        validMove = false;
                    }
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new HashSet<ChessMove>();

        int direction = 1;
        if (pieceColor == ChessGame.TeamColor.BLACK) direction = -1;

        boolean promotion = (direction == 1 && myPosition.getRow() == 7) || (direction == -1 && myPosition.getRow() == 2);

        //Check the front position
        var forwardPosition = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if (getPositionValid(board, forwardPosition) && board.getPiece(forwardPosition) == null) {
            if (promotion) {
                moves.add(new ChessMove(myPosition, forwardPosition, PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, forwardPosition, PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, forwardPosition, PieceType.KNIGHT));
                moves.add(new ChessMove(myPosition, forwardPosition, PieceType.ROOK));
            } else {
                moves.add(new ChessMove(myPosition, forwardPosition, null));
            }
        }
        //If the pawn is at the beginning position, check the position two in front
        if ((direction == 1 && myPosition.getRow() == 2) || (direction == -1 && myPosition.getRow() == 7)) {
            var twoForwardPosition = new ChessPosition(myPosition.getRow() + (direction * 2), myPosition.getColumn());
            if (getPositionValid(board, twoForwardPosition) && board.getPiece(forwardPosition) == null && board.getPiece(twoForwardPosition) == null) {
                moves.add(new ChessMove(myPosition, twoForwardPosition, null));
            }
        }
        //Check for enemies in the diagonals of the pawn
        var leftEnemyPosition = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() - 1);
        var rightEnemyPosition = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + 1);
        if (getPositionEnemy(board, leftEnemyPosition)) {
            if (promotion) {
                moves.add(new ChessMove(myPosition, leftEnemyPosition, PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, leftEnemyPosition, PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, leftEnemyPosition, PieceType.KNIGHT));
                moves.add(new ChessMove(myPosition, leftEnemyPosition, PieceType.ROOK));
            } else {
                moves.add(new ChessMove(myPosition, leftEnemyPosition, null));
            }
        }
        if (getPositionEnemy(board, rightEnemyPosition)) {
            if (promotion) {
                moves.add(new ChessMove(myPosition, rightEnemyPosition, PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, rightEnemyPosition, PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, rightEnemyPosition, PieceType.KNIGHT));
                moves.add(new ChessMove(myPosition, rightEnemyPosition, PieceType.ROOK));
            } else {
                moves.add(new ChessMove(myPosition, rightEnemyPosition, null));
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
        if (position.getRow() < 1 || position.getColumn() < 1) return false;
        else if (position.getRow() > 8 || position.getColumn() > 8) return false;

        var piece = board.getPiece(position);
        return piece != null && piece.getTeamColor() != pieceColor;
    }
}
