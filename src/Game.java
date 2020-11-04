import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.LinkedList;

public class Game extends Application {
    public static final int TILE_SIZE = 80;
    public static final int TILES = 8;

    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();

    private Tile[][] board = new Tile[TILES][TILES];
    private PieceType curPlayer = PieceType.RED;

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(TILES * TILE_SIZE, TILES * TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);

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
                    piece.setAvailableMoves(availableMoves(x, y));
                }
            }
        }

        return root;
    }

    private MoveResult tryMove(Piece piece, int newX, int newY) {
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

    private int toBoard(double pixel) {
        return (int) (pixel + TILE_SIZE / 2) / TILE_SIZE;
    }

    private Piece makePiece(PieceType type, int x, int y) {
        Piece piece = new Piece(type, x, y, board, curPlayer);

        piece.setOnMouseReleased(e -> {
            int newX = toBoard(piece.getLayoutX());
            int newY = toBoard(piece.getLayoutY());
            int oldX = toBoard(piece.getOldX());
            int oldY = toBoard(piece.getOldY());

            MoveResult result;

            if (newX == oldX && newY == oldY) {
                result = new MoveResult(MoveType.NONE);
            }
            else if (newX < 0 || newY < 0 || newX >= TILES || newY >= TILES) {
                result = new MoveResult(MoveType.NONE);
            }
            else {
                result = tryMove(piece, newX, newY);
            }

            switch (result.getType()) {
                case NONE:
                    piece.abortMove();
                    break;
                case NORMAL:
                    piece.move(newX, newY);
                    board[oldX][oldY].setPiece(null);
                    board[newX][newY].setPiece(piece);
                    changeCurrentPlayer();
                    break;
                case KILL:
                    piece.move(newX, newY);
                    board[oldX][oldY].setPiece(null);
                    board[newX][newY].setPiece(piece);

                    Piece otherPiece = result.getPiece();
                    pieceGroup.getChildren().remove(otherPiece);
                    changeCurrentPlayer();
                    break;
            }

            resetBoard();

            if (hasWon(PieceType.RED) && hasWon(PieceType.WHITE))
                System.out.println("It's a TIE!");
            else if (hasWon(PieceType.RED))
                    System.out.println("RED Won");
            else if (hasWon(PieceType.WHITE))
                    System.out.println("WHITE Won!!!");
        });

        return piece;
    }

    private void changeCurrentPlayer() {
        curPlayer = curPlayer == PieceType.RED ? PieceType.WHITE : PieceType.RED;
    }

    /**
     * Resets the LOA board. The tiles are redrawn with default colors.
     * For each piece, the available tiles for next move are recounted.
     */
    private void resetBoard() {
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
                    board[i][j].getPiece().setAvailableMoves(availableMoves(i, j));
                }
            }
        }
    }

    /**
     * Provides a set of tiles available for making the next move.
     *
     * @param x column number of the tile
     * @param y row number of the tile
     * @return an arraylist of the tiles available for next move
     */
    private ArrayList<TilePosition> availableMoves(int x, int y) {
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
                if (hasPieceOnXY(i, y, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(x + totalPiece, y, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x + totalPiece, y));
        }
        if (isWithinBoard(x - totalPiece, y)) {
            oppFound = false;
            occupied = false;
            for (int i = x - totalPiece + 1; i < x; i++) {
                if (hasPieceOnXY(i, y, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(x - totalPiece, y, ownType))
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
                if (hasPieceOnXY(x, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(x, y + totalPiece, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x, y + totalPiece));
        }
        if (isWithinBoard(x, y - totalPiece)) {
            oppFound = false;
            occupied = false;
            for (int j = y - totalPiece + 1; j < y; j++) {
                if (hasPieceOnXY(x, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(x, y - totalPiece, ownType))
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
                if (hasPieceOnXY(i, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(x - totalPiece, y - totalPiece, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x - totalPiece, y - totalPiece));
        }
        if (isWithinBoard(x + totalPiece, y + totalPiece)) {
            oppFound = false;
            occupied = false;
            for (int i = x + 1, j = y + 1; i < x + totalPiece && j < y + totalPiece; i++, j++) {
                if (hasPieceOnXY(i, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(x + totalPiece, y + totalPiece, ownType))
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
                if (hasPieceOnXY(i, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(x - totalPiece, y + totalPiece, ownType))
                occupied = true;
            if (!oppFound && !occupied)
                availableTiles.add(new TilePosition(x - totalPiece, y + totalPiece));
        }
        if (isWithinBoard(x + totalPiece, y - totalPiece)) {
            oppFound = false;
            occupied = false;
            for (int i = x + 1, j = y - 1; i < x + totalPiece && j > y - totalPiece; i++, j--) {
                if (hasPieceOnXY(i, j, oppType)) {
                    oppFound = true;
                    break;
                }
            }
            if (hasPieceOnXY(x + totalPiece, y - totalPiece, ownType))
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
     * @return true if the pieces of `side` are contiguous
     */
    private boolean hasWon(PieceType side) {
        int nrPcs = 0;
        int ctPcs = 0;

        boolean[][] visited = new boolean[TILES][TILES];
        TilePosition firstSpot = null;
        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                visited[i][j] = false;
                if (board[i][j].hasPiece() && board[i][j].getPiece().getType() == side) {
                    nrPcs++;
                    if (firstSpot == null)
                        firstSpot = board[i][j].getPosition();
                }
            }
        }

        if (firstSpot == null) {
            System.out.println("FUCK!");
            return false;
        }

        LinkedList<TilePosition> queue = new LinkedList<>();
        queue.add(firstSpot);
        visited[firstSpot.x][firstSpot.y] = true;
        ctPcs++;

        while (!queue.isEmpty()) {
            TilePosition pos = queue.remove();
            int i = pos.x;
            int j = pos.y;

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
                            ctPcs++;
                        }
                    }
                }
            }
        }

        return ctPcs == nrPcs;
    }

    /**
     * Checks if the specified tile is inside the board.
     *
     * @param x column number of the tile
     * @param y row number of the tile
     * @return true if tile at column x and row y is inside the board
     */
    private boolean isWithinBoard(int x, int y) {
        return x >= 0 && x < TILES && y >= 0 && y < TILES;
    }

    /**
     * Checks if the piece on the tile is of the specified type.
     *
     * @param x column number of the tile
     * @param y row number of the tile
     * @param type type of the piece (RED or WHITE)
     * @return true if the piece at col x and row y is of `type`
     */
    private boolean hasPieceOnXY(int x, int y, PieceType type) {
        if (!isWithinBoard(x, y)) return false;
        if (!board[x][y].hasPiece()) return false;
        return board[x][y].getPiece().getType() == type;
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(createContent());
        primaryStage.setTitle("Lines of Action");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}
