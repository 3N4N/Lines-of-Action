import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {
    private Piece piece;
    private TilePosition position;
    private TileColor color;

    public boolean hasPiece() {
        return piece != null;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public TilePosition getPosition() {
        return position;
    }

    public void setPosition(TilePosition position) {
        this.position = position;
    }

    public void setColor(TileColor color) {
        this.color = color;
    }

    public TileColor getColor() {
        return color;
    }

    public Tile(boolean light, int x, int y) {
        position = new TilePosition(x, y);
        color = light ? TileColor.LIGHT : TileColor.DARK;

        setWidth(Game.TILE_SIZE);
        setHeight(Game.TILE_SIZE);

        relocate(x * Game.TILE_SIZE, y * Game.TILE_SIZE);
        setFill(color == TileColor.LIGHT ? Color.valueOf("#feb") : Color.valueOf("#582"));
    }

    /**
     * Change the color of the tile
     * @param color the color to redraw the tile in
     */
    public void changeColor(TileColor color) {
        this.color = color;

        if (color == TileColor.DARK)
            setFill(Color.valueOf("#582"));
        else if (color == TileColor.LIGHT)
            setFill(Color.valueOf("#feb"));
        else
            setFill(Color.YELLOW);
    }
}
