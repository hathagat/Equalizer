package de.triple2.equalizer.controller;

import java.io.File;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/** Diese Klasse stellt einen Service bereit, um die ausgewählt Musik im Hintergrund abspielen zu können, ohne das dabei
 * das graphische Interface blockiert wird. */
public class EqualizerService extends Service<SoundProcessor> {
	private File music = null;
	private SoundProcessor soundProcessor;

	// Konstruktor, dem die abzuspielende Datei übergeben werden muss.
	public EqualizerService(File file){
		music = file;
	}

	/** Setter Methode für den SoundProcessor, der für den Thread verwendet wird. */
	public final void setSoundProcessor(SoundProcessor newProcessor) {
		soundProcessor = newProcessor;
	}

	/** Getter Methode, die den verwendeten SoundProcessor liefert. */
	public final SoundProcessor getSoundProcessor() {
		return soundProcessor;
	}


	@Override
	protected Task createTask() {
		final SoundProcessor soundProcessor = getSoundProcessor();
		return new Task<SoundProcessor>(){
			@Override
			protected SoundProcessor call() throws Exception {
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
