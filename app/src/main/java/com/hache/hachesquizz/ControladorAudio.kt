package com.hache.hachesquizz

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Un reproductor principal, sin audios de preguntas superpuestos. */
class ControladorAudio(private val context: Context) {
    private var player: MediaPlayer? = null

    var cargado by mutableStateOf(false)
        private set
    var reproduciendo by mutableStateOf(false)
        private set
    var posicionMs by mutableIntStateOf(0)
        private set
    var duracionMs by mutableIntStateOf(0)
        private set
    var bucle by mutableStateOf(false)
        private set

    fun preparar(recurso: Int, posicionInicial: Int = 0) {
        liberarReproductor()
        if (recurso == 0) return

        try {
            val nuevo = MediaPlayer.create(context, recurso) ?: return
            nuevo.isLooping = bucle
            nuevo.setOnCompletionListener {
                // Dejarlo preparado para volver a pulsar reproducir.
                reproduciendo = false
                posicionMs = 0
                it.seekTo(0)
            }
            duracionMs = nuevo.duration
            val inicio = posicionInicial.coerceIn(0, duracionMs)
            if (inicio > 0) nuevo.seekTo(inicio)
            posicionMs = inicio
            player = nuevo
            cargado = true
        } catch (_: Exception) {
            liberarReproductor()
        }
    }

    fun alternarReproduccion() {
        val actual = player ?: return
        try {
            if (actual.isPlaying) {
                actual.pause()
                reproduciendo = false
            } else {
                actual.start()
                reproduciendo = true
            }
        } catch (_: IllegalStateException) {
            liberarReproductor()
        }
    }

    fun pausar() {
        try {
            player?.let { if (it.isPlaying) it.pause() }
        } catch (_: IllegalStateException) {
            // El reproductor puede estar finalizando.
        }
        reproduciendo = false
    }

    fun alternarBucle() {
        bucle = !bucle
        player?.isLooping = bucle
    }

    fun moverA(ms: Int) {
        val actual = player ?: return
        val objetivo = ms.coerceIn(0, duracionMs)
        actual.seekTo(objetivo)
        posicionMs = objetivo
    }

    fun refrescarPosicion() {
        player?.let { posicionMs = it.currentPosition.coerceIn(0, duracionMs) }
    }

    private fun liberarReproductor() {
        player?.setOnCompletionListener(null)
        player?.release()
        player = null
        cargado = false
        reproduciendo = false
        posicionMs = 0
        duracionMs = 0
    }

    fun liberar() = liberarReproductor()
}

/** Efectos cortos: se libera el anterior antes de iniciar el siguiente. */
object EfectosSonido {
    private var player: MediaPlayer? = null

    fun reproducir(context: Context, acierto: Boolean) {
        liberar()
        val id = if (acierto) R.raw.efecto_acierto else R.raw.efecto_error
        try {
            player = MediaPlayer.create(context.applicationContext, id)?.apply {
                setOnCompletionListener { liberar() }
                start()
            }
        } catch (_: Exception) {
            liberar()
        }
    }

    fun liberar() {
        player?.setOnCompletionListener(null)
        player?.release()
        player = null
    }
}
