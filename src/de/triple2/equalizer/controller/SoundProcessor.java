package de.triple2.equalizer.controller;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.triple2.equalizer.model.WavFile;
import de.triple2.equalizer.view.LayoutController;

public class SoundProcessor {

	// abzuspielende Audio Line
	private SourceDataLine soundLine = null;
	// Array mit Slider Werten
	private double bandDbCoefficients[] = new double[10];
	// der LayoutController des Programms
	private LayoutController layout;

	/**
	 * Konstruktor
	 * @param layout Der LayoutController des Programms.
	 */
	public SoundProcessor(LayoutController layout) {
		this.layout = layout;
	}

	/**
	 * @deprecated Liest eine Audio Datei ein, übergibt sie dem Equalizer und
	 *             spielt sie ab.
	 * @param soundFile
	 *            Die Audiodatei.
	 */
	public void playSoundOld(File soundFile) {

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
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Beendet das Abspielen der Datei.
	 */
	public void stopSound() {
		soundLine.stop();
	}

	/**
	 * Liefert die Länge einer Audio Datei in Sekunden.
	 *
	 * @param soundFile
	 *            Die Audiodatei.
	 * @return Die Länge in Sekunden.
	 */
	public int getLength(File soundFile) {
		float durationInSeconds = 0;
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			AudioFormat format = audioInputStream.getFormat();
			long audioFileLength = soundFile.length();
			int frameSize = format.getFrameSize();
			float frameRate = format.getFrameRate();
			durationInSeconds = (audioFileLength / (frameSize * frameRate));
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		// runde auf zwei Nachkommastellen
		return Math.round(durationInSeconds);
	}

	/**
	 * Liefert die Dateiendung einer Datei.
	 *
	 * @param file
	 *            Die zu überprüfende Datei.
	 * @return Die Dateiendung.
	 */
	public String getFileExtension(File file) {
		String fileName = file.getName();

		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		} else {
			return "";
		}
	}

	public void playSound(File audioFile) {

		int numberOfEqBands = 10;

		try {
			WavFile wavFile = WavFile.openWavFile(audioFile);

			wavFile.display();
			wavFile.getValidBits();
			System.out.println("----");

			///////////////////////////////////////////////////////

			// Audio-Format auslesen
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
			// Erstellung der abzuspielenden Audio Line
			AudioFormat audioFormat = audioInputStream.getFormat();

			int bytesPerFrame = audioFormat.getFrameSize();

			int framesInBuffer = 1024;
			int numBytes = framesInBuffer * bytesPerFrame; // Puffergröße

			boolean isMono = (audioFormat.getChannels() == 1) ? true : false;
			boolean isBigEndian = audioFormat.isBigEndian();

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

			///////////////////////////////////////////////////////////////

			int bufferSize = framesInBuffer; // sollte Zweierpotenz sein

			double[] sampleBufferRealStereo = new double[2 * bufferSize];

			double[] sampleBufferRealLeft = new double[bufferSize];
			double[] sampleBufferRealRight = new double[bufferSize];
			double[] sampleBufferImag = new double[bufferSize];

			// liest 100 Samples, schreibt sie in sampleBufferReal rein

			// EQ initialisieren
			double constantValue = 0;
			// double bandDbCoefficients[] =
			// {constantValue,constantValue,constantValue,constantValue,constantValue,
			// constantValue,constantValue,constantValue,constantValue,constantValue};

			// double bandDbCoefficients[] = {0,-5,0,0,0,0,0,0,0,7,0};
			Equalizer eq = new Equalizer((int) wavFile.getSampleRate(), numberOfEqBands, bufferSize);
			FFT fft = new FFT(bufferSize);

			int framesRead = -1;
			// Unterscheidung Stereo/Mono
			if (!isMono) {
				while (framesRead != 0) {
					framesRead = wavFile.readFrames(sampleBufferRealStereo, bufferSize);

					// wavFile.readFrames(intBuffer,bufferSize);

					// Aufteilen in links/rechts
					for (int i = 0; i < 2 * bufferSize; i++) {
						if (i % 2 == 0) {
							sampleBufferRealLeft[i / 2] = sampleBufferRealStereo[i];
						} else {
							sampleBufferRealRight[(int) (Math.floor(i / 2))] = sampleBufferRealStereo[i];
						}
					}

					// fülle Array mit aktuellen Slider Werten
					setCurrentSliderValues();

					eq.applyEQ(bandDbCoefficients, sampleBufferRealLeft, fft);
					eq.applyEQ(bandDbCoefficients, sampleBufferRealRight, fft);

					// byte-array beschreiben

					byte[] audioBytes = new byte[framesRead * bytesPerFrame];
					short leftSample;
					short rightSample;
					for (int i = 0; i < framesRead; i++) {
						leftSample = convertSampleTo16BitShort(sampleBufferRealLeft[i]);

						if (isBigEndian) {
							// Alter Schwede, ich hätte nicht gedacht, dass ich
							// mich hierzu auch mit Bitshifting
							// und Byte-Codierungen in Little/Big-Endian
							// befassen muss...

							// die Samples werden in 2 Bytes codiert, wobei bei
							// Big Endian das höchstwertige Byte ganz links
							// steht,
							// bei Little Endian steht das höchstwertige Byte
							// ganz rechts

							leftSample = convertSampleTo16BitShort(sampleBufferRealLeft[i]);
							audioBytes[4 * i] = (byte) ((leftSample & 0xFF00) >> 8);
							audioBytes[4 * i + 1] = (byte) leftSample;

							rightSample = convertSampleTo16BitShort(sampleBufferRealRight[i]);
							audioBytes[4 * i + 2] = (byte) ((rightSample & 0xFF00) >> 8);
							audioBytes[4 * i + 3] = (byte) rightSample;

						} else {
							// bei Little-Endian müssen immer zwei Bytes
							// vertauscht werden
							// (aber nicht die darin enthaltenen Bits!)
							leftSample = convertSampleTo16BitShort(sampleBufferRealLeft[i]);
							audioBytes[4 * i + 1] = (byte) ((leftSample & 0xFF00) >> 8);
							audioBytes[4 * i] = (byte) leftSample;

							rightSample = convertSampleTo16BitShort(sampleBufferRealRight[i]);
							audioBytes[4 * i + 3] = (byte) ((rightSample & 0xFF00) >> 8);
							audioBytes[4 * i + 2] = (byte) rightSample;
						}
					}

					soundLine.write(audioBytes, 0, audioBytes.length);
				}
			} else {
				while (framesRead != 0) {
					framesRead = wavFile.readFrames(sampleBufferRealLeft, bufferSize);

					// fülle Array mit aktuellen Slider Werten
					setCurrentSliderValues();

					eq.applyEQ(bandDbCoefficients, sampleBufferRealLeft, fft);

					byte[] audioBytes = new byte[framesRead * bytesPerFrame];
					short sample;
					for (int i = 0; i < framesRead; i++) {
						sample = convertSampleTo16BitShort(sampleBufferRealLeft[i]);
						if (isBigEndian) {
							audioBytes[2 * i] = (byte) ((sample & 0xFF00) >> 8);
							audioBytes[2 * i + 1] = (byte) sample;
						} else {
							audioBytes[2 * i + 1] = (byte) ((sample & 0xFF00) >> 8);
							audioBytes[2 * i] = (byte) sample;
						}
					}
					soundLine.write(audioBytes, 0, audioBytes.length);
				}
			}

			soundLine.stop();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private short convertSampleTo16BitShort(double d) {
		if (d > 1) {
			d = 1;
			layout.setClipping(true);
		}
		else if (d < -1) {
			d = -1;
			layout.setClipping(true);
		}
		else {
			layout.setClipping(false);
		}
		return (short) ((Short.MAX_VALUE - Short.MIN_VALUE) * (d + 1) / 2 + Short.MIN_VALUE);
	}

	/**
	 * Passt die dem Equalizer übergebenen Array Werte
	 * an die vom Nutzer gewählten an.
	 */
	private void setCurrentSliderValues() {

		for (int i = 0; i < bandDbCoefficients.length; i++) {
			bandDbCoefficients[i] = layout.getSliderValue(i+1);
		}
	}
}
