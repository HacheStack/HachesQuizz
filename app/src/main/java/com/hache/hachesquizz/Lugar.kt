package com.hache.hachesquizz

import com.google.android.gms.maps.model.LatLng

/**
 * Práctica 4 - Modelo de datos para las localizaciones geográficas.
 */
data class Lugar(
    val nombre: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val esPersonalizado: Boolean = false
) {
    val coordenadas: LatLng
        get() = LatLng(latitud, longitud)

    fun coordenadasFormateadas(): String {
        return String.format(java.util.Locale.US, "%.5f, %.5f", latitud, longitud)
    }
}
