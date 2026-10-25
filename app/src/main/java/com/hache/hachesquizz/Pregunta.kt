package com.hache.hachesquizz

/** Sigue admitiendo Pregunta(texto, resultado) de la práctica 2. */
class Pregunta(
    val texto: String,
    val resultado: Boolean,
    val audio: String? = null,
    val infoUrl: String? = null
) {
    val tieneAudio: Boolean
        get() = !audio.isNullOrBlank()
}
