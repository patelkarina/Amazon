package amazons;


import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

import static amazons.Piece.*;

/** The state of an Amazons Game.
 *  @author Karina Patel
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 10;

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (this == model) {
            return;
        }
        _turn = model.turn();
        _winner = model.winner();
        if (_winner == null) {
            _winner = EMPTY;
        }
        _pieceboard = model.pieceboard().clone();
        _moveCount = model.numMoves();
        _moveHistory = new Stack<Move>();
        _moveHistory.addAll(model._moveHistory);
    }

    /** Clears the board to the initial position. */
    void init() {
        _turn = WHITE;
        _winner = EMPTY;

        _pieceboard = new Piece[SIZE * SIZE];
        for (int i = 0; i < _pieceboard.length; i++) {
            _pieceboard[i] = Piece.EMPTY;
        }

        put(Piece.WHITE, Square.sq(0, 3));
        put(Piece.WHITE, Square.sq(3, 0));
        put(Piece.WHITE, Square.sq(6, 0));
        put(Piece.WHITE, Square.sq(9, 3));

        put(Piece.BLACK, Square.sq(9, 6));
        put(Piece.BLACK, Square.sq(0, 6));
        put(Piece.BLACK, Square.sq(6, 9));
        put(Piece.BLACK, Square.sq(3, 9));

        _moveCount = 0;
        _moveHistory = new Stack<>();
    }

    /** Return the Piece whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the number of moves (that have not been undone) for this
     *  board. */
    int numMoves() {
        return _moveCount;
    }

    /** Return the winner in the current position, or null if the game is
     *  not yet finished. */
    Piece winner() {
        if (_winner.equals(EMPTY)) {
            return null;
        }
        return _winner;
    }

    /** Returns the board. */
    Piece[] pieceboard() {
        return _pieceboard;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW < 9. */
    final Piece get(int col, int row) {
        return _pieceboard[col * SIZE + row];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        put(p, s.col(), s.row());
    }

    /** Set square (COL, ROW) to P. */
    final void put(Piece p, int col, int row) {
        _pieceboard[col * SIZE + row] = p;
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, col - 'a', row - '1');
    }

    /** Return true iff FROM - TO is an unblocked queen move on the current
     *  board, ignoring the contents of ASEMPTY, if it is encountered.
     *  For this to be true, FROM-TO must be a queen move and the
     *  squares along it, other than FROM and ASEMPTY, must be
     *  empty. ASEMPTY may be null, in which case it has no effect. */
    boolean isUnblockedMove(Square from, Square to, Square asEmpty) {
        if (from.isQueenMove(to)) {
            int direction = from.direction(to);
            while (from != to) {
                Square next = from.queenMove(direction, 1);
                if (get(next) != EMPTY && next != asEmpty) {
                    return false;
                }
                from = next;
            }
            return true;
        } else {
            return false;
        }
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        if (from == null) {
            return false;
        }
        Piece x = get(from);
        return ((x == WHITE && _turn == WHITE)
                || (x == BLACK && _turn == BLACK));
    }

    /** Return true iff FROM-TO is a valid first part of move, ignoring
     *  spear throwing. */
    boolean isLegal(Square from, Square to) {
        if (isLegal(from)) {
            return isUnblockedMove(from, to, null);
        }
        return false;
    }

    /** Return true iff FROM-TO(SPEAR) is a legal move in the current
     *  position. */
    boolean isLegal(Square from, Square to, Square spear) {
        if (isLegal(from, to)) {
            return isUnblockedMove(to, spear, from);
        }
        return false;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        if (move == null) {
            return false;
        }
        return isLegal(move.from(), move.to(), move.spear());
    }


    /** Move FROM-TO(SPEAR), assuming this is a legal move. */
    void makeMove(Square from, Square to, Square spear) {

        if (winner() != null) {
            return;
        }

        Piece x = get(from);
        put(x, to);
        put(Piece.EMPTY, from);
        put(Piece.SPEAR, spear);
        Move m = Move.mv(from, to, spear);

        _moveCount += 1;
        _moveHistory.push(m);

        Iterator<Move> me = legalMoves(_turn);
        Iterator<Move> opp = legalMoves(_turn.opponent());
        if (!me.hasNext() && !opp.hasNext()) {
            _winner = _turn.opponent();
        } else if (!opp.hasNext()) {
            _winner = _turn;
        }

        if (_turn == BLACK) {
            _turn = WHITE;
        } else {
            _turn = BLACK;
        }

    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to(), move.spear());

    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (winner() != null) {
            return;
        }

        Move previous = _moveHistory.pop();

        Piece toMoveBack = get(previous.to());
        put(toMoveBack, previous.from());
        put(EMPTY, previous.to());
        put(EMPTY, previous.spear());

        _moveCount--;

        if (_turn == WHITE) {
            _turn = BLACK;
        } else {
            _turn = WHITE;
        }
    }

    /** Return an Iterator over the Squares that are reachable by an
     *  unblocked queen move from FROM. Does not pay attention to what
     *  piece (if any) is on FROM, nor to whether the game is finished.
     *  Treats square ASEMPTY (if non-null) as if it were EMPTY.  (This
     *  feature is useful when looking for Moves, because after moving a
     *  piece, one wants to treat the Square it came from as empty for
     *  purposes of spear throwing.) */
    Iterator<Square> reachableFrom(Square from, Square asEmpty) {

        return new ReachableFromIterator(from, asEmpty);
    }

    /** Return an Iterator over all legal moves on the current board. */
    Iterator<Move> legalMoves() {
        return new LegalMoveIterator(_turn);
    }

    /** Return an Iterator over all legal moves on the current board for
     *  SIDE (regardless of whose turn it is). */
    Iterator<Move> legalMoves(Piece side) {
        return new LegalMoveIterator(side);
    }

    /** An iterator used by reachableFrom. */
    private class ReachableFromIterator implements Iterator<Square> {

        /** Iterator of all squares reachable by queen move from FROM,
         *  treating ASEMPTY as empty. */
        ReachableFromIterator(Square from, Square asEmpty) {
            _from = from;
            _dir = -1;
            _steps = 0;
            _asEmpty = asEmpty;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _dir < 8;
        }

        @Override
        public Square next() {
            Square next = _from.queenMove(_dir, _steps);
            toNext();
            return next;
        }

        /** Advance _dir and _steps, so that the next valid Square is
         *  _steps steps in direction _dir from _from. */
        private void toNext() {
            Square next = null;
            while (_dir < 8 && !isUnblockedMove(_from, next, _asEmpty)) {
                _steps++;
                if (_steps > 9) {
                    _steps = 0;
                    _dir++;
                }
                next = _from.queenMove(_dir, _steps);
            }
        }

        /** Starting square. */
        private Square _from;
        /** Current direction. */
        private int _dir;
        /** Current distance. */
        private int _steps;
        /** Square treated as empty. */
        private Square _asEmpty;
    }

    /** An iterator used by legalMoves. */
    private class LegalMoveIterator implements Iterator<Move> {

        /** All legal moves for SIDE (WHITE or BLACK). */
        LegalMoveIterator(Piece side) {
            _startingSquares = Square.iterator();
            _spearThrows = NO_SQUARES;
            _pieceMoves = NO_SQUARES;
            _fromPiece = side;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _spearThrows.hasNext();
        }

        @Override
        public Move next() {
            Move next = Move.mv(_start, _nextSquare, _spearThrows.next());
            toNext();
            return next;
        }

        /** Advance so that the next valid Move is
         *  _start-_nextSquare(sp), where sp is the next value of
         *  _spearThrows. */
        private void toNext() {
            while (!_spearThrows.hasNext()) {
                while (!_pieceMoves.hasNext()) {
                    _start = null;
                    while (_startingSquares.hasNext() && _start == null) {
                        Square candidate = _startingSquares.next();
                        if (get(candidate) == _fromPiece) {
                            _start = candidate;
                        }
                    }

                    if (_start == null) {
                        return;
                    }

                    _pieceMoves = reachableFrom(_start, null);
                }

                _nextSquare = _pieceMoves.next();
                _spearThrows = reachableFrom(_nextSquare, _start);
            }
        }

        /** Color of side whose moves we are iterating. */
        private Piece _fromPiece;
        /** Current starting square. */
        private Square _start;
        /** Remaining starting squares to consider. */
        private Iterator<Square> _startingSquares;
        /** Current piece's new position. */
        private Square _nextSquare;
        /** Remaining moves from _start to consider. */
        private Iterator<Square> _pieceMoves;
        /** Remaining spear throws from _piece to consider. */
        private Iterator<Square> _spearThrows;
    }

    @Override
    public String toString() {
        String b = "";
        for (int i = SIZE - 1; i >= 0; i--) {
            b = b + " " + " ";
            for (int j = 0; j < SIZE; j++) {
                b = b + " " + _pieceboard[j * SIZE + i].toString();
            }
            b += "\n";
        }
        return b;
    }

    /** An empty iterator for initialization. */
    private static final Iterator<Square> NO_SQUARES =
            Collections.emptyIterator();

    /** Piece whose turn it is (BLACK or WHITE). */
    private Piece _turn;

    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;

    /**
     * The cache of all pieces on the board.
     */
    private Piece[] _pieceboard;

    /**
     * The number of moves for this board.
     */
    private int _moveCount;

    /**
     * Accounts for all the moves made.
     */
    private Stack<Move> _moveHistory;
}