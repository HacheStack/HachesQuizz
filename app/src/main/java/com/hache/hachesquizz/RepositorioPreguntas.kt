
package com.hache.hachesquizz

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object RepositorioPreguntas {

    private const val ARCHIVO = "preguntas.txt"

    // Crear el fichero con las preguntas iniciales
    private fun inicializar(context: Context) {

        val archivo = File(context.filesDir, ARCHIVO)

        // Si ya existe, no lo modificamos
        if (archivo.exists()) return

        val preguntasIniciales = listOf(
            "CLASICO|true|¿Android está basado en Linux?",
            "CLASICO|false|¿Java fue creado por Microsoft?",
            "CLASICO|true|¿Kotlin es un lenguaje de programación?",
            "CLASICO|false|¿HTML es una base de datos?",
            "CLASICO|true|¿Python permite programar con objetos?",

            "NINOS|true|¿Leonardo da Vinci pintó la Mona Lisa?",
            "NINOS|true|¿El Amazonas es el río más caudaloso del mundo?",
            "NINOS|false|¿Mercurio es el planeta más caliente?",
            "NINOS|true|¿El Principito vive en un planeta muy pequeño?",
            "NINOS|false|¿El Sol es un planeta?"
        )

        FileOutputStream(archivo)
            .bufferedWriter(Charsets.UTF_8)
            .use { escritor ->

                preguntasIniciales.forEach { linea ->
                    escritor.write(linea)
                    escritor.newLine()
                }
            }
    }

    // Leer las preguntas del fichero
    fun cargar(
        context: Context,
        modoNinos: Boolean
    ): ArrayList<Pregunta> {

        inicializar(context)

        val preguntas = arrayListOf<Pregunta>()

        val categoria = if (modoNinos) {
            "NINOS"
        } else {
            "CLASICO"
        }

        val archivo = File(context.filesDir, ARCHIVO)

        FileInputStream(archivo)
            .bufferedReader(Charsets.UTF_8)
            .use { lector ->

                lector.forEachLine { linea ->

                    val partes = linea.split("|", limit = 3)

                    if (partes.size == 3 &&
                        partes[0] == categoria
                    ) {

                        val resultado = when (partes[1]) {
                            "true" -> true
                            "false" -> false
                            else -> null
                        }

                        if (resultado != null &&
                            partes[2].isNotBlank()
                        ) {
                            preguntas.add(
                                Pregunta(partes[2], resultado)
                            )
                        }
                    }
                }
            }

        return preguntas
    }

    // Añadir una pregunta sin borrar las existentes
    fun guardar(
        context: Context,
        modoNinos: Boolean,
        pregunta: Pregunta
    ) {

        inicializar(context)

        val categoria = if (modoNinos) {
            "NINOS"
        } else {
            "CLASICO"
        }

        // Evitar saltos de línea dentro del enunciado
        val textoLimpio = pregunta.texto
            .trim()
            .replace(Regex("\\s+"), " ")

        require(textoLimpio.isNotEmpty()) {
            "La pregunta no puede estar vacía"
        }

        val archivo = File(context.filesDir, ARCHIVO)

        FileOutputStream(archivo, true)
            .bufferedWriter(Charsets.UTF_8)
            .use { escritor ->

                escritor.write(
                    "$categoria|${pregunta.resultado}|$textoLimpio"
                )
                escritor.newLine()
            }
    }
}


