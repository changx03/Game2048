package org.changx03.boardGame2048;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

import java.util.Random;

/**
 * Created by gungr on 1/12/2016.
 */
public class Tile extends Label {

    private int value;
    private Location location;
    private boolean merged;

    public  Tile(int value) {
        final int squareSize = Board.CELL_SIZE - 13;
        setMinSize(squareSize, squareSize);
        setMaxSize(squareSize, squareSize);
        setPrefSize(squareSize, squareSize);

        getStyleClass().addAll("game-label", "game-tile-" + value);

        setAlignment(Pos.CENTER);

        this.value = value;
        this.merged = false;
        setText(Integer.toString(value));
    }

    public int getValue(){ return value; }

    public static Tile newRandomTile() {

        return newTile(new Random().nextDouble() < 0.9 ? 2 : 4);
    }

    public static Tile newTile(int value) {
        return new Tile(value);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void merge(Tile another) {
        getStyleClass().remove("game-tile-" + value);
        this.value += another.getValue();
        setText(Integer.toString(value));
        merged = true;
        getStyleClass().add("game-tile-" + value);
    }

    public boolean isMergeable(Tile anotherTile) {
        return anotherTile != null && getValue()==anotherTile.getValue();
    }

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }
}
