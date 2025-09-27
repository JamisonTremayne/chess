package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTurn;
    private ChessBoard board;

    private boolean whiteQueensideCastle;
    private boolean whiteKingsideCastle;
    private boolean blackQueensideCastle;
    private boolean blackKingsideCastle;

    public ChessGame() {
        currentTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();

        whiteQueensideCastle = true;
        whiteKingsideCastle = true;
        blackQueensideCastle = true;
        blackKingsideCastle = true;
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

    public enum CastleSide {
        QUEENSIDE,
        KINGSIDE
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
            return new HashSet<ChessMove>();
        } else {
            HashSet<ChessMove> moves = (HashSet<ChessMove>) piece.pieceMoves(getBoard(), startPosition);
            HashSet<ChessMove> validMoves = new HashSet<>();
            for (ChessMove move : moves) {
                if (!makeGhostMove(move, piece.getTeamColor())) {
                    validMoves.add(move);
                }
            }

            //Check for Castling moves
            if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                int row = (piece.getTeamColor() == TeamColor.WHITE ? 1 : 8);
                if (canCastle(piece.getTeamColor(), CastleSide.KINGSIDE)) {
                    validMoves.add(new ChessMove(startPosition,
                            new ChessPosition(row, 7), null));
                }
                if (canCastle(piece.getTeamColor(), CastleSide.QUEENSIDE)) {
                    validMoves.add(new ChessMove(startPosition,
                            new ChessPosition(row, 3), null));
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

        HashSet<ChessMove> validMoves = (HashSet<ChessMove>) validMoves(move.getStartPosition());

        //Check for Castling Moves
        boolean castling = false;
        if (piece.getPieceType() == ChessPiece.PieceType.KING &&
                (move.getEndPosition().getColumn() == 3 || move.getEndPosition().getColumn() == 7)) {
            CastleSide side = (move.getEndPosition().getColumn() == 3 ? CastleSide.QUEENSIDE : CastleSide.KINGSIDE);
            if (canCastle(piece.getTeamColor(), side)) {
                castling = true;
            }
        }

        if (!validMoves.contains(move) || piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException();
        }

        //Handle castling changes
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                whiteKingsideCastle = false;
                whiteQueensideCastle = false;
            } else {
                blackKingsideCastle = false;
                blackQueensideCastle = false;
            }
        } else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            int row = move.getStartPosition().getRow();
            int col = move.getStartPosition().getColumn();
            if (row == 1 && col == 1) {
                whiteQueensideCastle = false;
            } else if (row == 1 && col == 8) {
                whiteKingsideCastle = false;
            } else if (row == 8 && col == 1) {
                blackQueensideCastle = false;
            } else if (row == 8 && col == 8) {
                blackKingsideCastle = false;
            }
        }

        if (!castling) {
            if (move.getPromotionPiece() != null) {
                piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            }
            board.addPiece(move.getEndPosition(), piece);
            board.removePiece(move.getStartPosition());
        } else {
            int newCol = move.getEndPosition().getColumn();
            int rookCol = (newCol == 3 ? 4 : 6);
            ChessPosition rookPosition = (new ChessPosition(
                    (piece.getTeamColor() == TeamColor.WHITE ? 1 : 8),
                    (newCol == 3 ? 1 : 8)));
            ChessPiece rook = board.getPiece(rookPosition);

            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(new ChessPosition(
                    (piece.getTeamColor() == TeamColor.WHITE ? 1 : 8), rookCol), rook);
            board.removePiece(move.getStartPosition());
            board.removePiece(rookPosition);

        }

        currentTurn = (currentTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        TeamColor enemyTeam = (teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        HashSet<ChessMove> enemyMoves = getTeamMoves(enemyTeam);
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
        return (isInCheck(teamColor) && !hasValidMoves(teamColor));
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return (!isInCheck(teamColor) && !hasValidMoves(teamColor));
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
    private boolean makeGhostMove(ChessMove move, TeamColor team) {
        //Make the move
        ChessPiece piece = board.getPiece(move.getStartPosition());
        ChessPiece enemyPiece = board.getPiece(move.getEndPosition());
        board.addPiece(move.getEndPosition(), piece);
        board.removePiece(move.getStartPosition());

        //Check if the player is in check
        boolean inCheck = isInCheck(team);

        //Reverse the move
        board.addPiece(move.getStartPosition(), piece);
        board.addPiece(move.getEndPosition(), enemyPiece);

        return inCheck;
    }

    private HashSet<ChessMove> getTeamMoves(TeamColor team) {
        HashSet<ChessMove> enemyMoves = new HashSet<>();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPosition position = new ChessPosition(i+1, j+1);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == team) {
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
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING &&
                piece.getTeamColor() == team) {
                    return position;
                }
            }
        }

        return null;
    }

    private boolean hasValidMoves(TeamColor team) {
        HashSet<ChessMove> validMoves = new HashSet<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPosition position = new ChessPosition(i+1, j+1);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == team) {
                    validMoves.addAll(validMoves(position));
                }
            }
        }

        return !validMoves.isEmpty();
    }

    private boolean canCastle(TeamColor team, CastleSide side) {
        boolean canCastleOnSide = (team == TeamColor.WHITE ? (
                (side == CastleSide.KINGSIDE ? whiteKingsideCastle : whiteQueensideCastle))
                : (side == CastleSide.KINGSIDE ? blackKingsideCastle : blackQueensideCastle));
        if (!canCastleOnSide || isInCheck(team)) {
            return false;
        }
        int dir = (side == CastleSide.KINGSIDE ? 1 : -1);
        int currentCol = 5;
        int row = (team == TeamColor.WHITE ? 1 : 8);
        ChessPosition oldPosition = new ChessPosition(row, 5);
        while (currentCol < 7 && currentCol > 2) {
            currentCol += dir;
            ChessPosition position = new ChessPosition(row, currentCol);
            ChessPiece piece = board.getPiece(position);
            if (piece != null) {
                return false;
            }
            if (makeGhostMove(new ChessMove(oldPosition, position, null), team)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTurn == chessGame.currentTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, board);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "currentTurn=" + currentTurn +
                ", board=" + board +
                '}';
    }
}
