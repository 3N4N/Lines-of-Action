package loa;

public abstract class Player {
    GameUtil util;

    TilePosition moveFrom;
    TilePosition moveTo;

    public TilePosition getMoveFrom() {
        return moveFrom;
    }

    public void setMoveFrom(TilePosition moveFrom) {
        this.moveFrom = moveFrom;
    }

    public TilePosition getMoveTo() {
        return moveTo;
    }

    public void setMoveTo(TilePosition moveTo) {
        this.moveTo = moveTo;
    }

    public Player(GameUtil util) {
        this.util = util;
    }

    public abstract MoveResult makeMove(Tile[][] board);
}
