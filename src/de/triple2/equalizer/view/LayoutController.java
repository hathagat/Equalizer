package de.triple2.equalizer.view;

import java.io.File;

import de.triple2.equalizer.controller.SoundProcessor;
import de.triple2.equalizer.controller.SoundService;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;

/**
 * Interface Logik
 */
public class LayoutController {

	// Menü
	public MenuItem menuItemOpen, menuItemClose, menuItemAbout;
	public Menu menuSlider, menuBuffer;
	public ToggleGroup toggleGroupGain, toggleGroupSlider, toggleGroupBuffer;
	public RadioMenuItem radioMenuItemGain5, radioMenuItemGain10, radioMenuItemGain15;
	public RadioMenuItem radioMenuItemSlider5, radioMenuItemSlider7, radioMenuItemSlider10;
	public RadioMenuItem radioMenuItemBuffer1024, radioMenuItemBuffer4096, radioMenuItemBuffer8192, radioMenuItemBuffer16384;
	// Slider
	public Label labeldBTopLeft, labeldBBottomLeft, labeldBTopRight, labeldBBottomRight;
	public Slider slider1, slider2, slider3, slider4, slider5, slider6, slider7, slider8, slider9, slider10;
	// Media Player
	public Button buttonPlay;
	public Label labelClipping, labelName, labelTime;
	// Diverses
	private File file;
	private final FileChooser fileChooser = new FileChooser();
	private SoundProcessor soundProcessor = new SoundProcessor(this);
	private SoundService soundService;

	/*
	 * Menü Handling
	 */

	/**
	 * Wird bei Klick auf Öffnen aufgerufen.
	 */
	public void handleMenuOpen() {
		// öffne Datei Auswahl Dialog
		configureFileChooser(fileChooser, "Lade Musik Datei");
		file = fileChooser.showOpenDialog(Main.getStage());
		// Zeige Titel und Länge an
		if(file != null) {
			labelName.setText("Datei:\n" + file.getName());
			labelTime.setText("Länge:\n" + soundProcessor.getLength(file) + " Sekunden");
			buttonPlay.setDisable(false);
		}
	}

	/**
	 * Wird bei Klick auf Beenden aufgerufen.
	 */
	public void handleMenuClose() {
		// schließe die aktuelle Stage und damit das Programm
		Main.getStage().close();
	}
	
	/**
	 * Wird bei Auswahl der Verstärkung aufgerufen.
	 */
	public void handleMenuGain() {
		Toggle selectedToggle = toggleGroupGain.getSelectedToggle();

		if(selectedToggle == radioMenuItemGain5) {
			setSliderGain(5);
		}
		else if(selectedToggle == radioMenuItemGain10) {
			setSliderGain(10);
		}
		else if(selectedToggle == radioMenuItemGain15) {
			setSliderGain(15);
		}
	}

	/**
	 * Wird bei Auswahl der Bänder Anzahl aufgerufen.
	 */
	public void handleMenuSlider() {
		Toggle selectedToggle = toggleGroupSlider.getSelectedToggle();

		if(selectedToggle == radioMenuItemSlider5) {

			// TODO setCoefficicentCount(5);
			// Slider ausblenden
			slider6.setVisible(false);
			slider7.setVisible(false);
			slider8.setVisible(false);
			slider9.setVisible(false);
			slider10.setVisible(false);
			// reservierten Platz frei machen
			slider6.setManaged(false);
			slider7.setManaged(false);
			slider8.setManaged(false);
			slider9.setManaged(false);
			slider10.setManaged(false);
			// Werte zurücksetzen
			slider6.setValue(0);
			slider7.setValue(0);
			slider8.setValue(0);
			slider9.setValue(0);
			slider10.setValue(0);
		}
		else if(selectedToggle == radioMenuItemSlider7) {

			// TODO setCoefficicentCount(7);
			// Slider einblenden
			slider6.setVisible(true);
			slider7.setVisible(true);
			// Platz reservieren
			slider6.setManaged(true);
			slider7.setManaged(true);
			// Slider ausblenden
			slider8.setVisible(false);
			slider9.setVisible(false);
			slider10.setVisible(false);
			// reservierten Platz frei machen
			slider8.setManaged(false);
			slider9.setManaged(false);
			slider10.setManaged(false);
			// Werte zurücksetzen
			slider8.setValue(0);
			slider9.setValue(0);
			slider10.setValue(0);
		}
		else if(selectedToggle == radioMenuItemSlider10) {

			// TODO setCoefficicentCount(10);
			// Slider einblenden
			slider6.setVisible(true);
			slider7.setVisible(true);
			slider8.setVisible(true);
			slider9.setVisible(true);
			slider10.setVisible(true);
			// Platz reservieren
			slider6.setManaged(true);
			slider7.setManaged(true);
			slider8.setManaged(true);
			slider9.setManaged(true);
			slider10.setManaged(true);
		}
	}

	/**
	 * Wird bei Auswahl der Buffer Größe aufgerufen.
	 */
	public void handleMenuBuffer() {
		Toggle selectedToggle = toggleGroupBuffer.getSelectedToggle();

		if(selectedToggle == radioMenuItemBuffer1024) {
			// TODO setBufferSize(1024);
		}
		else if(selectedToggle == radioMenuItemBuffer4096) {
			// TODO setBufferSize(4096);
		}
		else if(selectedToggle == radioMenuItemBuffer8192) {
			// TODO setBufferSize(8192);
		}
		else if(selectedToggle == radioMenuItemBuffer16384) {
			// TODO setBufferSize(16384);
		}
	}

	/**
	 * Setzt die Verstärkung für alle Slider auf den gewünschten Wert.
	 * @param gain Die gewählte Verstärkung.
	 */
	private void setSliderGain(int gain) {
		// Anzeige am Slider
		labeldBTopLeft.setText(gain + " dB");
		labeldBBottomLeft.setText("-" + gain + " dB");
		labeldBTopRight.setText(gain + " dB");
		labeldBBottomRight.setText("-" + gain + " dB");
		// Slider Werte
		slider1.setMin(-gain);
		slider1.setMax(gain);
		slider1.setBlockIncrement(gain/10);
		slider1.setMajorTickUnit(gain);

		slider2.setMin(-gain);
		slider2.setMax(gain);
		slider2.setBlockIncrement(gain/10);
		slider2.setMajorTickUnit(gain);

		slider3.setMin(-gain);
		slider3.setMax(gain);
		slider3.setBlockIncrement(gain/10);
		slider3.setMajorTickUnit(gain);

		slider4.setMin(-gain);
		slider4.setMax(gain);
		slider4.setBlockIncrement(gain/10);
		slider4.setMajorTickUnit(gain);

		slider5.setMin(-gain);
		slider5.setMax(gain);
		slider5.setBlockIncrement(gain/10);
		slider5.setMajorTickUnit(gain);

		slider6.setMin(-gain);
		slider6.setMax(gain);
		slider6.setBlockIncrement(gain/10);
		slider6.setMajorTickUnit(gain);

		slider7.setMin(-gain);
		slider7.setMax(gain);
		slider7.setBlockIncrement(gain/10);
		slider7.setMajorTickUnit(gain);

		slider8.setMin(-gain);
		slider8.setMax(gain);
		slider8.setBlockIncrement(gain/10);
		slider8.setMajorTickUnit(gain);

		slider9.setMin(-gain);
		slider9.setMax(gain);
		slider9.setBlockIncrement(gain/10);
		slider9.setMajorTickUnit(gain);

		slider10.setMin(-gain);
		slider10.setMax(gain);
		slider10.setBlockIncrement(gain/10);
		slider10.setMajorTickUnit(gain);
	}

	/*
	 * Media Player Handling
	 */

	/**
	 * Wird bei Klick auf den Play Button aufgerufen.
	 */
	public void handleButtonPlay() {
		if (buttonPlay.getText().equals("Abspielen")) {

			// Dateiendung überprüfen
			boolean isWav = false;
			if (soundProcessor.getFileExtension(file).equals("wav")
					|| soundProcessor.getFileExtension(file).equals("WAV")) {
				isWav = true;
			}

			// falls Datei korrekt
			if (file.exists() && isWav) {
				// spiele die Datei in neuem Service ab
				soundService = new SoundService(file);
				soundService.setSoundProcessor(soundProcessor);
				soundService.setOnSucceeded(e -> {
					toggleUIAtStop();
				});
				soundService.setOnCancelled(e -> {
					toggleUIAtStop();
				});
				soundService.start();
				toggleUIAtPlay();
			} else {
				// Fehlermeldungen
				if (file == null) {
					showError("Abspielen nicht möglich!", "Keine Datei angegeben.", "");
				} else if (file.exists() && !isWav) {
					showError("Abspielen nicht möglich!", "Datei ist kein Wavesound:", file.getAbsolutePath());
				} else {
					showError("Abspielen nicht möglich!", "Datei konnte nicht gefunden werden:",
							file.getAbsolutePath());
				}
			}
		} else {
			// bei Klick auf Stop
			soundService.cancel();
		}

	}

	/**
	 * Layout Änderungen wenn Audio abspielt
	 */
	private void toggleUIAtPlay() {
		buttonPlay.setText("Stop");
		menuSlider.setDisable(true);
		menuBuffer.setDisable(true);
	}

	/**
	 * Layout Änderungen wenn kein Audio abspielt
	 */
	private void toggleUIAtStop() {
		buttonPlay.setText("Abspielen");
		menuSlider.setDisable(false);
		menuBuffer.setDisable(false);
	}

	/**
	 * Konfiguriert den Öffnen Dialog.
	 *
	 * @param chooser
	 *            Der Öffnen Dialog.
	 * @param title
	 *            Der Titel des Dialog Fenster.
	 */
	private void configureFileChooser(final FileChooser chooser, final String title) {
		chooser.setTitle(title);
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("WAV", "*.wav"));
	}

	/**
	 * Zeigt einen Fehler Dialog an.
	 *
	 * @param headerText
	 *            Überschrift
	 * @param contentText
	 *            Text der Fehlermeldung
	 * @param filePath
	 *            Pfad zur Datei
	 */
	private void showError(String headerText, String contentText, String filePath) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Fehler");
		alert.setHeaderText(headerText);
		alert.setContentText(contentText + "\n" + filePath);

		alert.showAndWait();
	}

	/*
	 * Diverses
	 */

	/**
	 * Liefert den aktuellen Wert des gewünschten Sliders
	 *
	 * @param sliderNumber
	 *            Die Nummer des Sliders (1-10).
	 * @return Der aktuelle Wert des Sliders.
	 */
	public double getSliderValue(int sliderNumber) {
		switch (sliderNumber) {
		case 1:
			return slider1.getValue();
		case 2:
			return slider2.getValue();
		case 3:
			return slider3.getValue();
		case 4:
			return slider4.getValue();
		case 5:
			return slider5.getValue();
		case 6:
			return slider6.getValue();
		case 7:
			return slider7.getValue();
		case 8:
			return slider8.getValue();
		case 9:
			return slider9.getValue();
		case 10:
			return slider10.getValue();
		default:
			return 0;
		}
	}

	/**
	 * Zeigt an, ob Clipping vorliegt.
	 * @param clipping Wahrheitswert für das Clipping.
	 */
	public void setClipping(boolean clipping) {
		if(clipping) {
			System.out.println("Clipping!");
			//labelClipping.setText("Clipping!");
		}
		else {
			//labelClipping.setText("");
		}
	}
}
