package com.mobileapp.easynoteaudio;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TextToSpeechHelper {

    private static final String TAG = "TextToSpeechHelper";
    private TextToSpeech tts;
    private boolean isInitialized = false;

    public TextToSpeechHelper(Context context) {
        if (context == null) return;
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int langResult = tts.setLanguage(Locale.getDefault());
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Language is not supported by TTS engine.");
                    isInitialized = false;
                } else {
                    isInitialized = true;
                }
            } else {
                Log.w(TAG, "TTS Initialization failed.");
                isInitialized = false;
            }
        });
    }

    public void speak(String text) {
        if (text == null || text.trim().isEmpty()) return;
        if (isInitialized && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "EasyNoteTTS");
        }
    }

    public boolean isSpeaking() {
        return isInitialized && tts != null && tts.isSpeaking();
    }

    public void stop() {
        if (isInitialized && tts != null) {
            tts.stop();
        }
    }

    public void release() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            isInitialized = false;
        }
    }
}
