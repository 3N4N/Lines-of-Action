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

    public static void main(String[] args) {
        launch(args);
    }

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        root.getChildren().addAll(tileGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile(((x+y) % 2 == 0), x, y);
                tileGroup.getChildren().add(tile);
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
}
