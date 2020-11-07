package loa;

import java.util.ArrayList;
import java.util.Random;

import static loa.Game.TILES;

public class MachinePlayer extends Player {
    public MachinePlayer(GameUtil util) {
        super(util);
    }

    @Override
    public MoveResult makeMove(Tile[][] board) {

//        return machineMoveRandom(board);
        return machineMoveMiniMax(board);
    }

    private MoveResult machineMoveRandom(Tile[][] board) {
        ArrayList<TilePosition> pcsPos = new ArrayList<>();
        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                if (util.hasPieceOnXY(board, i, j, PieceType.WHITE)) {
                    pcsPos.add(board[i][j].getPosition());
                }
            }
        }

        int pos, oldX, oldY, newX, newY;
        Piece piece;
        ArrayList<TilePosition> availPos;
        Random rand = new Random();
        do {
            pos = rand.nextInt(pcsPos.size());
            oldX = pcsPos.get(pos).getX();
            oldY = pcsPos.get(pos).getY();
            piece = board[oldX][oldY].getPiece();

            availPos = util.availableMoves(board, oldX, oldY);
        } while(availPos.size() == 0);

        pos = rand.nextInt(availPos.size());
        newX = availPos.get(pos).getX();
        newY = availPos.get(pos).getY();

        return util.movePiece(board, piece, oldX, oldY, newX, newY);
    }

    private MoveResult machineMoveMiniMax(Tile[][] board) {
        int score = 0;
        int bestScore = Integer.MIN_VALUE;
        int oldX = -1;
        int oldY = -1;
        int newX = -1;
        int newY = -1;

        Tile[][] dummyBoard = new Tile[TILES][TILES];
        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                Piece piece = board[i][j].getPiece();
                dummyBoard[i][j] = new Tile(((i + j) % 2 == 0), i, j);
                if (piece != null) {
                    dummyBoard[i][j].setPiece(new Piece(piece.getType(), i, j, dummyBoard));
                }
            }
        }

        util.resetBoard(dummyBoard);

        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                if (!util.hasPieceOnXY(dummyBoard, i, j, PieceType.WHITE)) continue;
                Piece piece = dummyBoard[i][j].getPiece();
                piece.setAvailableMoves(util.availableMoves(dummyBoard, i, j));
                ArrayList<TilePosition> availableMoves = piece.getAvailableMoves();

                /*
                 * Move piece to all available positions,
                 * calculate the scores for each move,
                 * and make the move with the best score.
                 */
                for (TilePosition pos : availableMoves) {
                    // Store the piece that might get killed in the move
                    Piece otherPiece = dummyBoard[pos.getX()][pos.getY()].getPiece();

                    // Move piece to a valid tile
                    dummyBoard[i][j].setPiece(null);
                    dummyBoard[pos.getX()][pos.getY()].setPiece(piece);

                    // Calculate the score of the new game state
                    score = minimax(dummyBoard, 3, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

                    // Move piece to its previous tile
                    dummyBoard[i][j].setPiece(piece);
                    dummyBoard[pos.getX()][pos.getY()].setPiece(null);

                    // Restore the piece that might have gotten killed in the move
                    dummyBoard[pos.getX()][pos.getY()].setPiece(otherPiece);

                    if (score > bestScore) {
                        bestScore = score;
                        oldX = i;
                        oldY = j;
                        newX = pos.getX();
                        newY = pos.getY();
                    }
                }
            }
        }

        setMoveFrom(new TilePosition(oldX, oldY));
        setMoveTo(new TilePosition(newX, newY));

        return util.movePiece(board, board[oldX][oldY].getPiece(), oldX, oldY, newX, newY);
    }

    private int minimax(Tile[][] board, int depth,
                        int alpha, int beta, boolean isMaximizing) {
        int score = 0;
        int bestScore = 0;
        PieceType winner = util.getWinner(board);

        if (depth == 0) {
            return getScore(board, PieceType.WHITE);
        }

        if (winner != null) {
            if (winner == PieceType.WHITE) {
                return Integer.MAX_VALUE - 1;
            }
            else if (winner == PieceType.RED) {
                return Integer.MIN_VALUE + 1;
            }
            else {
                return 0;
            }
        }

        if (isMaximizing) {
            bestScore = Integer.MIN_VALUE;
            boolean breakFlag = false;

            for (int i = 0;  i < TILES && !breakFlag; i++) {
                for (int j = 0; j < TILES && !breakFlag; j++) {
                    if (!util.hasPieceOnXY(board, i, j, PieceType.WHITE)) continue;
                    Piece piece = board[i][j].getPiece();
                    piece.setAvailableMoves(util.availableMoves(board, i, j));
                    ArrayList<TilePosition> availableMoves = piece.getAvailableMoves();

                    /*
                     * Move piece to all available positions, calculate
                     * the scores for each move, and store the best score.
                     */
                    for (TilePosition pos : availableMoves) {
                        // Store the piece that might get killed in the move
                        Piece otherPiece = board[pos.getX()][pos.getY()].getPiece();

                        // Move piece to a valid tile
                        board[i][j].setPiece(null);
                        board[pos.getX()][pos.getY()].setPiece(piece);

                        // Calculate the score of the new game state
                        score = minimax(board, depth - 1, alpha, beta, false);

                        // Move piece to its previous tile
                        board[i][j].setPiece(piece);
                        board[pos.getX()][pos.getY()].setPiece(null);

                        // Restore the piece that might have gotten killed in the move
                        board[pos.getX()][pos.getY()].setPiece(otherPiece);

                        if (score > bestScore) bestScore = score;
                        if (score > alpha) alpha = score;
                        if (beta <= alpha) breakFlag = true;
                    }

                }
            }
        }
        else {
            bestScore = Integer.MAX_VALUE;
            boolean breakFlag = false;

            for (int i = 0;  i < TILES && !breakFlag; i++) {
                for (int j = 0; j < TILES && !breakFlag; j++) {
                    if (!util.hasPieceOnXY(board, i, j, PieceType.RED)) continue;
                    Piece piece = board[i][j].getPiece();
                    piece.setAvailableMoves(util.availableMoves(board, i, j));
                    ArrayList<TilePosition> availableMoves = piece.getAvailableMoves();

                    /*
                     * Move piece to all available positions, calculate
                     * the scores for each move, and store the best score.
                     */
                    for (TilePosition pos : availableMoves) {
                        // Store the piece that might get killed in the move
                        Piece otherPiece = board[pos.getX()][pos.getY()].getPiece();

                        // Move piece to a valid tile
                        board[i][j].setPiece(null);
                        board[pos.getX()][pos.getY()].setPiece(piece);

                        // Calculate the score of the new game state
                        score = minimax(board, depth - 1, alpha, beta, true);

                        // Move piece to its previous tile
                        board[i][j].setPiece(piece);
                        board[pos.getX()][pos.getY()].setPiece(null);

                        // Restore the piece that might have gotten killed in the move
                        board[pos.getX()][pos.getY()].setPiece(otherPiece);

                        if (score < bestScore) bestScore = score;
                        if (score < alpha) alpha = score;
                        if (beta <= alpha) breakFlag = true;
                    }

                }
            }
        }

        return bestScore;
    }

    /**
     * Returns a heuristic score of the game state.
     *
     * @param board the game board
     * @param side the side for which the score is to be calculated
     * @return the heuristic score of the game state
     */
    public int getScore(Tile[][] board, PieceType side) {
        int weightOfWeight        =  1;
        int weightOfDensity       = -1;
        int weightOfArea          = -1;
        int weightOfMobility      =  1;
        int weightOfConnectedness =  1;
        int weightOfQuadCount     =  1;

        return weightOfWeight * getTotalWeight(board, side)
                + weightOfDensity * getDensityScore(board, side)
                + weightOfArea * getArea(board, side)
                + weightOfMobility * getMobility(board, side)
                + weightOfConnectedness * getConnectedness(board, side)
                + weightOfQuadCount * getQuadCount(board, side);
    }

    private int getTotalWeight(Tile[][] board, PieceType side) {
        int weight = 0;

        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                if (util.hasPieceOnXY(board, i, j, side)) {
                    int z = (8 - TILES) / 2;
                    weight += GameUtil.pieceSquareTable[i + z][j + z];
                }
            }
        }

        return weight;
    }

    private TilePosition getCenterOfMass(Tile[][] board, PieceType side) {
        int x = 0;
        int y = 0;
        int count = 0;
        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                if (util.hasPieceOnXY(board, i, j, side)) {
                    count++;
                    x += i;
                    y += j;
                }
            }
        }

        return new TilePosition((x / count), (y / count));
    }

    private int getDensityScore(Tile[][] board, PieceType side) {
        TilePosition centerOfMass = getCenterOfMass(board, side);
        int cx = centerOfMass.getX();
        int cy = centerOfMass.getY();
        int d = 0;

        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                if (util.hasPieceOnXY(board, i, j, side)) {
                    int dx = Math.abs(cx - i);
                    int dy = Math.abs(cy - j);
                    d += Math.sqrt(dx * dx + dy * dy);
                }
            }
        }

        return d;
    }

    private int getArea(Tile[][] board, PieceType side) {
        int fx = Integer.MAX_VALUE;
        int fy = Integer.MAX_VALUE;
        int lx = Integer.MIN_VALUE;
        int ly = Integer.MIN_VALUE;

        for (int y = 0; y < TILES; y++) {
            for (int x = 0; x < TILES; x++) {
                if (util.hasPieceOnXY(board, x, y, side)) {
                    fx = Math.min(fx, x);
                    fy = Math.min(fy, y);
                    lx = Math.max(lx, x);
                    ly = Math.max(ly, y);
                }
            }
        }

        int len =  Math.abs(fx - lx);
        int wid = Math.abs(fy - ly);

        return len * wid;
    }

    private int getMobility(Tile[][] board, PieceType side) {
        ArrayList<TilePosition> availableTiles = new ArrayList<>();
        ArrayList<TilePosition> tmp = new ArrayList<>();
        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                if (util.hasPieceOnXY(board, i, j, side)) {
                    tmp = util.availableMoves(board, i, j);
                    if (!availableTiles.containsAll(tmp))
                        availableTiles.addAll(tmp);
                }
            }
        }

        return availableTiles.size();
    }

    private int getConnectedness(Tile[][] board, PieceType side) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                if (util.hasPieceOnXY(board, i, j, side)) {
                    TilePosition pos = new TilePosition(i, j);
                    max = Math.max(max, util.ctgPcsAt(board, side, pos));
                }
            }
        }

        return max;
    }

    private int getQuadCount(Tile[][] board, PieceType side) {
        int total = 0;
        int count = 0;

        for (int i = 0; i < TILES; i++) {
            for (int j = 0; j < TILES; j++) {
                count = 0;

                int[] intI = { i, i + 1 };
                int[] intJ = { j, j + 1 };

                for (int k = 0; k < 2; k++) {
                    for (int l = 0; l < 2; l++) {
                        if (util.hasPieceOnXY(board, intI[k], intJ[l], side)) {
                            count++;
                        }
                    }
                }

                if (count >= 3) {
                    total += count;
                }
            }
        }

        return total;
    }
}
