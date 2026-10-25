package com.hache.hachesquizz

import android.content.Context
import android.media.MediaPlayer

/**
 * Práctica 4 - Apartado 4.3: Música de fondo.
 * Gestiona una única instancia de MediaPlayer en bucle que se mantiene
 * activa durante la navegación entre todas las actividades de la aplicación.
 */
object MusicaFondo {

    private var mediaPlayer: MediaPlayer? = null
    var estaHabilitada: Boolean = false
        private set

    /**
     * Alterna la reproducción de la música ambiental (Play / Pause).
     */
    fun alternar(context: Context): Boolean {
        return if (estaSonando()) {
            pausar()
            estaHabilitada = false
            false
        } else {
            reproducir(context)
            estaHabilitada = true
            true
        }
    }

    /**
     * Inicia o reanuda la música de fondo en bucle continuo.
     */
    fun reproducir(context: Context) {
        estaHabilitada = true
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.musica_ambiental)?.apply {
                    isLooping = true
                    setVolume(0.55f, 0.55f)
                }
            }
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                }
            }
        } catch (_: Exception) {
            // Manejo de posibles excepciones de MediaPlayer
        }
    }

    /**
     * Pausa la música de fondo sin destruir el reproductor.
     */
    fun pausar() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Reanuda la reproducción únicamente si el usuario la tenía habilitada.
     */
    fun reanudar() {
        if (estaHabilitada) {
            try {
                mediaPlayer?.let {
                    if (!it.isPlaying) {
                        it.start()
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Comprueba si la música está reproduciéndose actualmente.
     */
    fun estaSonando(): Boolean = mediaPlayer?.isPlaying == true

    /**
     * Libera los recursos del reproductor al cerrar la aplicación.
     */
    fun liberar() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
            estaHabilitada = false
        }
    }
}
