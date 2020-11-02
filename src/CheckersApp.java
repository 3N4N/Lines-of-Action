import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class CheckersApp extends Application {
    public static final int TILE_SIZE = 80;
    public static final int HEIGHT = 8;
    public static final int WIDTH = 8;

    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile(((x+y) % 2 == 0), x, y);
                tileGroup.getChildren().add(tile);

                Piece piece = null;
                if (y <= 2 && (x + y) % 2 != 0) {
                    piece = new Piece(PieceType.RED, x, y);
                }
                if (y >= 5 && (x + y) % 2 != 0) {
                    piece = new Piece(PieceType.WHITE, x, y);
                }
                if (piece != null) {
                    tile.setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }
            }
        }

        return root;
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(createContent());
        primaryStage.setTitle("Checkers");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}
