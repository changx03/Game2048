package org.changx03.boardGame2048;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by gungr on 1/12/2016.
 */
public class Game2048 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        StackPane root = new StackPane();

        GameManager gameManager = new GameManager();
        root.getChildren().add(gameManager);

        Scene scene = new Scene(root, 600, 700);

        primaryStage.setTitle("Game 2048");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
