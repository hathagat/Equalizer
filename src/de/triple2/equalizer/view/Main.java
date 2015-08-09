package de.triple2.equalizer.view;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Programm Start
 */
public class Main extends Application {
    private static Stage window;

	@Override
	public void start(Stage primaryStage) throws IOException {
		window = primaryStage;

		Parent root = FXMLLoader.load(getClass().getResource("layout.fxml"));

		// Fensterinhalt
		Scene scene = new Scene(root, 800, 500);

		// Fenster
		window.setScene(scene);
		window.setTitle("triple2 Equalizer");
		window.setMinWidth(576);
		window.setMinHeight(320);
		window.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

    public static Stage getStage() {
        return window;
    }
}
