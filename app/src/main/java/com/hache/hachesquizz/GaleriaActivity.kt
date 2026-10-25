package com.hache.hachesquizz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modelo de datos para las imágenes de la galería municipal.
 */
data class ItemGaleria(
    val titulo: String,
    val descripcion: String,
    @DrawableRes val imagenResId: Int
)

/**
 * Práctica 4 - Apartado 4.4: Galería de imágenes.
 * Muestra una colección interactiva de fotografías de Santa Cruz de Tenerife y sus puntos de interés,
 * con navegación anterior/siguiente, descripciones sincronizadas e imágenes sin deformación.
 */
class GaleriaActivity : ComponentActivity() {

    private val coleccionFotos = listOf(
        ItemGaleria(
            titulo = "CIFP César Manrique",
            descripcion = "Centro Integrado de Formación Profesional situado en el barrio de Ofra (Santa Cruz de Tenerife). Referente autonómico en las familias profesionales de Informática y Comunicaciones, e Imagen y Sonido.",
            imagenResId = R.drawable.foto_cesar_manrique
        ),
        ItemGaleria(
            titulo = "Plaza de España",
            descripcion = "Centro neurálgico y punto de encuentro de Santa Cruz de Tenerife. Diseñada por los arquitectos Herzog & de Meuron, cuenta con un lago artificial de agua marina que cuenta con un géiser central frente al puerto.",
            imagenResId = R.drawable.foto_plaza_espana
        ),
        ItemGaleria(
            titulo = "Parque García Sanabria",
            descripcion = "El parque urbano más emblemático de la capital tinerfeña y un auténtico jardín botánico. Es célebre por su colorido Reloj de Flores permanente y sus numerosas esculturas de arte moderno.",
            imagenResId = R.drawable.foto_parque_garcia_sanabria
        ),
        ItemGaleria(
            titulo = "Catedral de San Cristóbal de La Laguna",
            descripcion = "Templo neoclásico catedralicio sede de la Diócesis de Tenerife. Emplazado en el casco histórico de San Cristóbal de La Laguna, ciudad declarada Patrimonio de la Humanidad por la UNESCO.",
            imagenResId = R.drawable.foto_catedral_laguna
        ),
        ItemGaleria(
            titulo = "Parque Nacional del Teide",
            descripcion = "El centro de visitantes de El Portillo es la puerta de entrada al Parque Nacional del Teide. El majestuoso volcán, con sus 3.718 metros, es el pico más alto de España y un entorno volcánico único en el mundo.",
            imagenResId = R.drawable.foto_teide
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val modoOscuro = Configuracion.esOscuro(this)

            TemaHaches(oscuro = modoOscuro) {
                PantallaGaleria(
                    fotos = coleccionFotos,
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
fun PantallaGaleria(
    fotos: List<ItemGaleria>,
    volver: () -> Unit
) {
    var indiceActual by rememberSaveable { mutableIntStateOf(0) }
    val total = fotos.size
    val fotoActual = fotos[indiceActual]

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
            // Título de la sección
            Text(
                text = "Hache's Quizz",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "GALERÍA DE TENERIFE",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(16.dp))

            // Indicador de foto actual
            Text(
                text = "Fotografía ${indiceActual + 1} de $total",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            // Contenedor animado de la imagen y texto
            AnimatedContent(
                targetState = fotoActual,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut()
                    )
                },
                label = "transicion_foto"
            ) { targetFoto ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Imagen adaptada sin deformarse (ContentScale.Fit dentro de marco con relación de aspecto)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(targetFoto.imagenResId),
                                contentDescription = targetFoto.titulo,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Tarjeta con Título y Descripción asociada
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = targetFoto.titulo,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = targetFoto.descripcion,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Justify
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Controles de navegación: Anterior y Siguiente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (indiceActual > 0) {
                            indiceActual--
                        } else {
                            // Salto cíclico al final
                            indiceActual = total - 1
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Anterior",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("ANTERIOR", fontSize = 13.sp)
                }

                Spacer(Modifier.width(14.dp))

                Button(
                    onClick = {
                        if (indiceActual < total - 1) {
                            indiceActual++
                        } else {
                            // Salto cíclico al principio
                            indiceActual = 0
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("SIGUIENTE", fontSize = 13.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Siguiente",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Botón para volver a la bienvenida
            BotonSecundario(
                texto = "VOLVER AL MENÚ",
                onClick = volver
            )
        }
    }
}
