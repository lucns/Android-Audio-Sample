package com.sample.audio.calculators;

import android.util.Log;

public class AudioCalculator {

    private byte[] bytes;
    private int[] amplitudes;
    private double[] decibels;
    private double frequency;
    private int amplitude;
    private double decibel;

    public AudioCalculator() {
    }

    public AudioCalculator(byte[] bytes) {
        this.bytes = bytes;
        amplitudes = null;
        decibels = null;
        frequency = 0.0D;
        amplitude = 0;
        decibel = 0.0D;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        amplitudes = null;
        decibels = null;
        frequency = 0.0D;
        amplitude = 0;
        decibel = 0.0D;
    }

    public int[] getAmplitudes() {
        if (amplitudes == null) amplitudes = getAmplitudesFromBytes(bytes);
        return amplitudes;
    }

    public double[] getDecibels() {
        if (amplitudes == null) amplitudes = getAmplitudesFromBytes(bytes);
        if (decibels == null) {
            decibels = new double[amplitudes.length];
            for (int i = 0; i < amplitudes.length; i++) {
                decibels[i] = resizeNumber(getRealDecibel(amplitudes[i]));
            }
        }
        return decibels;
    }

    public int[] getAmplitudeLevels() {
        if (amplitudes == null) getAmplitudes();
        int major = 0;
        int minor = 0;
        for (int i : amplitudes) {
            if (i > major) major = i;
            if (i < minor) minor = i;
        }
        amplitude = Math.max(major, minor * (-1));
        return new int[] {major, minor};
    }

    public int getAmplitude() {
        if (amplitude == 0) getAmplitudeLevels();
        return amplitude;
    }

    public double getDecibel() {
        if (decibel == 0.0D) decibel = resizeNumber(getRealDecibel(amplitude));
        return decibel;
    }

    public double getFrequency() {
        if (frequency == 0.0D) frequency = retrieveFrequency();
        return frequency;
    }

    private double retrieveFrequency() {
        int length = bytes.length / 2;
        int sampleSize = 8192;
        while (sampleSize > length) sampleSize = sampleSize >> 1;

        FrequencyCalculator frequencyCalculator = new FrequencyCalculator(sampleSize);
        frequencyCalculator.feedData(bytes, length);

        return resizeNumber(frequencyCalculator.getFreq());
    }

    private double getRealDecibel(int amplitude) {
        if (amplitude < 0) amplitude *= -1;
        double amp = (((double) amplitude) / 32767.0d) * 100.0d;
        if (amp == 0.0d) {
            amp = 1.0d;
        }
        double decibel = Math.sqrt(100.0d / amp);
        decibel *= decibel;
        if (decibel > 100.0d) {
            decibel = 100.0d;
        }
        return ((-1.0d * decibel) + 1.0d) / Math.PI;
    }

    public double resizeNumber(double value) {
        int temp = (int) (value * 10.0d);
        return temp / 10.0d;
    }

    private int[] getAmplitudesFromBytes(byte[] bytes) {
        int[] amps = new int[bytes.length / 2];
        for (int i = 0; i < bytes.length; i += 2) {
            short buff = bytes[i + 1];
            short buff2 = bytes[i];

            buff = (short) ((buff & 0xFF) << 8);
            buff2 = (short) (buff2 & 0xFF);

            short res = (short) (buff | buff2);
            amps[i == 0 ? 0 : i / 2] = (int) res;
        }
        return amps;
    }
}
