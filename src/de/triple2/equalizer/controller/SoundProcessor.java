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

    // abzuspielende Audio-Line
    private SourceDataLine soundLine = null;
    // Array mit Slider-Werten
    private double bandDbCoefficients[];
    // der LayoutController des Programms
    private final LayoutController layout;

	// Anzahl der Frames, die in Buffer aufzunehmen sind
    private int framesInBuffer = 8192;

	// Anzahl der EQ B�nder
    private int numberOfEqBands = 10;

	//erm�glicht Einlesen von Audiodateien
    private AudioInputStream audioInputStream = null;

	// Klasse zum Zugriff auf die Samples
    private WavFile wavFile = null;

	// die Frequenzen, die in der UI f�r jedes EQ-Band anzuzeigen sind
	private int[] frequenciesToShow;

	private Equalizer eqL;
	private Equalizer eqR;

	private double[] sampleBufferRealStereo;
	private int bufferSize;
	private double[] sampleBufferRealLeft;
	private double[] sampleBufferRealRight;
	private int bytesPerFrame;
	private boolean isMono;
	private boolean isBigEndian;

	/**
	 * Konstruktor
	 *
	 * @param layout
	 *            Der LayoutController des Programms.
	 */
	public SoundProcessor(LayoutController layout) {
		this.layout = layout;
		eqL = new Equalizer();
		eqR = new Equalizer();

	}

	/**
	 * Wird aufgerufen, sobald eine Datei eingelesen werden soll.
	 * @param audioFile die eingelesene Datei.
	 */
	public void initializeEqualizer(File audioFile) {
		try {
			// Array initialisieren
			bandDbCoefficients = new double[numberOfEqBands];
			wavFile = WavFile.openWavFile(audioFile);

			//Info zur Wave-Datei ausgeben
            //wavFile.display();
            //wavFile.getValidBits();
            //System.out.println("----");

			///////////////////////////////////////////////////////

			// Audio-Format auslesen
			audioInputStream = AudioSystem.getAudioInputStream(audioFile);
			// Erstellung der abzuspielenden Audio Line
			AudioFormat audioFormat = audioInputStream.getFormat();

			bytesPerFrame = audioFormat.getFrameSize();

			// Zeitl�nge eines Buffers: 2048/fs
			// int framesInBuffer = 8192;

			// numBytes = framesInBuffer * bytesPerFrame; // Puffergr��e in Byte

			isMono = (audioFormat.getChannels() == 1) ? true : false;
			//Byte-Kodierung der Samples ist auf 2 Varianten m�glich
            isBigEndian = audioFormat.isBigEndian();

			//Info �ber das Audioformat (wird f�r Ausgabe ben�tigt)
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            soundLine = (SourceDataLine) AudioSystem.getLine(info);

            // �ffnen der Line
            soundLine.open(audioFormat);
            // Starten der Line
            // Passiert erst, wennaudioBytes Array bef�llt wurde s.u.
            soundLine.start();

            // Einige Audio Dateien haben keine festgelegte Frame Gr��e.
            if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
                // Dann wird feste Anzahl an Bytes eingelesen.
                bytesPerFrame = 1;
            }

			///////////////////////////////////////////////////////////////

			bufferSize = framesInBuffer; // sollte Zweierpotenz sein

			// Buffer f�r verschiedene Arten
			sampleBufferRealStereo = new double[2 * bufferSize];
			sampleBufferRealLeft = new double[bufferSize];
			sampleBufferRealRight = new double[bufferSize];
			// sampleBufferImag = new double[bufferSize];

			// liest 100 Samples, schreibt sie in sampleBufferReal rein

			// EQ initialisieren
			// constantValue = 0;
			// double bandDbCoefficients[] =
			// {constantValue,constantValue,constantValue,constantValue,constantValue,
			// constantValue,constantValue,constantValue,constantValue,constantValue};

			// double bandDbCoefficients[] = {0,-5,0,0,0,0,0,0,0,7,0};

			int sr = (int) wavFile.getSampleRate();

			frequenciesToShow = eqL.initializeBands(sr, numberOfEqBands);
			eqL.calculateBandIndices(sr, bufferSize);
			frequenciesToShow = eqR.initializeBands(sr, numberOfEqBands);
			eqR.calculateBandIndices(sr, bufferSize);

        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (audioInputStream != null) {
                try {
                    audioInputStream.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

	/**
	 * �bergibt eine Audio-Datei dem Equalizer und spielt sie ab.
	 * @param audioFile Die Audiodatei.
	 */
	public void playSound(File audioFile) {

		try {
			FFT fft = new FFT(bufferSize);

			//Anzahl gelesener Frames
			int framesRead = -1;
			// Unterscheidung Stereo/Mono
			if (!isMono) {
				while (framesRead != 0) {
					// Frames einlesen
                    framesRead = wavFile.readFrames(sampleBufferRealStereo, bufferSize);

                    // Aufteilen in links/rechts (bei Stereo)
                    for (int i = 0; i < 2 * bufferSize; i++) {
                        if (i % 2 == 0) {
                            sampleBufferRealLeft[i / 2] = sampleBufferRealStereo[i];
                        } else {
                            sampleBufferRealRight[(int) (Math.floor(i / 2))] =
                                    sampleBufferRealStereo[i];
                        }
                    }

					// f�lle Array mit aktuellen Slider Werten
					setCurrentSliderValues();

					// Equalizer anwenden (�bergebenes Array im 2. Argument wird modifiziert)
					eqL.applyEQ(bandDbCoefficients, sampleBufferRealLeft, fft);
					eqR.applyEQ(bandDbCoefficients, sampleBufferRealRight, fft);

                    // Byte-Array initialisieren, das am Ende ausgegeben werden soll
					byte[] audioBytes = new byte[framesRead * bytesPerFrame];

					// einzelne Samples f�r links/rechts, die dann in Bytekanal zu schreiben
					short leftSample;
                    short rightSample;

                 // Schreiben aller ver�nderten Samples in audioBytes
					for (int i = 0; i < framesRead; i++) {

						leftSample = convertSampleTo16BitShort(sampleBufferRealLeft[i]);

						//Jetzt m�ssen Bytevertauschungen anhand der Endianness vorgenommen
						//werden. Wenn man das nicht macht, kommt einfach nur Rauschen raus.
						//(das als Ursache zu finden hat wirklich lange gedauert, das
						//kann ich versichern...)
						if (isBigEndian) {
							// Alter Schwede, ich h�tte nicht gedacht, dass ich
							// mich hierzu auch mit Bitshifting
							// und Byte-Codierungen in Little/Big-Endian
							// befassen muss...

							// die Samples werden in 2 Bytes codiert, wobei bei
							// Big Endian das h�chstwertige Byte ganz links
							// steht,
							// bei Little Endian steht das h�chstwertige Byte
							// ganz rechts

							leftSample = convertSampleTo16BitShort(sampleBufferRealLeft[i]);
							audioBytes[4 * i] = (byte) ((leftSample & 0xFF00) >> 8);
							audioBytes[4 * i + 1] = (byte) leftSample;

							rightSample = convertSampleTo16BitShort(sampleBufferRealRight[i]);
							audioBytes[4 * i + 2] = (byte) ((rightSample & 0xFF00) >> 8);
							audioBytes[4 * i + 3] = (byte) rightSample;

						} else {
							// bei Little-Endian m�ssen immer zwei Bytes
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
					//Ausgabe des der in audioBytes befindlichen Daten
					soundLine.write(audioBytes, 0, audioBytes.length);
				}
			} else {
				while (framesRead != 0) {
					// Frames einlesen
					framesRead = wavFile.readFrames(sampleBufferRealLeft, bufferSize);

					// f�lle Array mit aktuellen Slider Werten
					setCurrentSliderValues();

					//Equalizer anwenden (�bergebenes Array im 2. Argument wird modifiziert)
					eqL.applyEQ(bandDbCoefficients, sampleBufferRealLeft, fft);

					byte[] audioBytes = new byte[framesRead * bytesPerFrame];
					short sample;

					// Schreiben aller ver�nderten Samples in audioBytes
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
					//Ausgabe des der in audioBytes befindlichen Daten
					soundLine.write(audioBytes, 0, audioBytes.length);
				}
			}
		    //soundLine und Dateistrom schlie�en
            soundLine.stop();
            wavFile.close();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (audioInputStream != null) {
                try {
                    audioInputStream.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

	/**
	* F�hrt eine Konvertierung eines double-Wertes zwischen -1 und +1
	* zu einem 16-bit-short aus. Wenn der double-Wert au�erhalb dieser Grenzen liegt,
	* tritt Clipping auf und das Signal wird auf +/- 1 gesetzt.
	*
	* Bei der Konvertierung erfolgt au�erdem eine Skalierung in das
	* Intervall zwischen dem kleinstm�glichen und gr��tm�glichen Wert, den eine
	* 16-bit-Zahl annehmen kann. Dies wird ben�tigt, um die 16 Bit sp�ter in
	* zwei 8-Bit-Bl�cke aufzuteilen.
	* @param d Zu konvertierender double-Wert
	* @return Eine short-Variable zwischen Short.MIN_VALUE und Short.MAX_VALUE
	*/
	private short convertSampleTo16BitShort(double d) {
		if (d > 1) {
			d = 1;
			layout.setClipping(true);
		} else if (d < -1) {
			d = -1;
			layout.setClipping(true);
		} else {
			layout.setClipping(false);
		}
		return (short) ((Short.MAX_VALUE - Short.MIN_VALUE) * (d + 1) / 2 + Short.MIN_VALUE);
	}

    /**
     * Beendet das Abspielen der Datei.
     */
    public void stopSound() {
        soundLine.stop();
        soundLine.drain();
        soundLine.close();
        if (audioInputStream != null) {
            try {
                audioInputStream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        } else if (wavFile != null) {
            try {
                wavFile.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

	/**
	* Getter f�r die Anzahl der Frames im Buffer.
	* @return Gr��e des Buffers in Frames.
	*/
	public int getFramesInBuffer() {
		return framesInBuffer;
	}

    /**
     * Setter f�r die Anzahl der Frames im Buffer.
     * Achtung!
     * Danach muss der Equalizer neu eingestellt werden, also z.B. playSound() aufgerufen werden.
	 *
     * @param framesInBuffer Gr��e des Buffers in Frames.
     */
	public void setFramesInBuffer(int framesInBuffer) {
		this.framesInBuffer = framesInBuffer;
	}

	/**
	* Getter f�r die Anzahl der genutzten EQ-B�nder.
	* @return Anzahl der B�nder.
	*/
	public int getNumberOfEqBands() {
		return numberOfEqBands;
	}

    /**
     * Setter f�r die Anzahl der genutzten EQ-B�nder.
     * Achtung!
     * Danach muss der Equalizer neu eingestellt werden, also z.B. playSound() aufgerufen werden.
	 *
     * @param numberOfEqBands Anzahl der B�nder.
     */
	public void setNumberOfEqBands(int numberOfEqBands) {
		int sr = (int) wavFile.getSampleRate();

		frequenciesToShow = eqL.initializeBands(sr, numberOfEqBands);
		frequenciesToShow = eqR.initializeBands(sr, numberOfEqBands);
		this.numberOfEqBands = numberOfEqBands;
	}

	/**
	 * Liefert die L�nge einer Audio Datei in Sekunden.
	 *
	 * @param soundFile
	 *            Die Audiodatei.
	 * @return Die L�nge in Sekunden.
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
	 *            Die zu �berpr�fende Datei.
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

	/**
	 * Passt die dem Equalizer �bergebenen Array Werte an die vom Nutzer gew�hlten an.
	 */
	private void setCurrentSliderValues() {

		for (int i = 0; i < bandDbCoefficients.length; i++) {
			bandDbCoefficients[i] = layout.getSliderValue(i + 1);
		}
	}

	/**
	 * Ermittelt die vom Equalizer berechneten Frequenzen der einzelnen B�nder.
	 * @return Integer-Array mit den Frequenzen.
	 */
	public int[] getFrequencies() {
		return frequenciesToShow;
	}
}
