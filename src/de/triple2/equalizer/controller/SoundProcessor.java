package de.triple2.equalizer.controller;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class SoundProcessor {
	
	// abzuspielende Audio Line
	private SourceDataLine soundLine = null;

	public void playSound(File soundFile) {
		
		try {
			// Input Stream
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);

			int bytesPerFrame = audioInputStream.getFormat().getFrameSize();

			// Erstellung der abzuspielenden Audio Line
			AudioFormat audioFormat = audioInputStream.getFormat();
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
			soundLine = (SourceDataLine) AudioSystem.getLine(info);
			// Öffnen der Line
			soundLine.open(audioFormat);
			// Starten der Line
			// Passiert erst, wennaudioBytes Array befüllt wurde s.u.
			soundLine.start();

			// Einige Audio Dateien haben keine festgelegte Frame Größe.
			if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
				// Dann wird feste Anzahl an Bytes eingelesen.
				bytesPerFrame = 1;
			}

			// Byte Array der Größe von 1024 Frames.
			// Zum sukzessiven speichern von Chunks aus dem Input Stream.
			int numBytes = 1024 * bytesPerFrame;
			byte[] audioBytes = new byte[numBytes];

			try {
				int totalFramesRead = 0;
				int numBytesRead = 0;
				int numFramesRead = 0;

				// Lese numBytes an Bytes der Datei ein.
				// -1, falls keine Daten mehr zum Lesen vorhanden sind.
				while (numBytesRead != -1) {
					numBytesRead = audioInputStream.read(audioBytes);
					// Berechne die Anzahl an Frames, die schon gelesen wurden.
					numFramesRead = numBytesRead / bytesPerFrame;
					totalFramesRead += numFramesRead;

					// Veränderung der Audio Daten im Audio Bytes Array
					// TODO !!! EQUALIZER !!!

					// TODO evtl. neues Array aus den Daten vom Equalizer machen
					// und das dann in die SoundLine schreiben...
					if (numBytesRead > 0) {
						// Schreibt die Audio Daten zum Abspielen in den Mixer.
						soundLine.write(audioBytes, 0, numBytesRead);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void stopSound() {
		soundLine.stop();
	}
	
}
