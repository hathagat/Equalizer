package de.triple2.equalizer.controller;
/*
 * Free FFT and convolution (Java)
 *
 * Copyright (c) 2014 Project Nayuki
 * http://www.nayuki.io/page/free-small-fft-in-multiple-languages
 *
 * (MIT License)
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */

/**
* Externe Klasse zur Berechnung der FFT.
* Sie wurde etwas umgeschrieben, sodass sie besser zur Equalizer-Anwendung passt.
* Speziell werden nur noch Zweierpotenzen als Buffergrößen zugelassen und
* trigonometrische Tabellen im Voraus beim Konstruktor berechnet, um Rechenzeit zu sparen.
*/
public class FFT {
	double[] cosTable = null;
	double[] sinTable = null;

	public FFT(){

	}

	//Vorberechnung trigonometrischer Tabellen (soll die FFT beschleunigen)
	public FFT(int n){

		int levels = 31 - Integer.numberOfLeadingZeros(n);  // Equal to floor(log2(n))
		if (1 << levels != n)
			throw new IllegalArgumentException("Length is not a power of 2");
		double[] cosTable = new double[n / 2];
		double[] sinTable = new double[n / 2];
		for (int i = 0; i < n / 2; i++) {
			cosTable[i] = Math.cos(2 * Math.PI * i / n);
			sinTable[i] = Math.sin(2 * Math.PI * i / n);
		}
	}
	/*
	 * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
	 * The vector can have any length. This is a wrapper function.
	 */
	public void transform(double[] real, double[] imag) {
		if (real.length != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");

		int n = real.length;
		if (n == 0)
			return;
		else if ((n & (n - 1)) == 0){  // Is power of 2
			transformRadix2(real, imag);
			//System.out.println("Radix-2");
		}
		else{  // More complicated algorithm for arbitrary sizes
			//transformBluestein(real, imag);
			System.err.println("Bluestein (missing!)");
		}
	}


	/*
	 * Computes the inverse discrete Fourier transform (IDFT) of the given complex vector, storing the result back into the vector.
	 * The vector can have any length. This is a wrapper function. This transform does not perform scaling, so the inverse is not a true inverse.
	 */
	public void inverseTransform(double[] real, double[] imag) {
		transform(imag, real);
	}


	/*
	 * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
	 * The vector's length must be a power of 2. Uses the Cooley-Tukey decimation-in-time radix-2 algorithm.
	 */
	public void transformRadix2(double[] real, double[] imag) {
		// Initialization
		if (real.length != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		int n = real.length;
		int levels = 31 - Integer.numberOfLeadingZeros(n);  // Equal to floor(log2(n))
		if (1 << levels != n)
			throw new IllegalArgumentException("Length is not a power of 2");

		if (sinTable == null){
			cosTable = new double[n / 2];
			sinTable = new double[n / 2];
			//System.out.println("trig. Tabellen neu berechnet");
			for (int i = 0; i < n / 2; i++) {
				cosTable[i] = Math.cos(2 * Math.PI * i / n);
				sinTable[i] = Math.sin(2 * Math.PI * i / n);
			}
		}

		// Bit-reversed addressing permutation
		for (int i = 0; i < n; i++) {
			int j = Integer.reverse(i) >>> (32 - levels);
			if (j > i) {
				double temp = real[i];
				real[i] = real[j];
				real[j] = temp;
				temp = imag[i];
				imag[i] = imag[j];
				imag[j] = temp;
			}
		}

		// Cooley-Tukey decimation-in-time radix-2 FFT
		for (int size = 2; size <= n; size *= 2) {
			int halfsize = size / 2;
			int tablestep = n / size;
			for (int i = 0; i < n; i += size) {
				for (int j = i, k = 0; j < i + halfsize; j++, k += tablestep) {
					double tpre =  real[j+halfsize] * cosTable[k] + imag[j+halfsize] * sinTable[k];
					double tpim = -real[j+halfsize] * sinTable[k] + imag[j+halfsize] * cosTable[k];
					real[j + halfsize] = real[j] - tpre;
					imag[j + halfsize] = imag[j] - tpim;
					real[j] += tpre;
					imag[j] += tpim;
				}
			}
			if (size == n)  // Prevent overflow in 'size *= 2'
				break;
		}
	}

}
