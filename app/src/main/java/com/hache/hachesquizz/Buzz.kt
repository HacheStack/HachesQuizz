package com.hache.hachesquizz

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

class Buzz : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperar el modo de preguntas
        val modoNinos = intent.getBooleanExtra(
            "modo_ninos",
            false
        )

        // Recuperar el tiempo elegido
        val segundosPorPregunta = intent.getIntExtra(
            "segundos_por_pregunta",
            0
        )

        setContent {
            TemaHaches(
                oscuro = Configuracion.esOscuro(this)
            ) {
                PantallaJuego(
                    modoNinos = modoNinos,
                    segundosPorPregunta = segundosPorPregunta
                )
            }
        }
    }
}

@Composable
fun PantallaJuego(
    modoNinos: Boolean,
    segundosPorPregunta: Int
) {

    val context = LocalContext.current

    // PREGUNTAS MODO CLASICO
    val preguntasClasico: ArrayList<Pregunta> = remember {
        arrayListOf(
            Pregunta("¿Android está basado en Linux?", true),
            Pregunta("¿Java fue creado por Microsoft?", false),
            Pregunta("¿Kotlin es un lenguaje de programación?", true),
            Pregunta("¿HTML es una base de datos?", false),
            Pregunta("¿Python permite programar con objetos?", true)
        )
    }

    // PREGUNTAS MODO NIÑOS
    val preguntasNinos: ArrayList<Pregunta> = remember {
        arrayListOf(
            Pregunta("¿Leonardo da Vinci pintó la Mona Lisa?", true),
            Pregunta("¿El Amazonas es el río más caudaloso del mundo?", true),
            Pregunta("¿Mercurio es el planeta más caliente?", false),
            Pregunta("¿El Principito vive en un planeta muy pequeño?", true),
            Pregunta("¿El Sol es un planeta?", false)
        )
    }


    // Cargar preguntas desde el fichero de texto
    val preguntas: ArrayList<Pregunta> = remember(modoNinos) {
        RepositorioPreguntas.cargar(context, modoNinos)
    }

    // Pregunta actual
    var indicePregunta by rememberSaveable {
        mutableIntStateOf(0)
    }

    // Respuesta seleccionada
    var respuestaSeleccionada by rememberSaveable {
        mutableStateOf<Boolean?>(null)
    }

    // Tiempo que queda
    var tiempoRestante by rememberSaveable {
        mutableIntStateOf(segundosPorPregunta)
    }

    // Indica si ha terminado el tiempo
    var tiempoAgotado by rememberSaveable {
        mutableStateOf(false)
    }

    // Identificador del turno actual
    var turno by rememberSaveable {
        mutableIntStateOf(0)
    }

    val preguntaActual = preguntas[indicePregunta]

    // TEMPORIZADOR
    DisposableEffect(
        turno,
        segundosPorPregunta,
        respuestaSeleccionada,
        tiempoAgotado
    ) {

        val temporizador =
            if (
                segundosPorPregunta > 0 &&
                respuestaSeleccionada == null &&
                !tiempoAgotado &&
                tiempoRestante > 0
            ) {

                object : CountDownTimer(
                    tiempoRestante * 1000L,
                    1000L
                ) {

                    override fun onTick(
                        millisUntilFinished: Long
                    ) {
                        tiempoRestante =
                            ((millisUntilFinished + 999L) / 1000L).toInt()
                    }

                    override fun onFinish() {

                        if (
                            respuestaSeleccionada == null &&
                            !tiempoAgotado
                        ) {
                            tiempoRestante = 0
                            tiempoAgotado = true

                            Estadisticas.registrarTiempoAgotado(
                                context
                            )
                        }
                    }

                }.start()

            } else {
                null
            }

        // Cancelar el contador anterior
        onDispose {
            temporizador?.cancel()
        }
    }

    // FUNCION PARA CAMBIAR DE PREGUNTA
    fun cambiarPregunta(nuevoIndice: Int) {

        indicePregunta = nuevoIndice
        respuestaSeleccionada = null
        tiempoAgotado = false
        tiempoRestante = segundosPorPregunta

        // Reiniciar el temporizador
        turno++
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

        // NOMBRE DEL MODO
        Text(
            text = if (modoNinos) {
                "MODO NIÑ@S"
            } else {
                "MODO CLÁSICO"
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(12.dp))

        // NUMERO DE PREGUNTA
        Text(
            text = "Pregunta ${indicePregunta + 1} de ${preguntas.size}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        // MOSTRAR TIEMPO SI ES CONTRARRELOJ
        if (segundosPorPregunta > 0) {

            Text(
                text = "Tiempo restante: $tiempoRestante s",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (tiempoRestante <= 3) {
                    Color(0xFFFF5252)
                } else {
                    MaterialTheme.colorScheme.secondary
                }
            )

            Spacer(Modifier.height(16.dp))
        }

        // PREGUNTA
        Text(
            text = preguntaActual.texto,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(32.dp))

        // BOTONES VERDADERO Y FALSO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // VERDADERO
            BotonRespuesta(
                texto = "VERDADERO",
                valor = true,
                respuestaCorrecta = preguntaActual.resultado,
                respondida = respuestaSeleccionada != null ||
                        tiempoAgotado,
                modifier = Modifier.weight(1f),
                onClick = {

                    if (
                        respuestaSeleccionada == null &&
                        !tiempoAgotado
                    ) {

                        Estadisticas.registrarRespuesta(
                            context,
                            true == preguntaActual.resultado
                        )

                        respuestaSeleccionada = true
                    }
                }
            )

            // FALSO
            BotonRespuesta(
                texto = "FALSO",
                valor = false,
                respuestaCorrecta = preguntaActual.resultado,
                respondida = respuestaSeleccionada != null ||
                        tiempoAgotado,
                modifier = Modifier.weight(1f),
                onClick = {

                    if (
                        respuestaSeleccionada == null &&
                        !tiempoAgotado
                    ) {

                        Estadisticas.registrarRespuesta(
                            context,
                            false == preguntaActual.resultado
                        )

                        respuestaSeleccionada = false
                    }
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        // RESULTADO DE LA RESPUESTA
        if (tiempoAgotado) {

            Text(
                text = "¡Tiempo agotado! Respuesta incorrecta",
                color = Color(0xFFFF5252),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

        } else if (respuestaSeleccionada != null) {

            val acierto =
                respuestaSeleccionada == preguntaActual.resultado

            Text(
                text = if (acierto) {
                    "¡Correcto!"
                } else {
                    "Incorrecto"
                },
                color = if (acierto) {
                    Color(0xFF4CAF50)
                } else {
                    Color(0xFFFF5252)
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(32.dp))

        // BOTONES ANTERIOR Y SIGUIENTE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ANTERIOR
            Button(
                onClick = {
                    Estadisticas.registrarClick(context)

                    val anterior =
                        (indicePregunta - 1 + preguntas.size) %
                                preguntas.size

                    cambiarPregunta(anterior)
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {

                Icon(
                    painter = painterResource(
                        R.drawable.ic_arrow_back
                    ),
                    contentDescription = "Pregunta anterior",
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(4.dp))

                Text("ANTERIOR", fontSize = 12.sp)
            }

            // SIGUIENTE
            Button(
                onClick = {
                    Estadisticas.registrarClick(context)

                    val siguiente =
                        (indicePregunta + 1) % preguntas.size

                    cambiarPregunta(siguiente)
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {

                Text("SIGUIENTE", fontSize = 12.sp)

                Spacer(Modifier.width(4.dp))

                Icon(
                    painter = painterResource(
                        R.drawable.ic_arrow_forward
                    ),
                    contentDescription = "Siguiente pregunta",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // BOTON ALEATORIA
        Button(
            onClick = {
                Estadisticas.registrarClick(context)

                val aleatoria = Random.nextInt(preguntas.size)

                cambiarPregunta(aleatoria)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text("ALEATORIA")
        }
    }
}

// COMPONENTE REUTILIZABLE DE RESPUESTA
@Composable
fun BotonRespuesta(
    texto: String,
    valor: Boolean,
    respuestaCorrecta: Boolean,
    respondida: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val colorBoton = when {
        !respondida -> MaterialTheme.colorScheme.primary
        valor == respuestaCorrecta -> Color(0xFF2E7D32)
        else -> Color(0xFFC62828)
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = !respondida,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorBoton,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = colorBoton,
            disabledContentColor = Color.White
        )
    ) {
        Text(texto)
    }
}
