package de.triple2.equalizer.controller;

import java.lang.reflect.Array;
import java.util.Arrays;

public class Equalizer {

	private int highestFrequency = 0;
	private int lowestFrequency = 20; //20 als vom Menschen hörbare, niedrigste Frequenz
	private int numberOfBands = 0;
	private double[] logValues;
	private double[] bandFrequencies;
	private int[] frequenciesToShow;
	private int[] bandIndices = null;

	private int sampleRate;
	private int bufferSize = 0;

	/*
	 * 3 Typen von Glättung:
	 * 0 - keine Glättung
	 * 1 - Glättung der an das Frequenzspektrum zu multiplizierenden Funktion
	 * 2 - Glättung des Ergebnissignal
	 * 3 - Kombination aus 1 und 2
	 *
	 * 1 benutzt lineare Interpolation, um die Faktorfunktion zu glätten
	 * 2 benutzt gleitenden Mittelwert zur Glättung
	 *
	 * smoothnessPoints gibt bei Option 2/3 an, wie weit jeweils nach links/rechts zu schauen ist
	 */

	private int smoothnessType = 1;
	private int smoothnessPoints = 1;

	private int lastBufferMemorySize = 3;
	private double lastBuffer[] = null;

	public Equalizer(int argSampleRate, int argNumberOfBands, int argBufferSize){
		sampleRate = argSampleRate;

		highestFrequency = sampleRate/2;
		numberOfBands = argNumberOfBands;

		logValues = new double[numberOfBands+1];

		//Ober- und Untergrenzen (log-Werte bestimmen)
		logValues[0] = Math.log10(lowestFrequency);
		logValues[numberOfBands] = Math.log10(highestFrequency);

		double frequencySpan = logValues[numberOfBands]-logValues[0];

		for (int i=1; i<numberOfBands; i++){
			logValues[i] = logValues[0] + (i/(double)numberOfBands) * frequencySpan;

		}

		// Grenzfrequenzen der einzelnen Bänder bestimmen
		bandFrequencies = new double[numberOfBands+1];
		frequenciesToShow = new int[numberOfBands];
		for (int i=0; i<numberOfBands+1; i++){
			bandFrequencies[i] = Math.pow(10, logValues[i]);
			//System.out.println(bandFrequencies[i]);
		}

		//Frequenzen zur Anzeige im EQ
		int tenPower = 0;
		double tenFactor;
		for(int i=0; i<numberOfBands; i++){
			frequenciesToShow[i] = (int) (0.5*(bandFrequencies[i] + bandFrequencies[i+1]));
			tenPower = (int) Math.floor(Math.log10((double)frequenciesToShow[i]));

			tenPower--;
			tenFactor = Math.pow(10,tenPower);

			frequenciesToShow[i] = (int) ((int) (frequenciesToShow[i] / (tenFactor) ) * tenFactor);
		}


		// Die Band-Indizes lassen sich erst einlesen, wenn die Buffergröße bekannt ist
		if (argBufferSize != 0){
			bufferSize = argBufferSize;

			bandIndices = new int[numberOfBands+1];
			for (int i=0; i<numberOfBands+1; i++){
				//-1 am Ende ja oder nein...? Weil Arrays hier ja anders addressiert werden... Ich glaube,
				//dass es hier aber nicht reingehört (hat eine Rechnung auf Papier so ergeben)
				bandIndices[i] = (int) Math.ceil(((double) bufferSize / sampleRate) * bandFrequencies[i]);
			}

			//oben eingrenzen
			bandIndices[numberOfBands] = (bufferSize / 2);

		}
	}

	public int[] getFrequenciesToShow(){
		return frequenciesToShow;
	}

	public void setSmoothnessType (int option){
		smoothnessType = option;
	}
	//Modifiziert die Arrays durch Referenz

	public void applyEQ(double[] bandDbCoefficients, double[] samples, FFT fft){
		int fUpperLimit = 0;
		int fLowerLimit = 0;
		int bandHalfPoint = 0;

		double lambda = 1;
		double smoothingFactor = 1;
		double currentBandFactorCoefficient = 1;
		double previousBandFactorCoefficient = 1;
		double nextBandFactorCoefficient = 1;


		//wenn Band-Indizes noch nicht bestimmt
		if (bandIndices == null){
			bufferSize = samples.length; //sollte Zweierpotenz sein, ist somit immer ohne Rest durch 2 teilbar

			bandIndices = new int[numberOfBands+1];
			for (int i=0; i<numberOfBands+1; i++){
				bandIndices[i] = (int) Math.ceil(((double) bufferSize / sampleRate) * bandFrequencies[i]);
			}
			//oben eingrenzen
			bandIndices[numberOfBands] = bufferSize / 2;
		}

		double[] imagFrequencies = new double[bufferSize];


		//Band-Koeffizienten müssen so viele sein, wie viele Bänder es gibt
		if (bandDbCoefficients.length != numberOfBands){
			System.err.println("Anzahl der Band-Koeffizienten stimmt nicht mit Anzahl Bänder im EQ überein!");
			System.exit(2);
		}
		fft.transform(samples, imagFrequencies); //Samples sind der Realteil

		for (int i=0; i < numberOfBands; i++){
			fLowerLimit = bandIndices[i];
			fUpperLimit = bandIndices[i+1];

			bandHalfPoint = (int) (0.5*(fLowerLimit + fUpperLimit));

			if (i == 0){
				previousBandFactorCoefficient = 1;
				currentBandFactorCoefficient = Math.pow(10,bandDbCoefficients[i]/20);
			}
			if (i < numberOfBands-1){
				nextBandFactorCoefficient = Math.pow(10,bandDbCoefficients[i+1]/20);
			}else{
				nextBandFactorCoefficient = 1;
			}

			for (int j=fLowerLimit; j<fUpperLimit; j++){
				if (smoothnessType == 1 || smoothnessType > 2){
					if (j < bandHalfPoint){
						lambda = (double) (j - fLowerLimit)/(bandHalfPoint - fLowerLimit);
						smoothingFactor = 0.5*((1-lambda)*previousBandFactorCoefficient + (1+lambda)*currentBandFactorCoefficient);
						samples[j] *= smoothingFactor;
						imagFrequencies[j] *= smoothingFactor;

					}else{
						lambda = (double) (j - bandHalfPoint)/(fUpperLimit - bandHalfPoint);
						smoothingFactor = 0.5*((2-lambda)*currentBandFactorCoefficient + lambda*nextBandFactorCoefficient);
						samples[j] *= smoothingFactor;
						imagFrequencies[j] *= smoothingFactor;
					}
				}else{
					samples[j] *= currentBandFactorCoefficient;
					imagFrequencies[j] *= currentBandFactorCoefficient;
				}

				//Symmetrie
				if (j == 0){ //j darf nicht 0 sein, dass ist ja der asymmetrische Teil...
					System.err.println("Zugriff auf Sample 0 (asymmetrischer Teil)!");
					System.exit(2);
				}
				samples[bufferSize-j] = samples[j];
				imagFrequencies[bufferSize-j] = -imagFrequencies[j];
			}

			previousBandFactorCoefficient = currentBandFactorCoefficient;
			currentBandFactorCoefficient = nextBandFactorCoefficient;

		}
		//Man muss darauf achten, dass nach der inversen Trafo alle imagFrequencies nahe 0 sind, sonst ist etwas schiefgegangen...
		fft.inverseTransform(samples, imagFrequencies);

		//Glättung (Mittelwertfilter)
		if (smoothnessType > 1){
			double sampleSum;
			double samplesClone[] = new double[samples.length];
			for (int i=smoothnessPoints; i<samples.length-smoothnessPoints; i++){
				sampleSum = samples[i];
				for (int j=-smoothnessPoints; j<=smoothnessPoints; j++){
					sampleSum += samples[i+j];
				}
				samplesClone[i] = sampleSum / (2*smoothnessPoints + 1);
			}

			for (int i=0; i<samples.length; i++){
				samples[i]  = samplesClone[i];
			}
		}

		//Skalieren
		for (int i=0; i<samples.length; i++){
			samples[i] /= bufferSize;
		}


		//Arbeit mit dem lastBuffer

		if (lastBuffer == null){
			lastBuffer = new double[lastBufferMemorySize];
		}else{
			//erste Samples anpassen an lastBuffer (Glättung am Rand)
			double sampleSum;
			double samplesClone[] = new double[lastBufferMemorySize];

			for (int i=0; i<lastBufferMemorySize; i++){
				sampleSum = 0;
				for (int j=0; j<lastBufferMemorySize - i; j++){
					sampleSum += lastBuffer[j];
				}
				for (int j=0; j<lastBufferMemorySize + i + 1; j++){
					sampleSum += samples[j];
				}
				sampleSum /= (2*lastBufferMemorySize + 1);
				samplesClone[i] = sampleSum;

			}
			for (int i=0; i<lastBufferMemorySize; i++){
				samples[i] = samplesClone[i];
			}

		}

		//Merken der letzten Samples vom aktuellen Buffer (d.h. neuen lastBuffer erzeugen)
		//lastBuffer wird verkehrt herum beschrieben, d.h. letztes Sample an 1. Stelle, vorletztes Sample an 2.Stelle,...
		for (int i=0; i<lastBufferMemorySize; i++){
			lastBuffer[i] = samples[samples.length - 1 - i];
		}


	}
}
