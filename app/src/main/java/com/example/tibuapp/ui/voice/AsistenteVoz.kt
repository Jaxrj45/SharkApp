package com.example.tibuapp.ui.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

// Maneja la voz de salida — la app hablando al empleado
class AsistenteVoz(context: Context) {

    private var tts: TextToSpeech? = null
    private var listo = false

    init {
        tts = TextToSpeech(context) { estado ->
            if (estado == TextToSpeech.SUCCESS) {
                // Configuramos el idioma a español de Guatemala
                val resultado = tts?.setLanguage(Locale("es", "GT"))
                listo = resultado != TextToSpeech.LANG_MISSING_DATA &&
                        resultado != TextToSpeech.LANG_NOT_SUPPORTED
                // Si Guatemala no está disponible, usamos español genérico
                if (!listo) {
                    tts?.setLanguage(Locale("es", "ES"))
                    listo = true
                }
            }
        }
    }

    fun hablar(texto: String) {
        if (listo) {
            tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun detener() {
        tts?.stop()
    }

    fun liberar() {
        tts?.stop()
        tts?.shutdown()
    }
}