
package com.hache.hachesquizz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Datos de las estadísticas
data class DatosEstadisticas(
    val aciertos: Int,
    val fallos: Int,
    val clics: Int
)

// Objeto encargado de guardar las estadísticas
object Estadisticas {

    private const val ARCHIVO = "estadisticas"

    // REGISTRAR UNA RESPUESTA
    fun registrarRespuesta(context: Context, acierto: Boolean) {

        val prefs = context.getSharedPreferences(
            ARCHIVO, Context.MODE_PRIVATE
        )

        val clave = if (acierto) "aciertos" else "fallos"

        prefs.edit()
            .putInt(clave, prefs.getInt(clave, 0) + 1)
            .putInt("clics", prefs.getInt("clics", 0) + 1)
            .apply()
    }

    // REGISTRAR UN CLIC
    fun registrarClick(context: Context) {

        val prefs = context.getSharedPreferences(
            ARCHIVO, Context.MODE_PRIVATE
        )

        prefs.edit()
            .putInt("clics", prefs.getInt("clics", 0) + 1)
            .apply()
    }

    // REGISTRAR UN FALLO POR TIEMPO AGOTADO
    fun registrarTiempoAgotado(context: Context) {

        val prefs = context.getSharedPreferences(
            ARCHIVO, Context.MODE_PRIVATE
        )

        val fallosActuales = prefs.getInt("fallos", 0)

        prefs.edit()
            .putInt("fallos", fallosActuales + 1)
            .apply()
    }

    // OBTENER LAS ESTADÍSTICAS
    fun obtenerDatos(context: Context): DatosEstadisticas {

        val prefs = context.getSharedPreferences(
            ARCHIVO, Context.MODE_PRIVATE
        )

        return DatosEstadisticas(
            aciertos = prefs.getInt("aciertos", 0),
            fallos = prefs.getInt("fallos", 0),
            clics = prefs.getInt("clics", 0)
        )
    }
}

// ACTIVIDAD DE RESULTADOS
class Resultados : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TemaHaches(
                oscuro = Configuracion.esOscuro(this)
            ) {
                PantallaResultados(
                    volver = { finish() }
                )
            }
        }
    }
}

@Composable
fun PantallaResultados(volver: () -> Unit) {

    val context = LocalContext.current
    val datos = Estadisticas.obtenerDatos(context)

    val totalRespuestas = datos.aciertos + datos.fallos

    val porcentajeAciertos = if (totalRespuestas > 0) {
        datos.aciertos * 100f / totalRespuestas
    } else {
        0f
    }

    val porcentajeFallos = if (totalRespuestas > 0) {
        datos.fallos * 100f / totalRespuestas
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // TÍTULO PRINCIPAL
        Text(
            text = "Hache's Quizz",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(12.dp))

        // SUBTÍTULO
        Text(
            text = "ESTADÍSTICAS",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(32.dp))

        // ACIERTOS
        Text(
            text = "Aciertos: ${datos.aciertos}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        // FALLOS
        Text(
            text = "Fallos: ${datos.fallos}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        // PORCENTAJE DE ACIERTOS
        Text(
            text = "Porcentaje de aciertos: " +
                    String.format("%.1f", porcentajeAciertos) + "%",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        // PORCENTAJE DE FALLOS
        Text(
            text = "Porcentaje de fallos: " +
                    String.format("%.1f", porcentajeFallos) + "%",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        // CLICS TOTALES
        Text(
            text = "Clics contabilizados: ${datos.clics}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(32.dp))

        // BOTÓN VOLVER
        Button(
            onClick = volver,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text("VOLVER")
        }
    }
}
