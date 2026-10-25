package com.hache.hachesquizz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para validar las funciones clave de la Práctica 4.
 */
class Practica4UnitTest {

    @Test
    fun testFormatoDistancias() {
        assertEquals("450 m", RepositorioLugares.formatearDistancia(450f))
        assertEquals("999 m", RepositorioLugares.formatearDistancia(999f))
        val distKm = RepositorioLugares.formatearDistancia(38400f)
        assertTrue(distKm.contains("38.40") || distKm.contains("38,40"))
        assertTrue(distKm.endsWith("km"))
    }

    @Test
    fun testCoordenadasFormateadas() {
        val lugar = Lugar(
            nombre = "CIFP César Manrique",
            descripcion = "Centro educativo",
            latitud = 28.45574,
            longitud = -16.28270
        )
        assertEquals("28.45574, -16.28270", lugar.coordenadasFormateadas())
    }

    @Test
    fun testUsuariosLogin() {
        val credenciales = Autenticacion.usuariosValidos
        assertEquals("pgl2026", credenciales["alumno"])
        assertEquals("cesarmanrique", credenciales["profesor"])
        assertEquals("dam2026", credenciales["hache"])
        assertTrue(credenciales.size >= 3)

        // Verificación de robustez ante teclados móviles
        assertTrue(Autenticacion.validar("alumno", "pgl2026"))
        assertTrue(Autenticacion.validar("alumno ", "pgl2026 "))
        assertTrue(Autenticacion.validar("Alumno", "Pgl2026"))
        assertTrue(Autenticacion.validar("Profesor", "Cesarmanrique"))
        assertTrue(Autenticacion.validar("HACHE", "Dam2026 "))
    }
}
