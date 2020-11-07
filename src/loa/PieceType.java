package loa;

public enum PieceType {
    RED(1),
    WHITE(-1),
    NONE(0);

    final int moveDirection;

    PieceType(int moveDirection) {
        this.moveDirection = moveDirection;
    }

}
