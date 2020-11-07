package loa;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Game extends Application {
    public static final int TILE_SIZE = 80;
    public static final int TILES = 6;

    public void start(Stage primaryStage) {
        GameUtil util = new GameUtil();
        Player player1 = new HumanPlayer(util);
        Player player2 = new MachinePlayer(util);
        util.setPlayer1(player1);
        util.setPlayer2(player2);

        Scene scene = new Scene(util.createContent());
        primaryStage.setTitle("Lines of Action");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}
