package com.hache.hachesquizz

import android.content.Context
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.util.Locale

/**
 * Práctica 4 - Apartados 4.8, 4.9 y 4.11:
 * Repositorio de localizaciones, persistencia en archivos locales y cálculo de distancias.
 */
object RepositorioLugares {

    private const val ARCHIVO_LUGARES = "lugares_personalizados.txt"

    // Coordenadas centrales del municipio de referencia (Plaza de España, Santa Cruz de Tenerife)
    val CENTRO_MUNICIPIO = LatLng(28.46730, -16.24720)
    const val NOMBRE_MUNICIPIO = "Santa Cruz de Tenerife"

    // Lugares iniciales obligatorios de la práctica
    private val LUGARES_INICIALES = listOf(
        Lugar(
            nombre = "CIFP César Manrique",
            descripcion = "Centro Integrado de FP en Ofra, referente en Informática y Comunicaciones.",
            latitud = 28.45574,
            longitud = -16.28270,
            esPersonalizado = false
        ),
        Lugar(
            nombre = "Plaza de España",
            descripcion = "Centro neurálgico de Santa Cruz de Tenerife con lago artificial marino.",
            latitud = 28.46730,
            longitud = -16.24720,
            esPersonalizado = false
        ),
        Lugar(
            nombre = "Parque García Sanabria",
            descripcion = "Parque botánico con el famoso Reloj de Flores y esculturas.",
            latitud = 28.47170,
            longitud = -16.25360,
            esPersonalizado = false
        ),
        Lugar(
            nombre = "Catedral de San Cristóbal de La Laguna",
            descripcion = "Catedral neoclásica en ciudad Patrimonio de la Humanidad.",
            latitud = 28.48900,
            longitud = -16.31550,
            esPersonalizado = false
        ),
        Lugar(
            nombre = "P. N. del Teide (El Portillo)",
            descripcion = "Centro de Visitantes y acceso a las cumbres volcánicas del Teide.",
            latitud = 28.29520,
            longitud = -16.56560,
            esPersonalizado = false
        )
    )

    private fun archivo(context: Context): File {
        return File(context.filesDir, ARCHIVO_LUGARES)
    }

    /**
     * Devuelve las localizaciones iniciales junto con las añadidas por el usuario.
     */
    fun cargarTodos(context: Context): List<Lugar> {
        val lista = ArrayList<Lugar>(LUGARES_INICIALES)
        val file = archivo(context)

        if (file.exists()) {
            file.forEachLine(Charsets.UTF_8) { linea ->
                val partes = linea.split('|')
                if (partes.size >= 4) {
                    val nombre = partes[0].trim()
                    val desc = partes[1].trim()
                    val lat = partes[2].toDoubleOrNull()
                    val lng = partes[3].toDoubleOrNull()
                    if (nombre.isNotBlank() && lat != null && lng != null) {
                        lista.add(
                            Lugar(
                                nombre = nombre,
                                descripcion = desc,
                                latitud = lat,
                                longitud = lng,
                                esPersonalizado = true
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    /**
     * Guarda un nuevo lugar personalizado en el almacenamiento privado de la app.
     */
    fun guardarLugar(context: Context, lugar: Lugar): Boolean {
        return try {
            val file = archivo(context)
            val linea = "${lugar.nombre.trim()}|${lugar.descripcion.trim()}|${lugar.latitud}|${lugar.longitud}\n"
            file.appendText(linea, Charsets.UTF_8)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Calcula la distancia en metros entre dos puntos geográficos (Apartado 4.8).
     */
    fun calcularDistancia(origen: LatLng, destino: LatLng): Float {
        val resultados = FloatArray(1)
        Location.distanceBetween(
            origen.latitude, origen.longitude,
            destino.latitude, destino.longitude,
            resultados
        )
        return resultados[0]
    }

    /**
     * Formatea una distancia en metros o kilómetros según su magnitud.
     */
    fun formatearDistancia(metros: Float): String {
        return if (metros < 1000f) {
            String.format(Locale.getDefault(), "%.0f m", metros)
        } else {
            String.format(Locale.getDefault(), "%.2f km", metros / 1000f)
        }
    }

    /**
     * Encuentra el punto más alejado del centro del municipio para definir el radio del círculo (Apartado 4.9).
     */
    fun obtenerPuntoMasAlejado(centro: LatLng, lugares: List<Lugar>): Pair<Lugar?, Float> {
        var masAlejado: Lugar? = null
        var maxDistancia = 0f

        for (lugar in lugares) {
            val dist = calcularDistancia(centro, lugar.coordenadas)
            if (dist > maxDistancia) {
                maxDistancia = dist
                masAlejado = lugar
            }
        }
        return Pair(masAlejado, maxDistancia)
    }
}
