import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {

    public Tile(boolean light, int x, int y) {
        setWidth(CheckersApp.TILE_SIZE);
        setHeight(CheckersApp.TILE_SIZE);

        relocate(x * CheckersApp.TILE_SIZE, y * CheckersApp.TILE_SIZE);
        setFill(light ? Color.valueOf("#feb") : Color.valueOf("#582"));
    }
}
