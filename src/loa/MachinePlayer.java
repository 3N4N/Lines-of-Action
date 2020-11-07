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

        return machineMoveRandom(board);
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
}
