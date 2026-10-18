package com.hache.hachesquizz

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Configuracion {
    private const val ARCHIVO = "ajustes"
    private const val CLAVE = "modo_oscuro"

    fun esOscuro(context: Context): Boolean {
        val prefs = context.getSharedPreferences(
            ARCHIVO, Context.MODE_PRIVATE
        )
        return prefs.getBoolean(CLAVE, true)
    }

    fun guardarTema(context: Context, oscuro: Boolean) {
        context.getSharedPreferences(
            ARCHIVO, Context.MODE_PRIVATE
        ).edit().putBoolean(CLAVE, oscuro).apply()
    }
}


@Composable
fun TemaHaches(
    oscuro: Boolean,
    contenido: @Composable () -> Unit
) {

    val colores = if (oscuro) {

        // MODO OSCURO
        darkColorScheme(
            // Botones principales
            primary = Color(0xFFFF2E93),
            onPrimary = Color(0xFF1E1B2E),

            // Titulo Hache's Quizz
            secondary = Color(0xFFFFE600),
            onSecondary = Color(0xFF1E1B2E),

            // Fondo de las pantallas
            background = Color(0xFF1E1B2E),
            onBackground = Color(0xFFE2E8F0),

            // Superficies
            surface = Color(0xFF1E1B2E),
            onSurface = Color(0xFFE2E8F0),

            // Boton VOLVER
            tertiary = Color(0xFF312B47),
            onTertiary = Color(0xFFE2E8F0)
        )

    } else {

        // MODO CLARO
        lightColorScheme(
            // Botones principales
            primary = Color(0xFFFF2E93),
            onPrimary = Color(0xFF1E1B2E),

            // Titulo
            secondary = Color(0xFF5B3195),
            onSecondary = Color.White,

            // Fondo de las pantallas
            background = Color(0xFFF8F5FC),
            onBackground = Color(0xFF1E1B2E),

            // Superficies
            surface = Color(0xFFF8F5FC),
            onSurface = Color(0xFF1E1B2E),

            // Boton VOLVER
            tertiary = Color(0xFFE7DFEF),
            onTertiary = Color(0xFF1E1B2E)
        )
    }

    MaterialTheme(
        colorScheme = colores,
        content = contenido
    )
}

