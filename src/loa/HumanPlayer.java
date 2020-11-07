package loa;

public class HumanPlayer extends Player {
    public HumanPlayer(GameUtil util) {
        super(util);
    }

    public MoveResult makeMove(Tile[][] board) {
        int oldX = moveFrom.getX();
        int oldY = moveFrom.getY();
        int newX = moveTo.getX();
        int newY = moveTo.getY();

        Piece piece = board[oldX][oldY].getPiece();

        return util.movePiece(board, piece, oldX, oldY, newX, newY);
    }
}
