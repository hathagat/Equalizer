package application;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Interface Logik
 */
public class Controller {

	public BorderPane borderPane;
	public HBox hBoxTop, hBoxCenter, hBoxBottom;
	public TextField textFieldOpen;
	public Button buttonOpen, buttonPlay, buttonStop;
	public Slider slider1, slider2, slider3, slider4, slider5, slider6, slider7, slider8, slider9, slider10;
	public Label labelName, labelTime;

	private String filePath;

	/**
	 * Wird bei Klick auf den Open Button aufgerufen.
	 */
	public void handleButtonOpen() {
		// TODO FileChooser
		// textFieldOpen.setText(fileChooser);
	}

	/**
	 * Wird bei Klick auf den Play Button aufgerufen.
	 */
	public void handleButtonPlay() {
		filePath = textFieldOpen.getText();
		// TODO Datei abspielen
		// labelName.setText(fileName);
		// labelTime.setText(time);
	}

	/**
	 * Wird bei Klick auf den Stop Button aufgerufen.
	 */
	public void handleButtonStop() {
		// TODO abspielen stoppen
	}

	public String getFilePath() {
		return filePath;
	}
}
