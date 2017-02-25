package com.sample.audio.core;

public interface Callback {
    void onBufferAvailable(byte[] buffer);
}