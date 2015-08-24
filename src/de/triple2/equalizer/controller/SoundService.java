package de.triple2.equalizer.controller;

import java.io.File;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/** Diese Klasse stellt einen Service bereit, um die ausgew�hlte Musik im Hintergrund abspielen zu k�nnen, ohne das dabei
 * das graphische Interface blockiert wird. */
public class SoundService extends Service<SoundProcessor> {
	private File music = null;
	private SoundProcessor soundProcessor;

	// Konstruktor, dem die abzuspielende Datei �bergeben werden muss.
	public SoundService(File file) {
		music = file;
	}

	/** Setter Methode f�r den SoundProcessor, der f�r den Thread verwendet wird. */
	public final void setSoundProcessor(SoundProcessor newProcessor) {
		soundProcessor = newProcessor;
	}

	/** Getter Methode, die den verwendeten SoundProcessor liefert. */
	public final SoundProcessor getSoundProcessor() {
		return soundProcessor;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Task createTask() {
		final SoundProcessor soundProcessor = getSoundProcessor();
		return new Task<SoundProcessor>(){
			@Override
			protected SoundProcessor call() throws Exception {
				soundProcessor.initializeEqualizer(music);
				soundProcessor.playSound(music);
				return soundProcessor;
			}
		};
	}

	@Override
	public boolean cancel() {
		soundProcessor.stopSound();
		return super.cancel();
	}
}
