package com.hache.hachesquizz

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Práctica 4 - Apartados 4.2 y 4.3: Pantalla de Bienvenida y Música de Fondo.
 * Presenta el municipio seleccionado (Santa Cruz de Tenerife), texto descriptivo
 * con desplazamiento vertical (ScrollView) y accesos directos a todas las secciones.
 */
class BienvenidaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nombreUsuario = intent.getStringExtra("usuario_actual") ?: "Estudiante"

        setContent {
            val context = LocalContext.current
            var modoOscuro by rememberSaveable {
                mutableStateOf(Configuracion.esOscuro(this@BienvenidaActivity))
            }

            var musicaActiva by rememberSaveable {
                mutableStateOf(MusicaFondo.estaHabilitada)
            }

            TemaHaches(oscuro = modoOscuro) {
                PantallaBienvenida(
                    usuario = nombreUsuario,
                    modoOscuro = modoOscuro,
                    cambiarTema = { nuevo ->
                        modoOscuro = nuevo
                        Configuracion.guardarTema(this@BienvenidaActivity, nuevo)
                    },
                    musicaActiva = musicaActiva,
                    onToggleMusica = {
                        val nuevoEstado = MusicaFondo.alternar(context)
                        musicaActiva = nuevoEstado
                    },
                    irAJuego = {
                        startActivity(Intent(this@BienvenidaActivity, MenuPrincipal::class.java))
                    },
                    irAMapas = {
                        startActivity(Intent(this@BienvenidaActivity, MapaActivity::class.java))
                    },
                    irAGaleria = {
                        startActivity(Intent(this@BienvenidaActivity, GaleriaActivity::class.java))
                    },
                    cerrarSesion = {
                        MusicaFondo.pausar()
                        startActivity(Intent(this@BienvenidaActivity, LoginActivity::class.java))
                        finish()
                    }
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
fun PantallaBienvenida(
    usuario: String,
    modoOscuro: Boolean,
    cambiarTema: (Boolean) -> Unit,
    musicaActiva: Boolean,
    onToggleMusica: () -> Unit,
    irAJuego: () -> Unit,
    irAMapas: () -> Unit,
    irAGaleria: () -> Unit,
    cerrarSesion: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fila superior: Modo Oscuro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Modo oscuro",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = modoOscuro,
                    onCheckedChange = cambiarTema
                )
            }

            Spacer(Modifier.height(8.dp))

            // Saludo personalizado al usuario autenticado
            Text(
                text = "¡Bienvenido/a, $usuario!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Santa Cruz de Tenerife & CIFP César Manrique",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Imagen representativa del municipio (Tenerife)
            Image(
                painter = painterResource(R.drawable.banner_tenerife),
                contentDescription = "Panorámica de Santa Cruz de Tenerife y el Teide",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.height(16.dp))

            // Control de Música Ambiental (Apartado 4.3)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Música ambiental",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (musicaActiva) "Reproduciendo en segundo plano" else "Música desactivada",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = musicaActiva,
                        onCheckedChange = { onToggleMusica() }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Texto descriptivo con extensión suficiente para requerir ScrollView (Apartado 4.2)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Descubre Santa Cruz y Tenerife",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Santa Cruz de Tenerife es la capital de la isla de Tenerife y capital compartida de la comunidad autónoma de Canarias. Una urbe costera y cosmopolita resguardada por el Macizo de Anaga y asomada al océano Atlántico, célebre por su arquitectura de vanguardia, su patrimonio histórico y su afamado Carnaval declarado Fiesta de Interés Turístico Internacional.\n\n" +
                                "Entre sus atractivos indispensables destacan la emblemática Plaza de España con su majestuoso lago de agua marina, el pulmón verde del Parque García Sanabria repleto de flora exótica y esculturas contemporáneas, y el histórico núcleo de San Cristóbal de La Laguna, ciudad Patrimonio de la Humanidad por la UNESCO.\n\n" +
                                "En el ámbito educativo y tecnológico destaca el CIFP César Manrique, centro de referencia de Formación Profesional en Canarias donde estudiantes de Desarrollo de Aplicaciones Multiplataforma (DAM) impulsan proyectos de innovación digital como esta misma aplicación.\n\n" +
                                "Culminando la isla, el imponente Parque Nacional del Teide corona el paisaje canario a más de 3.700 metros de altura, representando una de las maravillas geológicas más espectaculares del planeta.",
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botones de acción principales
            Button(
                onClick = irAMapas,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("EXPLORADOR DE MAPAS (GOOGLE MAPS)", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = irAGaleria,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("GALERÍA DE FOTOS DE TENERIFE", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = irAJuego,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("JUEGO DE PREGUNTAS (QUIZZ)", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            BotonSecundario(
                texto = "CERRAR SESIÓN",
                onClick = cerrarSesion
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}
