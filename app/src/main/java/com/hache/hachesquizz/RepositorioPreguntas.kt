package com.hache.hachesquizz

import android.content.Context
import java.io.File

/** Conserva las preguntas guardadas anteriormente en preguntas.txt. */
object RepositorioPreguntas {
    private const val ARCHIVO = "preguntas.txt"
    private const val CLASICO = "CLASICO"
    private const val NINOS = "NINOS"

    // Sonidos sintetizados que acompañan al paquete de esta práctica.
    private val multimedia = listOf(
        Pregunta(
            "Escucha el sonido. ¿Es un piano?", true, "audio_piano",
            "https://es.wikipedia.org/wiki/Piano"
        ),
        Pregunta(
            "Escucha el sonido. ¿Es una campana?", true, "audio_campana",
            "https://es.wikipedia.org/wiki/Campana"
        ),
        Pregunta(
            "Escucha el sonido. ¿Se oye un reloj?", true, "audio_reloj",
            "https://es.wikipedia.org/wiki/Reloj"
        ),
        Pregunta(
            "Escucha el sonido. ¿Es una guitarra eléctrica?", false, "audio_lluvia",
            "https://es.wikipedia.org/wiki/Lluvia"
        ),
        Pregunta(
            "Escucha el sonido. ¿Se oye una sirena?", true, "audio_sirena",
            "https://dle.rae.es/sirena"
        )
    )

    private fun archivo(context: Context) = File(context.filesDir, ARCHIVO)

    private fun inicializar(context: Context) {
        val destino = archivo(context)
        if (!destino.exists()) {
            val iniciales = listOf(
                "$CLASICO|true|¿Android está basado en Linux?",
                "$CLASICO|false|¿Java fue creado por Microsoft?",
                "$CLASICO|true|¿Kotlin es un lenguaje de programación?",
                "$CLASICO|false|¿HTML es una base de datos?",
                "$CLASICO|true|¿Python permite programar con objetos?",
                "$NINOS|true|¿Leonardo da Vinci pintó la Mona Lisa?",
                "$NINOS|true|¿El Amazonas es el río más caudaloso del mundo?",
                "$NINOS|false|¿Mercurio es el planeta más caliente?",
                "$NINOS|true|¿El Principito vive en un planeta muy pequeño?",
                "$NINOS|false|¿El Sol es un planeta?"
            )
            destino.writeText(iniciales.joinToString("\n", postfix = "\n"), Charsets.UTF_8)
        }

        // Migración de Práctica 2 a Práctica 3: añade cada multimedia una sola vez.
        val contenido = destino.readText(Charsets.UTF_8)
        val nuevas = multimedia.filter { pregunta ->
            !contenido.contains("|@${pregunta.audio}|")
        }
        if (nuevas.isNotEmpty()) {
            destino.appendText(
                nuevas.joinToString("", transform = { formatear(CLASICO, it) + "\n" }),
                Charsets.UTF_8
            )
        }
    }

    fun cargar(context: Context, modoNinos: Boolean): ArrayList<Pregunta> {
        inicializar(context)
        val categoria = if (modoNinos) NINOS else CLASICO
        val lista = arrayListOf<Pregunta>()
        archivo(context).forEachLine(Charsets.UTF_8) { linea ->
            interpretar(linea, categoria)?.let { lista.add(it) }
        }
        return lista
    }

    private fun interpretar(linea: String, categoria: String): Pregunta? {
        val partes = linea.split('|', limit = 3)
        if (partes.size != 3 || partes[0] != categoria) return null
        val correcta = partes[1].toBooleanStrictOrNull() ?: return null
        val resto = partes[2]

        // Formato nuevo: CATEGORIA|true|@audio|https://web|texto
        if (resto.startsWith('@')) {
            val datos = resto.drop(1).split('|', limit = 3)
            if (datos.size != 3 || datos[2].isBlank()) return null
            return Pregunta(datos[2], correcta, datos[0].ifBlank { null }, datos[1].ifBlank { null })
        }
        // Formato antiguo: CATEGORIA|true|texto
        return if (resto.isNotBlank()) Pregunta(resto, correcta) else null
    }

    private fun formatear(categoria: String, pregunta: Pregunta): String {
        val texto = pregunta.texto.trim().replace(Regex("\\s+"), " ").trimStart('@')
        require(texto.isNotBlank()) { "La pregunta no puede estar vacía" }
        return if (pregunta.tieneAudio) {
            "$categoria|${pregunta.resultado}|@${pregunta.audio}|${pregunta.infoUrl.orEmpty()}|$texto"
        } else {
            "$categoria|${pregunta.resultado}|$texto"
        }
    }

    // Mantiene la firma utilizada por AgregarPregunta.kt de la Práctica 2.
    fun guardar(context: Context, modoNinos: Boolean, pregunta: Pregunta) {
        inicializar(context)
        val categoria = if (modoNinos) NINOS else CLASICO
        archivo(context).appendText(formatear(categoria, pregunta) + "\n", Charsets.UTF_8)
    }
}
