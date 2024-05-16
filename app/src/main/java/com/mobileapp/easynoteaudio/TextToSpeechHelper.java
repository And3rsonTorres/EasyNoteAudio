package com.mobileapp.easynoteaudio;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import androidx.lifecycle.ViewModel;

import java.util.Locale;

public class TextToSpeechHelper extends ViewModel {

    private final TextToSpeech tts;
    private boolean isInitialized = false;

    public TextToSpeechHelper(Context context) {
        tts = new TextToSpeech(context, new OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set the language for the text to speech engine.
                    int langResult = tts.setLanguage(Locale.getDefault());
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Handle language initialization failure
                        isInitialized = false;
                    } else {
                        isInitialized = true;
                    }
                } else {
                    // Handle TextToSpeech initialization failure
                    isInitialized = false;
                }
            }
        });
    }

    public void speak(String text) {
        if (isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            // Handle TTS not initialized
        }
    }

    public void stop() {
        if (isInitialized) {
            tts.stop();
        }
    }

    public void release() {
        if (isInitialized) {
            tts.shutdown();
        }
    }
}
