package com.hache.hachesquizz

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

/**
 * Práctica 4 - Apartados 4.5, 4.6, 4.7, 4.8, 4.9 y 4.11:
 * Actividad principal de mapas con Google Maps, selección con Spinner/ExposedDropdown,
 * animaciones con CameraPosition, trazado con Polyline, cálculo de distancias y círculo de cobertura (Circle).
 */
class MapaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val modoOscuro = Configuracion.esOscuro(this)

            TemaHaches(oscuro = modoOscuro) {
                PantallaMapa(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(
    volver: () -> Unit
) {
    val context = LocalContext.current
    var lugares by remember { mutableStateOf(RepositorioLugares.cargarTodos(context)) }
    var indiceSeleccionado by rememberSaveable { mutableIntStateOf(0) }
    var dropdownExpandido by remember { mutableStateOf(false) }

    var googleMapInstancia by remember { mutableStateOf<GoogleMap?>(null) }
    val marcadoresMap = remember { mutableStateMapOf<Int, Marker>() }
    var polylineRuta by remember { mutableStateOf<Polyline?>(null) }
    var circuloArea by remember { mutableStateOf<Circle?>(null) }

    var rutaVisible by rememberSaveable { mutableStateOf(false) }
    var panelDetallesVisible by rememberSaveable { mutableStateOf(false) }

    // Launcher para agregar nuevo lugar y refrescar la lista
    val launcherAgregarLugar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val listaActualizada = RepositorioLugares.cargarTodos(context)
            lugares = listaActualizada
        }
    }

    val mapView = recordarMapViewConCicloDeVida()

    // Función auxiliar para redibujar marcadores, círculo y ruta
    val actualizarElementosEnMapa: (GoogleMap) -> Unit = { gMap ->
        // Limpiar elementos previos
        gMap.clear()
        marcadoresMap.clear()
        polylineRuta = null
        circuloArea = null

        // 1. Agregar marcadores personalizados para cada lugar (Apartado 4.5)
        lugares.forEachIndexed { index, lugar ->
            val tonoMarcador = when {
                lugar.esPersonalizado -> BitmapDescriptorFactory.HUE_AZURE
                index == 0 -> BitmapDescriptorFactory.HUE_ORANGE // César Manrique
                index == 1 -> BitmapDescriptorFactory.HUE_ROSE   // Plaza de España
                index == 2 -> BitmapDescriptorFactory.HUE_GREEN  // Parque García Sanabria
                index == 3 -> BitmapDescriptorFactory.HUE_VIOLET // Catedral La Laguna
                else -> BitmapDescriptorFactory.HUE_RED          // Teide
            }

            val marker = gMap.addMarker(
                MarkerOptions()
                    .position(lugar.coordenadas)
                    .title(lugar.nombre)
                    .snippet("Lat: ${String.format(java.util.Locale.US, "%.4f", lugar.latitud)} | Lng: ${String.format(java.util.Locale.US, "%.4f", lugar.longitud)}")
                    .icon(BitmapDescriptorFactory.defaultMarker(tonoMarcador))
            )
            marker?.let {
                it.tag = index
                marcadoresMap[index] = it
            }
        }

        // 2. Representación de un área geográfica mediante Circle (Apartado 4.9)
        val (lugarMasAlejado, radioMaximo) = RepositorioLugares.obtenerPuntoMasAlejado(
            RepositorioLugares.CENTRO_MUNICIPIO,
            lugares
        )
        if (radioMaximo > 0f) {
            circuloArea = gMap.addCircle(
                CircleOptions()
                    .center(RepositorioLugares.CENTRO_MUNICIPIO)
                    .radius(radioMaximo.toDouble())
                    .strokeColor(AndroidColor.parseColor("#FFFF2E93"))
                    .strokeWidth(5f)
                    .fillColor(AndroidColor.parseColor("#22FF2E93")) // translúcido para visualizar el mapa
            )
        }

        // 3. Representación de recorridos mediante Polyline (Apartado 4.7)
        if (rutaVisible && lugares.size >= 2) {
            val polylineOptions = PolylineOptions()
                .color(AndroidColor.parseColor("#FFFFE600"))
                .width(10f)
                .geodesic(true)

            lugares.forEach { polylineOptions.add(it.coordenadas) }
            polylineRuta = gMap.addPolyline(polylineOptions)
        }
    }

    // Efecto para sincronizar el mapa cuando cambian los lugares o la visibilidad de la ruta
    LaunchedEffect(lugares, rutaVisible, googleMapInstancia) {
        googleMapInstancia?.let { gMap ->
            actualizarElementosEnMapa(gMap)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título de la actividad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hache's Quizz",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "EXPLORADOR DE MAPAS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(
                    onClick = {
                        val intent = Intent(context, AgregarLugarActivity::class.java)
                        launcherAgregarLugar.launch(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLocationAlt,
                        contentDescription = "Añadir punto de interés",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Aviso si la API Key de Google Maps no está configurada en local.properties
            if (BuildConfig.MAPS_API_KEY.isBlank()) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Clave de Google Maps no configurada en local.properties. Los marcadores y rutas se calculan, pero para ver las calles introduce tu MAPS_API_KEY de Google Cloud.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Apartado 4.5: Spinner / Selector de localizaciones
            ExposedDropdownMenuBox(
                expanded = dropdownExpandido,
                onExpandedChange = { dropdownExpandido = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = lugares.getOrNull(indiceSeleccionado)?.nombre ?: "Seleccionar lugar",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lugar seleccionado (Spinner)") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpandido)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpandido,
                    onDismissRequest = { dropdownExpandido = false }
                ) {
                    lugares.forEachIndexed { index, lugar ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = lugar.nombre + if (lugar.esPersonalizado) " (Creado)" else "",
                                    fontWeight = if (index == indiceSeleccionado) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                indiceSeleccionado = index
                                dropdownExpandido = false

                                // Apartado 4.6: Animación fluida de cámara con CameraPosition
                                googleMapInstancia?.let { gMap ->
                                    val camPos = CameraPosition.Builder()
                                        .target(lugar.coordenadas)
                                        .zoom(15.5f)
                                        .tilt(25f)
                                        .bearing(0f)
                                        .build()

                                    gMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos), 1200, null)
                                    marcadoresMap[index]?.showInfoWindow()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Contenedor de Google Maps interactivo
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = {
                        mapView.apply {
                            getMapAsync { gMap ->
                                googleMapInstancia = gMap
                                gMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                                gMap.uiSettings.isZoomControlsEnabled = true
                                gMap.uiSettings.isCompassEnabled = true
                                gMap.uiSettings.isMapToolbarEnabled = true

                                // Vista inicial centrada en el municipio (Santa Cruz de Tenerife)
                                gMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(RepositorioLugares.CENTRO_MUNICIPIO, 11.2f)
                                )

                                // Click en marcadores: muestra ventana informativa (Apartado 4.5)
                                gMap.setOnMarkerClickListener { marker ->
                                    val index = marker.tag as? Int
                                    if (index != null && index in lugares.indices) {
                                        indiceSeleccionado = index
                                    }
                                    marker.showInfoWindow()
                                    true
                                }

                                actualizarElementosEnMapa(gMap)
                            }
                        }
                    },
                    update = {
                        googleMapInstancia?.let { gMap ->
                            actualizarElementosEnMapa(gMap)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                )
            }

            Spacer(Modifier.height(10.dp))

            // Barra de acciones: Botón RUTA (Apartado 4.7) y Ver Distancias (Apartado 4.8)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        rutaVisible = !rutaVisible
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (rutaVisible) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = "Ruta",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (rutaVisible) "OCULTAR RUTA" else "VER RUTA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { panelDetallesVisible = !panelDetallesVisible },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (panelDetallesVisible) "CERRAR INFO" else "DISTANCIAS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Panel de información de cálculo de distancias (Apartado 4.8)
            AnimatedVisibility(visible = panelDetallesVisible) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                        .padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "📏 Cálculo de distancias (Location.distanceBetween)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(4.dp))

                        var distanciaTotal = 0f
                        for (i in 0 until lugares.size - 1) {
                            val origen = lugares[i]
                            val destino = lugares[i + 1]
                            val tramo = RepositorioLugares.calcularDistancia(origen.coordenadas, destino.coordenadas)
                            distanciaTotal += tramo
                            Text(
                                text = "• Tramo ${i + 1}: ${origen.nombre} ➔ ${destino.nombre}: ${RepositorioLugares.formatearDistancia(tramo)}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp))

                        Text(
                            text = "Distancia Total del Recorrido: ${RepositorioLugares.formatearDistancia(distanciaTotal)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        val (masLejano, distRadio) = RepositorioLugares.obtenerPuntoMasAlejado(
                            RepositorioLugares.CENTRO_MUNICIPIO,
                            lugares
                        )
                        if (masLejano != null) {
                            Text(
                                text = "Radio del Área Municipal (Circle): ${RepositorioLugares.formatearDistancia(distRadio)} (hasta ${masLejano.nombre})",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            BotonSecundario(
                texto = "VOLVER AL MENÚ",
                onClick = volver
            )
        }
    }
}
