package loa;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.LinkedList;

import static loa.Game.TILES;
import static loa.Game.TILE_SIZE;

public class GameUtil {
    /**
     * Table for determining weight of tile position.
     */
    public static final int[][] pieceSquareTable = {
            { -80, -25, -20, -20, -20, -20, -25, -80 },
            { -25,  10,  10,  10,  10,  10,  10, -25 },
            { -20,  10,  25,  25,  25,  25,  10, -20 },
            { -20,  10,  25,  50,  50,  25,  10, -20 },
            { -20,  10,  25,  50,  50,  25,  10, -20 },
            { -20,  10,  25,  25,  25,  25,  10, -20 },
            { -25,  10,  10,  10,  10,  10,  10, -25 },
            { -80, -25, -20, -20, -20, -20, -25, -80 }
    };

    public Group tileGroup = new Group();
    public Group pieceGroup = new Group();

    /** The game board. */
    private Tile[][] board = new Tile[TILES][TILES];

    /** Tracks the current player. */
    private PieceType curPlayer = PieceType.RED;

    /** The user. Definitely human. Plays red side. */
    private Player player1;

    /** The opponent. Either human or machine. Plays white side. */
    private Player player2;

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(TILES * TILE_SIZE, TILES * TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);

        /*
         * Initialize the board.
         */
        for (int y = 0; y < TILES; y++) {
            for (int x = 0; x < TILES; x++) {
                board[x][y] = new Tile(((x + y) % 2 == 0), x, y);
                tileGroup.getChildren().add(board[x][y]);

                Piece piece = null;
                if ((y == 0 || y == TILES - 1) && (x > 0 && x < TILES - 1)) {
                    piece = makePiece(PieceType.RED, x, y);
                }
                if ((x == 0 || x == TILES - 1) && (y > 0 && y < TILES - 1)) {
                    piece = makePiece(PieceType.WHITE, x, y);
                }
                if (piece != null) {
                    board[x][y].setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }
            }
        }

        for (int y = 0; y < TILES; y++) {
            for (int x = 0; x < TILES; x++) {
                Piece piece = board[x][y].getPiece();
                if (piece != null) {
                    piece.setCurPlayer(curPlayer);
                    piece.setAvailableMoves(availableMoves(board, x, y));
                }
            }
        }

        return root;
    }

    /**
     * Makes a new piece and handles mouse-release event on it.
     * <p>
     * Creates a piece and listens for mouse-release event.
     * When the event is fired, the piece is moved to the position of the mouse.
     *
     * @param type the color of the piece
     * @param x the row
     * @param y the col
     * @return the created piece
     */
    private Piece makePiece(PieceType type, int x, int y) {
        Piece piece = new Piece(type, x, y, board);

        piece.setOnMouseReleased(e -> {
            int newX = toBoard(piece.getLayoutX());
            int newY = toBoard(piece.getLayoutY());
            int oldX = toBoard(piece.getOldX());
            int oldY = toBoard(piece.getOldY());
            player1.setMoveFrom(new TilePosition(oldX, oldY));
            player1.setMoveTo(new TilePosition(newX, newY));
            MoveResult result = player1.makeMove(board);
            if (result.getType() != MoveType.NONE
                    && player2 instanceof MachinePlayer) {
                player2.makeMove(board);
            }
        });

        return piece;
    }

    private int toBoard(double pixel) {
        return (int) (pixel + TILE_SIZE / 2) / TILE_SIZE;
    }

    /**
     * Moves the piece to a new position on the board if valid
     * and kill the opponent piece if any is at the new position.
     *
     * @param board the game board
     * @param piece the piece to be moved
     * @param oldX the row to move the piece from
     * @param oldY the col to move the piece from
     * @param newX the row to move the piece to
     * @param newY the col to move the piece to
     * @return the result of the move
     */
    public MoveResult movePiece(Tile[][] board, Piece piece,
                                int oldX, int oldY, int newX, int newY) {
        MoveResult result;

        if (newX == oldX && newY == oldY) {
            System.out.println("Wat!!");
            result = new MoveResult(MoveType.NONE);
        }
        else if (newX < 0 || newY < 0 || newX >= TILES || newY >= TILES) {
            result = new MoveResult(MoveType.NONE);
        }
        else {
            result = tryMove(board, piece, newX, newY);
        }

        switch (result.getType()) {
            case NONE:
                piece.abortMove();
                break;
            case NORMAL:
                piece.move(newX, newY);
                board[oldX][oldY].setPiece(null);
                board[newX][newY].setPiece(piece);
                if (player2 instanceof HumanPlayer)
                    curPlayer = changeCurrentPlayer();
                break;
            case KILL:
                piece.move(newX, newY);
                board[oldX][oldY].setPiece(null);
                board[newX][newY].setPiece(piece);

                Piece otherPiece = result.getPiece();
                pieceGroup.getChildren().remove(otherPiece);
                if (player2 instanceof HumanPlayer)
                    curPlayer = changeCurrentPlayer();
                break;
        }

        resetBoard(board);
        declareWinner();

        return result;
    }

    /**
     * Returns a MoveResult object containing the validity of a move
     * and the piece of the opponent if any will be killed in the move.
     *
     * @param board the game board
     * @param piece the piece to be moved
     * @param newX the row of the new tile
     * @param newY the col of the new tile
     * @return the result of the move
     */
    public MoveResult tryMove(Tile[][] board, Piece piece, int newX, int newY) {
        TilePosition newPosition = new TilePosition(newX, newY);
        if (piece.getAvailableMoves().contains(newPosition)) {
            if (board[newX][newY].hasPiece()) {
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            }
            else {
                return new MoveResult(MoveType.NORMAL);
            }
        }

        return new MoveResult(MoveType.NONE);
    }

    public void declareWinner() {
        if (hasWon(board, PieceType.RED) && hasWon(board, PieceType.WHITE))
            System.out.println("It's a TIE!");
        else if (hasWon(board, PieceType.RED))
            System.out.println("RED Won");
        else if (hasWon(board, PieceType.WHITE))
            System.out.println("WHITE Won!!!");

        Platform.exit();
    }

    /**
     * Resets the LOA board.
     * <p>
     * The tiles are redrawn with default colors.
     * The pieces are informed of the current player.
     * For each piece, the available tiles for next move are recounted.
     */
    public void resetBoard(Tile[][] board) {
        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                if ((i + j) % 2 == 0) {
                    board[i][j].changeColor(TileColor.LIGHT);
                }
                else {
                    board[i][j].changeColor(TileColor.DARK);
                }

                if (board[i][j].hasPiece()) {
                    board[i][j].getPiece().setCurPlayer(curPlayer);
                    board[i][j].getPiece().setAvailableMoves(availableMoves(board, i, j));
                }
            }
        }
    }

    /**
     * Returns a set of tiles available for making the next move.
     *
     * @param x col number of the tile
     * @param y row number of the tile
     * @return an arraylist of the tiles available for next move
     */
    public ArrayList<TilePosition> availableMoves(Tile[][] board, int x, int y) {
        ArrayList<TilePosition> availableTiles = new ArrayList<>();
        PieceType ownType = board[x][y].getPiece().getType();
        PieceType oppType = ownType == PieceType.RED ? PieceType.WHITE : PieceType.RED;

        /*
         * Check the availability of horizontal tiles
         */
        int totalPiece = 0;
        boolean oppFound, occupied;
        for (int i = 0; i < TILES; i++) {
            if (board[i][y].hasPiece()) totalPiece++;
        }
        if (isWithinBoard(x + totalPiece, y)) {
            oppFound = false;
            occupied = false;
            for (int i = x + 1; i < x + totalPiece; i++) {
                if (hasPieceOnXY(board, i, y, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(board, x + totalPiece, y, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x + totalPiece, y));
        }
        if (isWithinBoard(x - totalPiece, y)) {
            oppFound = false;
            occupied = false;
            for (int i = x - totalPiece + 1; i < x; i++) {
                if (hasPieceOnXY(board, i, y, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(board, x - totalPiece, y, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x - totalPiece, y));
        }

        /*
         * Check the availability of vertical tiles
         */
        totalPiece = 0;
        for (int i = 0; i < TILES; i++) {
            if (board[x][i].hasPiece()) totalPiece++;
        }
        if (isWithinBoard(x, y + totalPiece)) {
            oppFound = false;
            occupied = false;
            for (int j = y + 1; j < y + totalPiece; j++) {
                if (hasPieceOnXY(board, x, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(board, x, y + totalPiece, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x, y + totalPiece));
        }
        if (isWithinBoard(x, y - totalPiece)) {
            oppFound = false;
            occupied = false;
            for (int j = y - totalPiece + 1; j < y; j++) {
                if (hasPieceOnXY(board, x, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(board, x, y - totalPiece, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x, y - totalPiece));
        }

        /*
         * Check the availability of diagonal tiles going from top-left to bottom-right
         */
        totalPiece = 0;
        for (int i = x, j = y; i >= 0 && i < TILES && j >= 0 && j < TILES; i--, j--) {
            if (board[i][j].hasPiece()) totalPiece++;
        }
        for (int i = x, j = y; i >= 0 && i < TILES && j >= 0 && j < TILES; i++, j++) {
            if (board[i][j].hasPiece()) totalPiece++;
        }
        totalPiece--; // counted the piece in board[x][y] twice
        if (isWithinBoard(x - totalPiece, y - totalPiece)) {
            oppFound = false;
            occupied = false;
            for (int i = x - totalPiece + 1, j = y - totalPiece + 1; i < x && j < y; i++, j++) {
                if (hasPieceOnXY(board, i, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(board, x - totalPiece, y - totalPiece, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x - totalPiece, y - totalPiece));
        }
        if (isWithinBoard(x + totalPiece, y + totalPiece)) {
            oppFound = false;
            occupied = false;
            for (int i = x + 1, j = y + 1; i < x + totalPiece && j < y + totalPiece; i++, j++) {
                if (hasPieceOnXY(board, i, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(board, x + totalPiece, y + totalPiece, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x + totalPiece, y + totalPiece));
        }

        /*
         * Check the availability of diagonal tiles going from bottom-left to top-right
         */
        totalPiece = 0;
        for (int i = x, j = y; i >= 0 && i < TILES && j >= 0 && j < TILES; i--, j++) {
            if (board[i][j].hasPiece()) totalPiece++;
        }
        for (int i = x, j = y; i >= 0 && i < TILES && j >= 0 && j < TILES; i++, j--) {
            if (board[i][j].hasPiece()) totalPiece++;
        }
        totalPiece--; // counted the piece in board[x][y] twice
        if (isWithinBoard(x - totalPiece, y + totalPiece)) {
            oppFound = false;
            occupied = false;
            for (int i = x - totalPiece + 1, j = y + totalPiece - 1; i < x && j > y; i++, j--) {
                if (hasPieceOnXY(board, i, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(board, x - totalPiece, y + totalPiece, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x - totalPiece, y + totalPiece));
        }
        if (isWithinBoard(x + totalPiece, y - totalPiece)) {
            oppFound = false;
            occupied = false;
            for (int i = x + 1, j = y - 1; i < x + totalPiece && j > y - totalPiece; i++, j--) {
                if (hasPieceOnXY(board, i, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(board, x + totalPiece, y - totalPiece, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x + totalPiece, y - totalPiece));
        }

        return availableTiles;
    }

    /**
     * Checks if the specified side has won.
     * <p>
     * A side wins if all the pieces of the side are contiguous.
     *
     * @param side the side
     * @return true if side param has won
     */
    private boolean hasWon(Tile[][] board, PieceType side) {
        int nrPcs = totalPcs(board, side);
        int ctPcs = contPcs(board, side);

        if (ctPcs == -1) {
            return false;
        }

        return nrPcs == ctPcs;
    }

    /**
     * Checks if the game is over.
     * <p>
     * A game is over when a player wins or the game ends in a tie.
     *
     * @param board the game board
     * @return true if the game is over
     */
    private boolean gameOver(Tile[][] board) {
        return hasWon(board, PieceType.WHITE) || hasWon(board, PieceType.RED);
    }

    /**
     * Returns a PieceType object of the winning side.
     *
     * @param board the game board
     * @return piece type of the winner if the game is over, otherwise null
     */
    public PieceType getWinner(Tile[][] board) {
        if (hasWon(board, PieceType.RED) && hasWon(board, PieceType.WHITE))
            return PieceType.NONE;
        if (hasWon(board, PieceType.RED))
            return PieceType.RED;
        if (hasWon(board, PieceType.WHITE))
            return PieceType.WHITE;

        return null;
    }

    /**
     * Returns the total number of pieces of a side on the board.
     *
     * @param board the game board
     * @param side the side of the pieces to be counted
     * @return the number of pieces of side param
     */
    private int totalPcs(Tile[][] board, PieceType side) {
        int count = 0;
        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                if (hasPieceOnXY(board, i, j, side))
                    count++;
            }
        }

        return count;
    }

    /**
     * Returns the number of contiguous pieces of a side in a random cluster.
     *
     * @param board the game board
     * @param side the side to count the pieces of
     * @return the number of contiguous pieces of side param
     */
    private int contPcs(Tile[][] board, PieceType side) {
        TilePosition firstSpot = null;
        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                if (hasPieceOnXY(board, i, j, side)) {
                    if (firstSpot == null)
                        firstSpot = board[i][j].getPosition();
                }
            }
        }

        if (firstSpot == null) {
            System.out.println("ERROR: No piece of " + side + " is on the board.");
            return -1;
        }

        return ctgPcsAt(board, side, firstSpot);
    }

    /**
     * Returns the number of contiguous pieces of a side at a given cluster.
     *
     * @param board the game board
     * @param side the side to count the pieces of
     * @param firstSpot the tile around where to count the cluster
     * @return the number of contiguous pieces of side param
     */
    int ctgPcsAt(Tile[][] board, PieceType side, TilePosition firstSpot) {
        int count = 0;
        boolean[][] visited = new boolean[TILES][TILES];

        LinkedList<TilePosition> queue = new LinkedList<>();
        queue.add(firstSpot);
        visited[firstSpot.getX()][firstSpot.getY()] = true;
        count++;

        while (!queue.isEmpty()) {
            TilePosition pos = queue.remove();
            int i = pos.getX();
            int j = pos.getY();

            int[] intI = {i - 1, i , i + 1};
            int[] intJ = {j - 1, j, j + 1};

            for (int m = 0; m < 3; m++) {
                for (int n = 0; n < 3; n++) {
                    if (m == 1 && n == 1) continue;
                    if (isWithinBoard(intI[m], intJ[n]) && !visited[intI[m]][intJ[n]]) {
                        Piece p = board[intI[m]][intJ[n]].getPiece();
                        if (p != null && p.getType() == side) {
                            queue.add(board[intI[m]][intJ[n]].getPosition());
                            visited[intI[m]][intJ[n]] = true;
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }


    /**
     * Checks if the specified tile is inside the board.
     *
     * @param x col number of the tile
     * @param y row number of the tile
     * @return true if tile at col x and row y is inside the board
     */
    private boolean isWithinBoard(int x, int y) {
        return x >= 0 && x < TILES && y >= 0 && y < TILES;
    }

    /**
     * Checks if the piece on the tile is of the specified type.
     *
     * @param x col number of the tile
     * @param y row number of the tile
     * @param type type of the piece (RED or WHITE)
     * @return true if the piece at col x and row y is of `type`
     */
    public boolean hasPieceOnXY(Tile[][] board, int x, int y, PieceType type) {
        if (!isWithinBoard(x, y)) return false;
        if (!board[x][y].hasPiece()) return false;
        return board[x][y].getPiece().getType() == type;
    }
}

