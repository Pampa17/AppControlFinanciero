package com.jpdev.appcontrolfinanciero.ui.agregar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface VoiceState {
    data object Idle : VoiceState
    data object Listening : VoiceState
    data class Result(val text: String) : VoiceState
    data class Error(val message: String) : VoiceState
}

/** Thin wrapper over android.speech.SpeechRecognizer — free, built into the platform. */
class VoiceRecognizerController(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null
    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = VoiceState.Error("El reconocimiento de voz no está disponible en este dispositivo")
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = VoiceState.Listening
                }

                override fun onResults(results: Bundle?) {
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    _state.value = if (text != null) VoiceState.Result(text)
                    else VoiceState.Error("No se entendió, intenta de nuevo")
                }

                override fun onError(error: Int) {
                    _state.value = VoiceState.Error("No se pudo reconocer el audio, intenta de nuevo")
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            startListening(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
                }
            )
        }
    }

    fun reset() {
        _state.value = VoiceState.Idle
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }
}
