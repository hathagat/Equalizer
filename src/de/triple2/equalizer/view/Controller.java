package de.triple2.equalizer.view;

import java.io.File;

import de.triple2.equalizer.controller.SoundProcessor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

/**
 * Interface Logik
 */
public class Controller {

    public BorderPane borderPane;
    public HBox hBoxTop, hBoxCenter, hBoxBottom;
    public TextField textFieldOpen;
    public Button buttonOpen;
	public Button buttonPlay;
    public Slider slider1, slider2, slider3, slider4, slider5, slider6, slider7, slider8, slider9, slider10;
    public Label labelName, labelTime;

    private File file;
    private final FileChooser fileChooser = new FileChooser();
	private SoundProcessor soundProcessor = new SoundProcessor();

    /**
     * Wird bei Klick auf den Open Button aufgerufen.
     */
    public void handleButtonOpen() {
    	// öffne Datei Auswahl Dialog
        configureFileChooser(fileChooser, "Lade Musik Datei");
        file = fileChooser.showOpenDialog(Main.getStage());

        // falls Datei gewählt wurde
        if (file != null) {
            textFieldOpen.setText(file.getAbsolutePath());
        }
    }

    /**
     * Wird bei Klick auf den Play Button aufgerufen.
     */
    public void handleButtonPlay() {
    	if(buttonPlay.getText().equals("Abspielen")) {
        	// hole den Pfad aus dem Textfeld
            file = new File(textFieldOpen.getText());

        	if(file.exists()) {
    			buttonPlay.setText("Stop");
        		// spiele die Datei in neuem Thread ab
        		Runnable run = () -> {

        			soundProcessor.playSound(file);
        			buttonPlay.setText("Abspielen");
        		};
        		Thread thread = new Thread(run);
        		thread.start();
        	}
        	else {
        		// Fehlermeldungen
        		if(textFieldOpen.getText().equals("")) {
        			showError("Abspielen nicht möglich!", "Keine Datei angegeben.", "");
        		}
        		else {
            		showError("Abspielen nicht möglich!", "Datei konnte nicht gefunden werden:", file.getAbsolutePath());
        		}
        	}

            // TODO labelName.setText(fileName);
            // labelTime.setText(time);
    	}
    	else {
    		// bei Klick auf Stop
    		soundProcessor.stopSound();
    	}

    }

    /**
     * Konfiguriert den Öffnen Dialog.
     *
     * @param chooser Der Öffnen Dialog.
     * @param title Der Titel des Dialog Fenster.
     */
    private void configureFileChooser(final FileChooser chooser, final String title) {
        chooser.setTitle(title);
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("WAV", "*.wav"));
    }

    /**
     * Zeigt einen Fehler Dialog an.
     * @param headerText Überschrift
     * @param contentText Text der Fehlermeldung
     * @param filePath Pfad zur Datei
     */
    private void showError(String headerText, String contentText, String filePath) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Fehler");
		alert.setHeaderText(headerText);
		alert.setContentText(contentText + "\n" + filePath);

		alert.showAndWait();
    }
}
