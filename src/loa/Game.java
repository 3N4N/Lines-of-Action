package loa;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.util.Optional;

public class Game extends Application {
    public static final int TILE_SIZE = 80;
    public static final int TILES = 6;

    public void start(Stage primaryStage) {
        GameUtil util = new GameUtil();
        Player player1 = new HumanPlayer(util);
        Player player2 = null;

        /*
         * Create a dialog box to determine opponent type
         */
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Lines of Action");
        dialog.setContentText("Play against:");

        ButtonType type = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType typeHuman = new ButtonType("Human");
        ButtonType typeMachine = new ButtonType("Machine");

        // Add buttons to the dialog pane
        dialog.getDialogPane().getButtonTypes().addAll(type, typeHuman, typeMachine);

        // Display the dialog box and take input
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.orElseThrow() == typeHuman) {
            player2 = new HumanPlayer(util);
        } else if (result.orElseThrow() == typeMachine) {
            player2 = new MachinePlayer(util);
        } else if (result.orElseThrow() == type) {
            return;
        }

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
