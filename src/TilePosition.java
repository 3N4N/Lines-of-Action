public class TilePosition {
    private int x, y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    TilePosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        TilePosition objPos = (TilePosition) obj;

        return objPos.x == this.x && objPos.y == this.y;
    }

    @Override
    public int hashCode() {
        return this.x + this.y;
    }
}
