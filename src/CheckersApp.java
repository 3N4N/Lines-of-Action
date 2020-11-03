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

    Tile[][] board = new Tile[HEIGHT][WIDTH];

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                board[x][y] = new Tile(((x+y) % 2 == 0), x, y);
                tileGroup.getChildren().add(board[x][y]);

                Piece piece = null;
                if ((y == 0 || y == 7) && (x > 0 && x < 7)) {
                    piece = new Piece(PieceType.RED, x, y);
                }
                if ((x == 0 || x == 7) && (y > 0 && y < 7)) {
                    piece = new Piece(PieceType.WHITE, x, y);
                }
                if (piece != null) {
                    board[x][y].setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }
            }
        }

        return root;
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(createContent());
        primaryStage.setTitle("Lines of Action");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}
