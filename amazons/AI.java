package amazons;

import java.util.Iterator;

import static java.lang.Math.*;

import static amazons.Piece.*;

/** A Player that automatically generates moves.
 *  @author Karina Patel
 */
class AI extends Player {

    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {

        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }

        Move current = null;
        Iterator<Move> legalMoves;
        if (sense == 1) {
            legalMoves = board.legalMoves(WHITE);
        } else {
            legalMoves = board.legalMoves(BLACK);
        }

        Board temp = new Board(board);
        while (legalMoves.hasNext()) {
            current = legalMoves.next();
            temp.makeMove(current);
            int response = findMove(temp, depth - 1,
                    saveMove, sense * -1, alpha, beta);
            temp.undo();

            if (sense == 1) {
                beta = min(beta, response);
                alpha = max(alpha, response);
                if (beta <= alpha) {
                    break;
                }
            } else {
                beta = min(beta, response);
                alpha = max(alpha, response);
                if (beta <= alpha) {
                    break;
                }
            }
        }

        if (saveMove) {
            _lastFoundMove = current;
        }

        if (sense == 1) {
            return alpha;
        }
        return beta;
    }


    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private int maxDepth(Board board) {
        int N = board.numMoves();
        return 2;
    }


    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        Piece winner = board.winner();
        if (winner == BLACK) {
            return -WINNING_VALUE;
        } else if (winner == WHITE) {
            return WINNING_VALUE;
        }

        int mobilityWhite = 0;
        int mobilityBlack = 0;

        Iterator<Move> white = board.legalMoves(WHITE);
        Iterator<Move> black = board.legalMoves(BLACK);

        while (white.hasNext()) {
            mobilityWhite++;
            white.next();
        }

        while (black.hasNext()) {
            mobilityBlack++;
            black.next();
        }

        int difference = mobilityWhite - mobilityBlack;
        return difference;
    }
}
