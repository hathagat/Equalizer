package de.triple2.equalizer.controller;

/**
* Equalizer-Klasse. Beinhaltet die Logik, d.h. wie genau
* das Signal zu modifizieren ist.
*/
public class Equalizer {

	/// höchste darstellbare Frequenz; laut Abtasttheorem ist das die Hälfte der Samplerate
	private int highestFrequency = 0;

	/// 20 als vom Menschen hörbare, niedrigste Frequenz
	private int lowestFrequency = 20;

	/// Bandanzahl des Equalizers
	private int numberOfBands = 0;

	/// speichert die Logarithmenwerte der bandFrequencies
	private double[] logValues;

	/// die Frequenzen am Rand jedes EQ-Bandes
	private double[] bandFrequencies;

	/// die Frequenzen, die in der UI für jedes EQ-Band anzuzeigen sind
	private int[] frequenciesToShow;

	/// gibt an, wie weit jedes EQ-Band im Array geht
	private int[] bandIndices = null;

	/// Samplerate des Musikstücks (für Initialisierung benötigt)
	private int sampleRate;

	/// Buffergröße im EQ. Benötigt für die Berechnung der bandFrequencies
	private int bufferSize = 0;

	/*
	 * 3 Typen von Glättung (festgelegt in smoothnessType)
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

	/**
	* speichert einige Samples des letzten Buffers, um einen weicheren Übergang zum nächsten
	* Buffer zu schaffen.
	*/
	private double lastBuffer[] = null;

	/**
	* gibt an, wie viele Punkte aus dem letzten Buffer in den gleitenden Mittelwert
	* einbezogen werden sollen, um den Bufferanfang anzupassen.
	*/
	private int lastBufferMemorySize = 3;

	/**
	* Methode mit Samplerate, Bandanzahl und Buffergröße, um
	* die entsprechenden Membervariablen zu initialisieren.
	*
	* Diese werden benötigt, um gleich am Anfang die Breite der Bänder
	* und die betroffenen Frequenzen zu berechnen.
	*
	* @param argSampleRate Samplerate
	* @param argNumberOfBands Anzahl der Bänder
	* @return Integer Array mit den Frequenzen der Bänder
	*/
	public int[] initializeBands(int argSampleRate, int argNumberOfBands) {
		// Das hier ist so ziemlich eine 1-zu-1-Anwendung des Matlab-Teils.
		// Vielleicht ist es besser, das Matlab-Beispiel zu lesen, um das hier zu verstehen,
		// da fast alle Erklärungen hier mit copy-paste zutreffen.

		highestFrequency = sampleRate/2; // laut Abtasttheorem ist höchste Frequenz = halbe Samplerate
		numberOfBands = argNumberOfBands;

		logValues = new double[numberOfBands+1]; // Array initialiseren

		//Ober- und Untergrenzen (log-Werte bestimmen)
		//Warum ich das mache, wird schon im Simulationsteil (also in der Matlab-Datei)
		//beschrieben (deswegen muss man das hier im Beleg nicht wiederholen, denke ich)
		//Das entspricht dem c_vektor in der Matlab-Datei
		//Kurze Erklärung: In EQs sind logarithmische Darstellungen üblich (die Bänder sind somit
		//nicht gleichbreit). Das wird hier auch nachgebildet.

		logValues[0] = Math.log10(lowestFrequency); //log-Wert der tiefsten Frequenz
		logValues[numberOfBands] = Math.log10(highestFrequency); //log-Wert der höchsten Frequenz

		// log-Abstand zwischen dem höchsten und geringsten Wert
		double frequencySpan = logValues[numberOfBands]-logValues[0];

		// da der höchste/tiefste logarithmierte Wert bekannt ist, können die Zwischenwerte
		// auch berechnet werden (mit linearer Interpolation, d.h. einfach eine Linie durchziehen,
		// die vom höchsten zum geringsten Wert geht)
		for (int i=1; i<numberOfBands; i++){
			logValues[i] = logValues[0] + (i/(double)numberOfBands) * frequencySpan;

		}

		// Grenzfrequenzen der einzelnen Bänder bestimmen (entspricht f_vector im Matlab-Teil)
		// Das ist die Umkehrung des Logarithmierens, um die Frequenzen zu ermitteln.
		bandFrequencies = new double[numberOfBands+1];
		for (int i=0; i<numberOfBands+1; i++){
			bandFrequencies[i] = Math.pow(10, logValues[i]);
		}

		//Frequenzen zur Anzeige im EQ bestimmen
		frequenciesToShow = new int[numberOfBands];

		//tenPower und tenFactor nutze ich, um die Zahlen zu runden.
		//Es kommen sonst krumme Frequenzen wie 16107 Hz raus, was für die Anzeige nicht schön ist,
		//deswegen wird das gerundet auf 16000. Jedoch können die ersten Bänder nicht auf
		//Tausender gerundet werden, sonst würde aus 22 Hz dann 0 Hz werden.
		//Die Rundungsgenauigkeit variiert also nach der Länge der Zahl und dafür ist tenPower
		//da.

		int tenPower = 0;
		double tenFactor;
		for(int i=0; i<numberOfBands; i++){
			//die anzuzeigende Frequenz ist genau in der Mitte zwischen 2 Bändern
			frequenciesToShow[i] = (int) (0.5*(bandFrequencies[i] + bandFrequencies[i+1]));

			// damit bestimme ich, wie lang die Zahl ist, z.B. hat 16000 fünf Stellen, also
			// würde für tenPower=5 rauskommen.
			tenPower = (int) Math.floor(Math.log10((double)frequenciesToShow[i]));

			tenPower--;
			tenFactor = Math.pow(10,tenPower);

			//das Runden auf tenPower Stellen wird mit einer int-Division und Multiplikation
			//realisiert. Beispiel: 16107 int-dividiert durch 1000 ist 16. Das wieder mal 1000
			//ist 16000.
			frequenciesToShow[i] = (int) ((int) (frequenciesToShow[i] / (tenFactor) ) * tenFactor);
		}
		return frequenciesToShow;
	}

	/**
	 * Bestimmt die Frequenzen der einzelnen Bänder.
	 * @param argSampleRate Samplerate
	 * @param argBufferSize Buffergröße in Frames
	 */
	public void calculateBandIndices(int argSampleRate, int argBufferSize) {
		sampleRate = argSampleRate;

		// Bestimmung der Grenzen im Array für jedes Band (entspricht f_limits_vector in Matlab)
		// Die Band-Indizes lassen sich erst berechnen, wenn die Buffergröße bekannt ist, denn
		// die Buffergröße bestimmt auch, wie viele Frequenzen die FFT berechnet. Und man muss
		// erst wissen, wie viele Frequenzen man hat, bevor man die einteilen kann.
		if (argBufferSize != 0){
			bufferSize = argBufferSize;
			bandIndices = new int[numberOfBands+1];

			//Die Formel ist schwer zu erklären (im Matlab-Teil habe ich auch keine gute Erklärung
			//hinbekommen), aber sie berechnet die Array-Grenzen.
			for (int i=0; i<numberOfBands+1; i++){

				bandIndices[i] = (int) Math.ceil(((double) bufferSize / sampleRate) * bandFrequencies[i]);
			}

			//oben eingrenzen (Erklärung im Matlab-Skript, wäre hier zu lange zum Aufschreiben)
			bandIndices[numberOfBands] = (bufferSize / 2);

		}
	}

	/**
	* Setzt fest, welche Art von Glättung im EQ erfolgen soll (siehe oben).
	* @param option Integer, der die Art der Glättung angibt. Die Zahl sollte
	* 0,1,2 oder 3 sein. Für andere Werte wird kein Abfangen durchgeführt!
	*/
	public void setSmoothnessType (int option){
		smoothnessType = option;
	}

	/**
	* Wendet den Equalizer mit den gegebenen Bandkoeffizienten auf die Samples an.
	*
	* @param bandDbCoefficients Die Reglereinstellungen des Equalizers in Dezibel.
	* @param samples Menge von Samples, auf die der Equalizer anzuwenden ist.
	* @param fft Ein Fast-Fourier-Transformations-Objekt. Dies wird benötigt, damit
	*   die FFT nicht jedes mal neu initialisiert werden muss, um Rechenzeit
	*   (speziell durch die Vorberechnung trigonometrischer Tabellen) zu sparen.
	*/
	public void applyEQ(double[] bandDbCoefficients, double[] samples, FFT fft){
		//Grenzen für das aktuelle Band
		int fUpperLimit = 0;
		int fLowerLimit = 0;

		//Mittelpunkt für jedes Band
		int bandHalfPoint = 0;

		//Parameter für die Glättung (die Mathematik dahinter kann wohl nur
		//im Theorieteil beschrieben werden).
		double lambda = 1;
		double smoothingFactor = 1; //Koeffizient für Bandkoeffizienten
		double currentBandFactorCoefficient = 1; //aktueller Bandkoeffizient
		double previousBandFactorCoefficient = 1; //vorheriger Bandkoeffizient
		double nextBandFactorCoefficient = 1; //nächster Bandkoeffizient


		//wenn Band-Indizes noch nicht bestimmt sind (weil bei der Initialisierung
		//als bufferSize=0 vorgegeben wurde), wird die bufferSize aus samples.length gelesen;
		//dann wird das gleiche wie oben getan
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

		//FFT anwenden auf die Samples (in die übergebenen Arrays wird das Ergebnis geschrieben)
		fft.transform(samples, imagFrequencies); //Samples sind der Realteil

		//Anwendung des Equalizers
		//für jedes Band
		for (int i=0; i < numberOfBands; i++){

			//Grenzen bestimmen
			fLowerLimit = bandIndices[i];
			fUpperLimit = bandIndices[i+1];

			//Mittelpunkt des Bandes bestimmen
			bandHalfPoint = (int) (0.5*(fLowerLimit + fUpperLimit));

			//Einstellen von next/previous BandFactorCoefficient für den ersten Durchlauf
			//Mit der Potenz wird die dB-Zahl in einen Faktor umgewandelt (siehe auch Matlab
			//für weitere Info)
			if (i == 0){
				previousBandFactorCoefficient = 1;
				currentBandFactorCoefficient = Math.pow(10,bandDbCoefficients[i]/20);
			}
			if (i < numberOfBands-1){
				nextBandFactorCoefficient = Math.pow(10,bandDbCoefficients[i+1]/20);
			}else{
				nextBandFactorCoefficient = 1;
			}

			//alle Frequenzen in dem aktuellen Band werden nun modifiziert
			//dazu werden einfach nur die Frequenzen mit den jeweiligen
			//Bandkoeffizienten (als Faktor, nicht in dB) multipliziert
			for (int j=fLowerLimit; j<fUpperLimit; j++){

				//Wenn Glättungsverfahren 1 auszuführen ist
				if (smoothnessType == 1 || smoothnessType > 2){
					//Glättungsverfahren "Glättung der zu multiplizierenden Funktion"
					//Das kann wahrscheinlich nur im Theorieteil erklärt werden...
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
					//wenn das Glättungsverfahren nicht auszuführen ist, wird normal multipliziert
					samples[j] *= currentBandFactorCoefficient;
					imagFrequencies[j] *= currentBandFactorCoefficient;
				}

				//Im Spektrum muss die Symmetrie aufrechterhalten bleiben, sonst geht die
				//inverse FFT nicht mehr auf. Deswegen wird das berechnete Ergebnis
				//nochmal auf die andere Seite geschrieben

				//für die 0. Frequenz darf das aber nicht passieren, denn die hat kein
				//symmetrisches Gegenstück

				if (j == 0){ //j darf nicht 0 sein, dass ist ja der asymmetrische Teil...
					System.err.println("Zugriff auf Sample 0 (asymmetrischer Teil)!");
					//System.exit(2);
				}else{
					samples[bufferSize-j] = samples[j];
					imagFrequencies[bufferSize-j] = -imagFrequencies[j];
				}
			}
			//Einstellen von den BandFactorCoefficients für den nächsten Durchlauf
			previousBandFactorCoefficient = currentBandFactorCoefficient;
			currentBandFactorCoefficient = nextBandFactorCoefficient;

		}

		//Inverse Transformation, um aus dem Frequenzspektrum wieder das Zeitsignal zu gewinnen
		//die (konjugierte) Symmetrie ist hier wichtig, d.h. nach der inversen Transformation
		//müssen alle imaginären Teile nahe 0 sein.
		fft.inverseTransform(samples, imagFrequencies);

		//Glättung (Mittelwertfilter)
		//ich glaub, wir brauchen das gar nicht, da das nur ein Tiefpassfilter ist...
		//wir können das ja für den Theorieteil drinlassen
		//Erklärung, wie das funktioniert, wäre hier zu lang (ist dann im Matheteil drin)
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

		//Skalieren (die genutzte FFT ergibt nach der inversen FFT
		//ein zu großes Signal, was laut Doku skaliert werden muss)
		for (int i=0; i<samples.length; i++){
			samples[i] /= bufferSize;
		}


		//Arbeit mit dem lastBuffer
		//Er speichert sich die letzten Samples aus dem letzten Equalizer-Aufruf,
		//um das aktuelle Ergebnis an den Rändern daran anzupassen
		if (lastBuffer == null){
			lastBuffer = new double[lastBufferMemorySize];
		}else{
			//erste Samples anpassen an lastBuffer (Glättung am Rand)
			double sampleSum;
			double samplesClone[] = new double[lastBufferMemorySize];

			//es wird hier im Wesentlichen auch ein gleitender Mittelwert
			//genutzt; der so breit wie lastBufferMemorySize*2+1 ist
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
