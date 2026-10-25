package com.hache.hachesquizz

/** Mapa explícito de recursos, para detectar nombres erróneos al compilar. */
object Recursos {
    fun audio(nombre: String?): Int = when (nombre) {
        "audio_piano" -> R.raw.audio_piano
        "audio_campana" -> R.raw.audio_campana
        "audio_reloj" -> R.raw.audio_reloj
        "audio_lluvia" -> R.raw.audio_lluvia
        "audio_sirena" -> R.raw.audio_sirena
        else -> 0
    }
}
