package com.hache.hachesquizz

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Práctica 4 - Apartado 4.10: Incorporación de puntos de interés personalizados.
 * Permite seleccionar una coordenada directamente en el mapa (onMapClick),
 * ingresar nombre y descripción, y almacenar el punto de forma persistente.
 */
class AgregarLugarActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val modoOscuro = Configuracion.esOscuro(this)

            TemaHaches(oscuro = modoOscuro) {
                PantallaAgregarLugar(
                    onGuardadoExitoso = {
                        setResult(RESULT_OK)
                        finish()
                    },
                    volver = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MusicaFondo.reanudar()
    }
}

@Composable
fun PantallaAgregarLugar(
    onGuardadoExitoso: () -> Unit,
    volver: () -> Unit
) {
    val context = LocalContext.current
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var coordenadaSeleccionada by remember { mutableStateOf<LatLng?>(null) }
    var mensajeError by rememberSaveable { mutableStateOf<String?>(null) }

    var googleMapInstancia by remember { mutableStateOf<GoogleMap?>(null) }
    var marcadorActual by remember { mutableStateOf<Marker?>(null) }

    val mapView = recordarMapViewConCicloDeVida()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hache's Quizz",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "AÑADIR NUEVO PUNTO DE INTERÉS",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (BuildConfig.MAPS_API_KEY.isBlank()) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Clave de Google Maps no configurada en local.properties. Puedes seleccionar coordenadas pulsando el mapa, pero para ver las calles introduce tu MAPS_API_KEY.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Mapa interactivo con onMapClick
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AndroidView(
                    factory = {
                        mapView.apply {
                            getMapAsync { gMap ->
                                googleMapInstancia = gMap
                                gMap.uiSettings.isZoomControlsEnabled = true
                                gMap.uiSettings.isCompassEnabled = true
                                // Centrar en Santa Cruz de Tenerife
                                gMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(RepositorioLugares.CENTRO_MUNICIPIO, 12f)
                                )

                                // Apartado 4.10: Evento onMapClick
                                gMap.setOnMapClickListener { posicion ->
                                    marcadorActual?.remove()
                                    marcadorActual = gMap.addMarker(
                                        MarkerOptions()
                                            .position(posicion)
                                            .title(if (nombre.isNotBlank()) nombre else "Punto seleccionado")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    )
                                    marcadorActual?.showInfoWindow()
                                    coordenadaSeleccionada = posicion
                                    mensajeError = null
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                )
            }

            Spacer(Modifier.height(8.dp))

            // Indicador de coordenadas seleccionadas
            if (coordenadaSeleccionada != null) {
                Text(
                    text = String.format(
                        java.util.Locale.US,
                        "📍 Coordenadas: %.5f, %.5f",
                        coordenadaSeleccionada!!.latitude,
                        coordenadaSeleccionada!!.longitude
                    ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Text(
                    text = "Toca cualquier punto del mapa para colocar el marcador",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(12.dp))

            // Campo Nombre del lugar
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    if (mensajeError != null) mensajeError = null
                    marcadorActual?.title = if (it.isNotBlank()) it else "Punto seleccionado"
                },
                label = { Text("Nombre del lugar *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // Campo Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // Mensaje de error / validación
            AnimatedVisibility(visible = mensajeError != null) {
                Text(
                    text = mensajeError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Botón Guardar Localización
            Button(
                onClick = {
                    val coords = coordenadaSeleccionada
                    if (coords == null) {
                        mensajeError = "Debes pulsar sobre el mapa para seleccionar una ubicación."
                    } else if (nombre.isBlank()) {
                        mensajeError = "Por favor, introduce un nombre para el lugar."
                    } else {
                        val nuevoLugar = Lugar(
                            nombre = nombre.trim(),
                            descripcion = descripcion.trim().ifEmpty { "Punto de interés personalizado" },
                            latitud = coords.latitude,
                            longitud = coords.longitude,
                            esPersonalizado = true
                        )
                        val exito = RepositorioLugares.guardarLugar(context, nuevoLugar)
                        if (exito) {
                            Toast.makeText(context, "¡Lugar guardado correctamente!", Toast.LENGTH_SHORT).show()
                            onGuardadoExitoso()
                        } else {
                            mensajeError = "Error al guardar el lugar en el archivo local."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("GUARDAR LOCALIZACIÓN", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            // Botón Eliminar Selección Actual (descarta marcador pendiente)
            OutlinedButton(
                onClick = {
                    marcadorActual?.remove()
                    marcadorActual = null
                    coordenadaSeleccionada = null
                    mensajeError = null
                    Toast.makeText(context, "Selección descartada", Toast.LENGTH_SHORT).show()
                },
                enabled = coordenadaSeleccionada != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ELIMINAR SELECCIÓN ACTUAL")
            }

            Spacer(Modifier.height(12.dp))

            BotonSecundario(
                texto = "CANCELAR Y VOLVER",
                onClick = volver
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
