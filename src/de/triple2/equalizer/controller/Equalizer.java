package de.triple2.equalizer.controller;


/*
 * Es wird laut Doku empfohlen, bei FFT.java die cos/sin-Tabellen im Voraus zu berechnen, 
 * wenn die BufferSizes immer gleich sind (was hier der Fall ist).
 * 
 */
public class Equalizer {
	
	private int highestFrequency = 0;
	private int lowestFrequency = 20; //20 als vom Menschen hörbare, niedrigste Frequenz
	private int numberOfBands = 0;
	private double[] logValues;
	private double[] bandFrequencies;
	private int[] bandIndices = null;
	
	private int sampleRate;
	private int bufferSize = 0;
	
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
		for (int i=0; i<numberOfBands+1; i++){
			bandFrequencies[i] = Math.pow(10, logValues[i]);
			//System.out.println(bandFrequencies[i]);
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
//			for (int i=0; i<numberOfBands+1; i++){
//				System.out.println(bandIndices[i]);
//			}
		}
	}
	
	//Modifiziert die Arrays durch Referenz
	
	public void applyEQ(double[] bandDbCoefficients, double[] samples, FFT fft){
		int fUpperLimit = 0;
		int fLowerLimit = 0;
		double currentBandFactorCoefficient = 1;
		
		
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
			return;
		}
		fft.transform(samples, imagFrequencies); //Samples sind der Realteil

		for (int i=0; i < numberOfBands; i++){
			fLowerLimit = bandIndices[i];
			fUpperLimit = bandIndices[i+1];
			
			currentBandFactorCoefficient = Math.pow(10,bandDbCoefficients[i]/20);
			
			for (int j=fLowerLimit; j<fUpperLimit; j++){
				samples[j] *= currentBandFactorCoefficient;
				imagFrequencies[j] *= currentBandFactorCoefficient;
				
				//Symmetrie
				if (j == 0){
					System.exit(2);
				}
				samples[bufferSize-j] = samples[j];
				imagFrequencies[bufferSize-j] = -imagFrequencies[j];
			}
		}
		//Man muss darauf achten, dass nach der inversen Trafo alle imagFrequencies nahe 0 sind, sonst ist etwas schiefgegangen...
		fft.inverseTransform(samples, imagFrequencies);	
		
		//Skalieren
		for (int i=0; i<samples.length; i++){
			samples[i] /= bufferSize;
		}
		
	}
}
