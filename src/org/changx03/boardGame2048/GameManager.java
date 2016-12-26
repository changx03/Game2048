package org.changx03.boardGame2048;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.util.Duration;

/**
 * Created by gungr on 1/12/2016.
 */
public class GameManager extends Group {

    private Board board;
    private final Map<Location, Tile> gameGrid = new HashMap<>();
    private final List<Location> locations = new ArrayList<>();
    private volatile boolean movingTiles = false;
    private final ParallelTransition parallelTransition = new ParallelTransition();
    private int tilesWereMoved=0;
    private final Set<Tile> mergedToBeRemoved = new HashSet<>();

    public GameManager() {
        board = new Board();
        getChildren().add(board);

        initializeGameGrid();
        startGame();
    }

    private void initializeGameGrid() {
        gameGrid.clear();
        locations.clear();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Location location = new Location(i, j);
                locations.add(location);
                gameGrid.put(location, null);
            }
        }
    }

    public void startGame() {
        Tile tile0 = Tile.newRandomTile();
//        tile0.setLocation(new Location(1,2));
//        board.addTile(tile0);

        List<Location> locCopy = locations.stream().collect(Collectors.toList());
        Collections.shuffle(locCopy);
        tile0.setLocation(locCopy.get(0));
        gameGrid.put(tile0.getLocation(), tile0);
        Tile tile1 = Tile.newRandomTile();
        tile1.setLocation(locCopy.get(1));
        gameGrid.put(tile1.getLocation(), tile1);

        redrawTilesInGameGrid();
    }

    private void redrawTilesInGameGrid() {
        gameGrid.values().stream().filter(Objects::nonNull).forEach(board::addTile);

    }

    private Location findFarthestLocation(Location location, Direction direction) {
        Location farthest = location;
        do {
            farthest = location;
            location = farthest.offset(direction);
        } while (location.isValidFor() && gameGrid.get(location) == null);
        return farthest;
    }

    public void move(Direction direction) {
        synchronized (gameGrid) {
            if (movingTiles) {
                return;
            }
        }
//        List<Tile> tiles = board.getGridGroup().getChildren().stream()
//                .filter(g -> g instanceof Tile).map(t -> (Tile) t)
//                .collect(Collectors.toList());
//        board.getGridGroup().getChildren().removeAll(tiles);
//        tiles.forEach(t -> {
//            Tile newTile = Tile.newTile(t.getValue());
//            final Location newLoc = t.getLocation().offset(direction);
//            if (newLoc.isValidFor() && !tiles.stream().filter(t2 -> t2.getLocation().equals(newLoc)).findAny().isPresent()) {
//                newTile.setLocation(newLoc);
//            } else {
//                newTile.setLocation(t.getLocation());
//            }
//            board.addTile(newTile);
//        });

        IntStream.range(0, 4).boxed().forEach(i -> {
            IntStream.range(0, 4).boxed().forEach(j -> {
                Tile t = gameGrid.get(new Location(i, j));
                if (t != null) {
                    final Location newLoc = findFarthestLocation(t.getLocation(), direction);
                    if (!newLoc.equals(t.getLocation())) {
//                        board.moveTile(t, newLoc);
                        parallelTransition.getChildren().add(animateExistingTile(t, newLoc));

                        gameGrid.put(newLoc, t);
                        gameGrid.replace(t.getLocation(), null);
                        t.setLocation(newLoc);
                    }
                }
            });
        });

        parallelTransition.setOnFinished(e -> {
            synchronized (gameGrid) {
                movingTiles = false;
            }
        });

        synchronized (gameGrid) {
            movingTiles = true;
        }
        parallelTransition.play();
        parallelTransition.getChildren().clear();
    }

    private Timeline animateExistingTile(Tile tile, Location newLocation) {
        Timeline timeline = new Timeline();
        // Animate tiles movement from actual location to new location in 65ms
        KeyValue kvX = new KeyValue(tile.layoutXProperty(),
                newLocation.getLayoutX(Board.CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);
        KeyValue kvY = new KeyValue(tile.layoutYProperty(),
                newLocation.getLayoutY(Board.CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);

        KeyFrame kfX = new KeyFrame(Duration.millis(65), kvX);
        KeyFrame kfY = new KeyFrame(Duration.millis(65), kvY);

        timeline.getKeyFrames().add(kfX);
        timeline.getKeyFrames().add(kfY);

        return timeline;
    }

    private void addAndAnimateRandomTile(Location randomLocation) {
        // Scale from 0 to 1 in 125 ms the new tile added to the board
        Tile tile = Tile.newRandomTile();
        tile.setLocation(randomLocation);
        tile.setScaleX(0);
        tile.setScaleY(0);
        board.addTile(tile);
        gameGrid.put(tile.getLocation(), tile);

        final ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(125), tile);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        // TO-DO: Step 37. After last movement on full grid, check if there are movements available

        scaleTransition.play();

    }
}
