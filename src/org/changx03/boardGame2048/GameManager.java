package org.changx03.boardGame2048;

import javafx.scene.Group;

import java.util.List;
import java.util.stream.Collectors;

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

    public void move(Direction direction) {
        List<Tile> tiles = board.getGridGroup().getChildren().stream()
                .filter(g->g instanceof Tile).map(t->(Tile)t)
                .collect(Collectors.toList());
        board.getGridGroup().getChildren().removeAll(tiles);
        tiles.forEach(t->{
            Tile newTile = Tile.newTile(t.getValue());
            final Location newLoc=t.getLocation().offset(direction);
            if(newLoc.isValidFor() && !tiles.stream().filter(t2->t2.getLocation().equals(newLoc)).findAny().isPresent()){
                newTile.setLocation(newLoc);
            } else {
                newTile.setLocation(t.getLocation());
            }
            board.addTile(newTile);
        });
    }
}
