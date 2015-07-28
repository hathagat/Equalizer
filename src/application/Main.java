package application;
	
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
	@Override
	public void start(Stage window) throws IOException {
		
		Parent root = FXMLLoader.load(getClass().getResource("layout.fxml"));

		// Fensterinhalt
		Scene scene = new Scene(root, 800, 600);
		
		// Fenster
		window.setScene(scene);
		window.setTitle("triple2 Equalizer");
		window.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
