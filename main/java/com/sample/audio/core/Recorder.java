package com.sample.audio.core;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

public class Recorder {

    private int audioSource = MediaRecorder.AudioSource.DEFAULT;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int sampleRate = 44100;
    private double attenuation = 1;
    private Thread thread;
    private Callback callback;

    public Recorder() {
    }

    public Recorder(Callback callback) {
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void start() {
        if (thread != null) return;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

                int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);
                AudioRecord recorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioEncoding, minBufferSize);

                if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                    Thread.currentThread().interrupt();
                    return;
                } else {
                    Log.i(Recorder.class.getSimpleName(), "Started.");
                    //callback.onStart();
                }
                byte[] buffer = new byte[minBufferSize];
                recorder.startRecording();

                while (thread != null && !thread.isInterrupted() && recorder.read(buffer, 0, minBufferSize) > 0) {
                    if (attenuation != 1) buffer = applyGain(buffer, attenuation);
                    callback.onBufferAvailable(buffer);
                }
                recorder.stop();
                recorder.release();
            }
        }, Recorder.class.getName());
        thread.start();
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void changeAttenuation(double gain) {
        if (gain < -25) gain = -25;
        if (gain > 25) gain = 25;

        if (gain > 0) {
            attenuation = gain + 1;
        } else if (gain < 0){
            attenuation = 1.04D - ((-1) * (gain / 25));
        } else {
            attenuation = 1;
        }
    }

    private byte[] applyGain(byte[] data, double gain) {
        byte[] data2 = new byte[data.length];
        for (int i = 0; i < data.length; i += 2) {
            short buff = data[i + 1];
            short buff2 = data[i];

            buff = (short) ((buff & 0xFF) << 8);
            buff2 = (short) (buff2 & 0xFF);

            short res = (short) (buff | buff2);
            res = (short) (res * gain);

            data2[i] = (byte) res;
            data2[i + 1] = (byte) (res >> 8);
        }
        return data2;
    }
}
