package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTurn;
    private ChessBoard board;

    public ChessGame() {
        currentTurn = TeamColor.WHITE;
        board = new ChessBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        } else {
            HashSet<ChessMove> moves = (HashSet<ChessMove>) piece.pieceMoves(getBoard(), startPosition);
            HashSet<ChessMove> validMoves = new HashSet<>();
            for (ChessMove move : moves) {
                if (!makeGhostMove(move)) {
                    validMoves.add(move);
                }
            }
            return validMoves;
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        

        board.addPiece(move.getEndPosition(), piece);
        board.removePiece(move.getStartPosition());
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        HashSet<ChessMove> enemyMoves = getEnemyMoves(teamColor);
        ChessPosition kingPosition = getKingPosition(teamColor);

        for (ChessMove move : enemyMoves) {
            if (move.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    //Returns true if a move WOULD make the player in check, and returns false otherwise.
    private boolean makeGhostMove(ChessMove move) {
        //Make the move
        ChessPiece piece = board.getPiece(move.getStartPosition());
        ChessPiece enemyPiece = board.getPiece(move.getEndPosition());
        board.addPiece(move.getEndPosition(), piece);
        board.removePiece(move.getStartPosition());

        //Check if the player is in check
        boolean inCheck = isInCheck(currentTurn);

        //Reverse the move
        board.addPiece(move.getStartPosition(), piece);
        board.addPiece(move.getEndPosition(), enemyPiece);

        return inCheck;
    }

    private HashSet<ChessMove> getEnemyMoves(TeamColor team) {
        HashSet<ChessMove> enemyMoves = new HashSet<>();
        TeamColor enemyTeam = (team == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPosition position = new ChessPosition(i+1, j+1);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == enemyTeam) {
                    enemyMoves.addAll(piece.pieceMoves(board, position));
                }
            }
        }

        return enemyMoves;
    }

    private ChessPosition getKingPosition(TeamColor team) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPosition position = new ChessPosition(i+1, j+1);
                ChessPiece piece = board.getPiece(position);
                if (piece.getPieceType() == ChessPiece.PieceType.KING &&
                piece.getTeamColor() == team) {
                    return position;
                }
            }
        }

        return null;
    }
}
