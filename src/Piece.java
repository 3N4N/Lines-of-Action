import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

public class Piece extends StackPane {
    public static final int TILE_SIZE = CheckersApp.TILE_SIZE;

    private PieceType type;
    public PieceType getType() { return type; }

    public Piece(PieceType type, int x, int y) {
        this.type = type;

        relocate(x * TILE_SIZE, y * TILE_SIZE);

        Ellipse bg = new Ellipse(TILE_SIZE * 0.3125, TILE_SIZE * 0.26);
        bg.setFill(Color.BLACK);
        bg.setStroke(Color.BLACK);
        bg.setStrokeWidth(TILE_SIZE * 0.03);
        bg.setTranslateX((TILE_SIZE - TILE_SIZE * 0.3125 * 2) / 2);
        bg.setTranslateY((TILE_SIZE - TILE_SIZE * 0.26 * 2) / 2 + TILE_SIZE * 0.07);

        Ellipse fg = new Ellipse(TILE_SIZE * 0.3125, TILE_SIZE * 0.26);
        fg.setFill(type == PieceType.RED ? Color.valueOf("#c40003") : Color.valueOf("#fff9f4"));
        fg.setStroke(Color.BLACK);
        fg.setStrokeWidth(TILE_SIZE * 0.03);
        fg.setTranslateX((TILE_SIZE - TILE_SIZE * 0.3125 * 2) / 2);
        fg.setTranslateY((TILE_SIZE - TILE_SIZE * 0.26 * 2) / 2);

        getChildren().addAll(bg, fg);
    }
}
