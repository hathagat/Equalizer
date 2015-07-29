package de.triple2.equalizer.view;

import java.io.File;

import de.triple2.equalizer.controller.EqualizerService;
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
public class LayoutController {

    public BorderPane borderPane;
    public HBox hBoxTop, hBoxCenter, hBoxBottom;
    public TextField textFieldOpen;
    public Button buttonOpen;
	public Button buttonPlay;
    public Slider slider1, slider2, slider3, slider4, slider5, slider6, slider7, slider8, slider9, slider10;
    public Label labelPath, labelName, labelTime;

    private File file;
    private final FileChooser fileChooser = new FileChooser();
	private SoundProcessor soundProcessor = new SoundProcessor();
	private EqualizerService musicService;

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

            // Dateiendung überprüfen
            boolean isWav = false;
            if(soundProcessor.getFileExtension(file).equals("wav") ||
            		soundProcessor.getFileExtension(file).equals("WAV")) {
            	isWav = true;
            }

            // falls Datei korrekt
        	if(file.exists() && isWav) {
    			buttonPlay.setText("Stop");
        		// spiele die Datei in neuem Service ab
    			musicService = new EqualizerService(file);
    			musicService.setSoundProcessor(soundProcessor);
    			musicService.setOnSucceeded(e -> {
    				System.out.println("Done: ");
    				buttonPlay.setText("Abspielen");
    			});
    			musicService.setOnCancelled(e -> {
    				buttonPlay.setText("Abspielen");
    			});
    			musicService.start();

                labelName.setText("Datei:\n" + file.getName());
                // TODO evtl aktuelle Zeit anzeigen
                labelTime.setText("Länge:\n" + soundProcessor.getLength(file) + " Sekunden");
        	}
        	else {
        		// Fehlermeldungen
        		if(textFieldOpen.getText().equals("")) {
        			showError("Abspielen nicht möglich!", "Keine Datei angegeben.", "");
        		}
        		else if(file.exists() && !isWav) {
        			showError("Abspielen nicht möglich!", "Datei ist kein Wavesound:", file.getAbsolutePath());
        		}
        		else {
            		showError("Abspielen nicht möglich!", "Datei konnte nicht gefunden werden:", file.getAbsolutePath());
        		}
        	}
    	}
    	else {
    		// bei Klick auf Stop
    		musicService.cancel();
    	}

    }

    /**
     * Konfiguriert den Öffnen Dialog.
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
