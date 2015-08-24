package de.triple2.equalizer.view;

import java.io.File;

import de.triple2.equalizer.controller.SoundProcessor;
import de.triple2.equalizer.controller.SoundService;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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
	public GridPane gridPaneSlider;
	public Label labeldBTopLeft, labeldBBottomLeft, labeldBTopRight, labeldBBottomRight;
	public Slider slider1, slider2, slider3, slider4, slider5, slider6, slider7, slider8, slider9, slider10;
	public Label labelSlider1, labelSlider2, labelSlider3, labelSlider4, labelSlider5, labelSlider6, labelSlider7, labelSlider8, labelSlider9, labelSlider10;
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

			soundProcessor.initializeEqualizer(file);
			// Zeige die Frequenzen der Bänder an.
			changeFrequencies(-1);
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
	 * Wird bei Klick auf Über aufgerufen.
	 */
	public void handleMenuAbout() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(Main.getStage());
		alert.setTitle("Über");
		alert.setHeaderText(null);
		alert.setContentText("triple2 Equalizer \n\n"
				+ "Version: 1.0 \n\n"
				+ "Diese Anwendung wurde im Rahmen des Moduls Multimediale Signalverarbeitung "
				+ "an der Hochschule Anhalt entwickelt. \n\n"
				+ "(c) triple2 2015");
		alert.showAndWait();
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
		Slider[] sliders = {slider1, slider2, slider3, slider4, slider5, slider6, slider7, slider8, slider9, slider10};
		Toggle selectedToggle = toggleGroupSlider.getSelectedToggle();
		ObservableList<ColumnConstraints> columnConstraints = gridPaneSlider.getColumnConstraints();

		// Bänder konfigurieren
		if(selectedToggle == radioMenuItemSlider5) {
			// Frequenzen ändern
			changeFrequencies(5);

			// Spaltenbreite anpassen
			for(int i=1; i<columnConstraints.size()-1; i++) {
				if(i>=6) {
					columnConstraints.get(i).setHgrow(Priority.NEVER);
				}
			}

			// Label ausblenden
			labelSlider6.setVisible(false);
			labelSlider7.setVisible(false);
			labelSlider8.setVisible(false);
			labelSlider9.setVisible(false);
			labelSlider10.setVisible(false);
			// reservierten Platz frei machen
			labelSlider6.setManaged(false);
			labelSlider7.setManaged(false);
			labelSlider8.setManaged(false);
			labelSlider9.setManaged(false);
			labelSlider10.setManaged(false);

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
		}
		else if(selectedToggle == radioMenuItemSlider7) {
			// Frequenzen ändern
			changeFrequencies(7);

			// Spaltenbreite anpassen
			for(int i=1; i<columnConstraints.size()-1; i++) {
				if(i>=6 && i<=7) {
					columnConstraints.get(i).setHgrow(Priority.SOMETIMES);
				}
				else if(i>=8) {
					columnConstraints.get(i).setHgrow(Priority.NEVER);
				}
			}

			// Label einblenden
			labelSlider6.setVisible(true);
			labelSlider7.setVisible(true);
			// Platz reservieren
			labelSlider6.setManaged(true);
			labelSlider7.setManaged(true);
			// Label ausblenden
			labelSlider8.setVisible(false);
			labelSlider9.setVisible(false);
			labelSlider10.setVisible(false);
			// reservierten Platz frei machen
			labelSlider8.setManaged(false);
			labelSlider9.setManaged(false);
			labelSlider10.setManaged(false);

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
		}
		else if(selectedToggle == radioMenuItemSlider10) {
			// Frequenzen ändern
			changeFrequencies(10);

			// Spaltenbreite anpassen
			for(int i=1; i<columnConstraints.size()-1; i++) {
				if(i>=6) {
					columnConstraints.get(i).setHgrow(Priority.SOMETIMES);
				}
			}

			// Label einblenden
			labelSlider6.setVisible(true);
			labelSlider7.setVisible(true);
			labelSlider8.setVisible(true);
			labelSlider9.setVisible(true);
			labelSlider10.setVisible(true);
			// Platz reservieren
			labelSlider6.setManaged(true);
			labelSlider7.setManaged(true);
			labelSlider8.setManaged(true);
			labelSlider9.setManaged(true);
			labelSlider10.setManaged(true);

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
		// eingestellte Werte zurücksetzen
		for(Slider s : sliders) {
			s.setValue(0);
		}
	}

	private void changeFrequencies(int bandCount) {
		// falls Datei vorhanden
		if(file!=null) {
			// falls sinnvolle Anzahl Bänder übergeben.
			if(bandCount != -1) {
				soundProcessor.setNumberOfEqBands(bandCount);
			}

			Label[] labels = {labelSlider1, labelSlider2, labelSlider3, labelSlider4, labelSlider5, labelSlider6, labelSlider7, labelSlider8, labelSlider9, labelSlider10};
			int[] frequencies = soundProcessor.getFrequencies();

			for(int i=0; i<frequencies.length; i++) {
				labels[i].setText(Integer.toString(frequencies[i]) + " Hz");
			}
		}
	}

	/**
	 * Wird bei Auswahl der Buffer Größe aufgerufen.
	 */
	public void handleMenuBuffer() {
		Toggle selectedToggle = toggleGroupBuffer.getSelectedToggle();

		if(selectedToggle == radioMenuItemBuffer1024) {
			soundProcessor.setFramesInBuffer(1024);
		}
		else if(selectedToggle == radioMenuItemBuffer4096) {
			soundProcessor.setFramesInBuffer(4096);
		}
		else if(selectedToggle == radioMenuItemBuffer8192) {
			soundProcessor.setFramesInBuffer(8192);
		}
		else if(selectedToggle == radioMenuItemBuffer16384) {
			soundProcessor.setFramesInBuffer(16384);
		}
	}

	/**
	 * Setzt die Verstärkung für alle Slider auf den gewünschten Wert.
	 * @param gain Die gewählte Verstärkung.
	 */
	private void setSliderGain(int gain) {
		Slider[] sliders = {slider1, slider2, slider3, slider4, slider5, slider6, slider7, slider8, slider9, slider10};
		// Anzeige am Slider
		labeldBTopLeft.setText(gain + " dB");
		labeldBBottomLeft.setText("-" + gain + " dB");
		labeldBTopRight.setText(gain + " dB");
		labeldBBottomRight.setText("-" + gain + " dB");
		// Slider Werte
		for(Slider s : sliders) {
			s.setMin(-gain);
			s.setMax(gain);
			s.setBlockIncrement(gain/10);
			s.setMajorTickUnit(gain);
		}
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
			// TODO labelClipping.setText("Clipping!");
		}
		else {
			//labelClipping.setText("");
		}
	}
}
