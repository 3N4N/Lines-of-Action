import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

import java.util.ArrayList;

public class Piece extends StackPane {
    public static final int TILE_SIZE = Game.TILE_SIZE;
    private PieceType curPlayer;
    private Tile[][] board;

    private PieceType type;
    private double mouseX, mouseY;
    private double oldX, oldY;
    private ArrayList<TilePosition> availableMoves;

    public PieceType getType() {
        return type;
    }

    public double getOldX() {
        return oldX;
    }

    public double getOldY() {
        return oldY;
    }

    public void setCurPlayer(PieceType curPlayer) {
        this.curPlayer = curPlayer;
    }

    public ArrayList<TilePosition> getAvailableMoves() {
        return availableMoves;
    }

    public void setAvailableMoves(ArrayList<TilePosition> availableMoves) {
        this.availableMoves = availableMoves;
    }

    public Piece(PieceType type, int x, int y, Tile[][] board, PieceType curPlayer) {
        this.type = type;
        this.curPlayer = curPlayer;
        this.board = board;

        move(x, y);

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

        setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e. getSceneY();
        });

        setOnMouseDragged(e -> {
            if (type == this.curPlayer) {
                relocate(e.getSceneX() - mouseX + oldX, e.getSceneY() - mouseY + oldY);
                for (TilePosition pos : availableMoves) {
                    this.board[pos.x][pos.y].changeColor(TileColor.GREEN);
                }
            }
        });
    }

    public void move(int x, int y) {
        oldX = x * TILE_SIZE;
        oldY = y * TILE_SIZE;
        relocate(oldX, oldY);
    }

    public void abortMove() {
        relocate(oldX, oldY);
    }
}
