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
    private int tilesWereMoved = 0;
    private final Set<Tile> mergedToBeRemoved = new HashSet<>();

    public GameManager() {
        board = new Board();
        getChildren().add(board);

        board.resetGameProperty().addListener((ov, b, b1) -> {
            if (b1) {
                initializeGameGrid();
                startGame();
            }
        });

        initializeGameGrid();
        startGame();
    }

    private void initializeGameGrid() {
        gameGrid.clear();
        locations.clear();
        GridOperator.traverseGrid((i, j) -> {
            Location location = new Location(i,j);
            locations.add(location);
            gameGrid.put(location, null);
            return 0;
        });
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

        // Sort grid before traversing it
        GridOperator.sortGrid(direction);
        // reset points
        board.setPoints(0);

        tilesWereMoved = GridOperator.traverseGrid((i,j)->{
            Tile t=gameGrid.get(new Location(i,j));
            if(t!=null){
                final Location newLoc=findFarthestLocation(t.getLocation(),direction);

                Location nextLocation = newLoc.offset(direction);
                Tile tileToBeMerged = nextLocation.isValidFor() ? gameGrid.get(nextLocation) : null;
                if (tileToBeMerged != null && !tileToBeMerged.isMerged() && t.isMergeable(tileToBeMerged)) {
                    tileToBeMerged.merge(t);
                    tileToBeMerged.toFront();
                    gameGrid.put(nextLocation, tileToBeMerged);
                    gameGrid.replace(t.getLocation(), null);
                    parallelTransition.getChildren().add(animateExistingTile(t, nextLocation));
                    parallelTransition.getChildren().add(animateMergedTile(tileToBeMerged));
                    mergedToBeRemoved.add(t);

                    // Add points
                    board.addPoints(tileToBeMerged.getValue());

                    // Check for a winning tile
                    if(tileToBeMerged.getValue()==2048){
                        System.out.println("You win!");
                        board.setGameWin(true);
                    }

                    return 1;
                }

                if(!newLoc.equals(t.getLocation())){
                    parallelTransition.getChildren().add(animateExistingTile(t, newLoc));
                    gameGrid.put(newLoc, t);
                    gameGrid.replace(t.getLocation(),null);
                    t.setLocation(newLoc);
                    return 1;
                }
            }
            return 0;
        });

        // Call animate score
        board.animateScore();

        parallelTransition.setOnFinished(e -> {
            synchronized (gameGrid) {
                movingTiles = false;
            }

            board.getGridGroup().getChildren().removeAll(mergedToBeRemoved);
            mergedToBeRemoved.clear();
            gameGrid.values().stream().filter(Objects::nonNull).forEach(t->t.setMerged(false));

            Location randomAvailableLocation = findRandomAvailableLocation();
            if (randomAvailableLocation != null){
//                addAndAnimateRandomTile(randomAvailableLocation);
                if(tilesWereMoved>0){
                    addAndAnimateRandomTile(randomAvailableLocation);
                }
            } else {
                if(mergeMovementsAvailable()==0){
                    System.out.println("Game Over");
                    board.setGameOver(true);
                }
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

        scaleTransition.setOnFinished(e -> {
            if (gameGrid.values().parallelStream().noneMatch(Objects::isNull) && mergeMovementsAvailable()==0 ) {
                System.out.println("Game Over");
            }
        });

        scaleTransition.play();
    }

    private Location findRandomAvailableLocation() {
        Location location = null;
        // From empty tiles remaining, get a random position

        List<Location> availableLocations = locations.stream().filter(l -> gameGrid.get(l) == null)
                .collect(Collectors.toList());

        if (availableLocations.isEmpty()) {
            return null;
        }

        Collections.shuffle(availableLocations);
        location = availableLocations.get(0);

        return location;
    }

    private SequentialTransition animateMergedTile(Tile tile) {
        final ScaleTransition scale0 = new ScaleTransition(Duration.millis(80), tile);
        scale0.setToX(1.2);
        scale0.setToY(1.2);
        scale0.setInterpolator(Interpolator.EASE_IN);
        final ScaleTransition scale1 = new ScaleTransition(Duration.millis(80), tile);
        scale1.setToX(1.0);
        scale1.setToY(1.0);
        scale1.setInterpolator(Interpolator.EASE_OUT);
        return new SequentialTransition(scale0, scale1);
    }

    private int mergeMovementsAvailable() {
        final AtomicInteger numMergeableTile = new AtomicInteger();
        Stream.of(Direction.UP, Direction.LEFT).parallel().forEach(direction -> {
            GridOperator.traverseGrid((x, y) -> {
                Location thisloc = new Location(x, y);
                Tile t1=gameGrid.get(thisloc);
                if(t1!=null){
                    Location nextLoc=thisloc.offset(direction);
                    if(nextLoc.isValidFor()){
                        Tile t2=gameGrid.get(nextLoc);
                        if(t2!=null && t1.isMergeable(t2)){
                            numMergeableTile.incrementAndGet();
                        }
                    }
                }
                return 0;
            });
        });
        return numMergeableTile.get();
    }
}
