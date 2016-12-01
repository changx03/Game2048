package org.changx03.boardGame2048;

import javafx.scene.Group;

/**
 * Created by gungr on 1/12/2016.
 */
public class GameManager extends Group {

    private Board board;

    public GameManager(){
        board = new Board();
        getChildren().add(board);

        startGame();
    }

    public void startGame() {
        Tile tile0 = Tile.newRandomTile();
        tile0.setLocation(new Location(1,2));
        board.addTile(tile0);
    }
}
