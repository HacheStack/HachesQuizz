package com.hache.hachesquizz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

class Buzz : ComponentActivity() {
    private val reproductor by lazy { ControladorAudio(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val modoNinos = intent.getBooleanExtra("modo_ninos", false)
        val segundos = intent.getIntExtra("segundos_por_pregunta", 0)

        setContent {
            TemaHaches(oscuro = Configuracion.esOscuro(this)) {
                PantallaJuego(
                    modoNinos = modoNinos,
                    segundosPorPregunta = segundos,
                    audio = reproductor,
                    volver = {
                        reproductor.pausar()
                        EfectosSonido.liberar()
                        val intent = Intent(this@Buzz, MenuPrincipal::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("ir_al_menu_principal", true)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    override fun onStop() {
        reproductor.pausar()
        EfectosSonido.liberar()
        super.onStop()
    }

    override fun onDestroy() {
        reproductor.liberar()
        EfectosSonido.liberar()
        super.onDestroy()
    }
}

@Composable
fun PantallaJuego(
    modoNinos: Boolean,
    segundosPorPregunta: Int,
    audio: ControladorAudio,
    volver: () -> Unit = {}
) {
    BackHandler {
        volver()
    }

    val context = LocalContext.current
    val preguntas: ArrayList<Pregunta> = remember(modoNinos) {
        RepositorioPreguntas.cargar(context, modoNinos)
    }

    var indicePregunta by rememberSaveable { mutableIntStateOf(0) }
    var respuestaSeleccionada by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var tiempoRestante by rememberSaveable { mutableIntStateOf(segundosPorPregunta) }
    var tiempoAgotado by rememberSaveable { mutableStateOf(false) }
    var turno by rememberSaveable { mutableIntStateOf(0) }
    var posicionAudioGuardada by rememberSaveable { mutableIntStateOf(0) }

    // Una sola lista: el repositorio garantiza preguntas iniciales para ambas categorías.
    val preguntaActual = preguntas[indicePregunta]

    // Cambiar de pregunta siempre prepara su audio, incluso si ALEATORIA repite índice.
    // En una rotación se recupera la posición guardada, sin reproducción automática.
    LaunchedEffect(indicePregunta, turno) {
        EfectosSonido.liberar()
        audio.preparar(Recursos.audio(preguntaActual.audio), posicionAudioGuardada)
    }

    LaunchedEffect(audio.posicionMs) {
        if (audio.cargado) posicionAudioGuardada = audio.posicionMs
    }

    // La barra de progreso se actualiza aproximadamente cada medio segundo.
    LaunchedEffect(audio.reproduciendo) {
        while (audio.reproduciendo) {
            audio.refrescarPosicion()
            delay(500L)
        }
    }

    DisposableEffect(turno, segundosPorPregunta, respuestaSeleccionada, tiempoAgotado) {
        val cuentaAtras = if (
            segundosPorPregunta > 0 &&
            respuestaSeleccionada == null &&
            !tiempoAgotado &&
            tiempoRestante > 0
        ) {
            object : CountDownTimer(tiempoRestante * 1000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    tiempoRestante = ((millisUntilFinished + 999L) / 1000L).toInt()
                }

                override fun onFinish() {
                    if (respuestaSeleccionada == null && !tiempoAgotado) {
                        tiempoRestante = 0
                        tiempoAgotado = true
                        audio.pausar()
                        Estadisticas.registrarTiempoAgotado(context)
                        EfectosSonido.reproducir(context, false)
                    }
                }
            }.start()
        } else null

        onDispose { cuentaAtras?.cancel() }
    }

    fun cambiarPregunta(nuevoIndice: Int) {
        audio.pausar()
        EfectosSonido.liberar()
        indicePregunta = nuevoIndice
        respuestaSeleccionada = null
        tiempoAgotado = false
        tiempoRestante = segundosPorPregunta
        posicionAudioGuardada = 0
        turno++
    }

    fun responder(valor: Boolean) {
        if (respuestaSeleccionada == null && !tiempoAgotado) {
            val acierto = valor == preguntaActual.resultado
            audio.pausar()
            Estadisticas.registrarRespuesta(context, acierto)
            EfectosSonido.reproducir(context, acierto)
            respuestaSeleccionada = valor
        }
    }

    val espacio = dimensionResource(R.dimen.hq_espacio_medio)
    val espacioGrande = dimensionResource(R.dimen.hq_espacio_grande)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.hq_margen_pantalla)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(
                if (modoNinos) R.string.hq_modo_ninos else R.string.hq_modo_clasico
            ),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(espacio))

        Text(
            text = stringResource(R.string.hq_pregunta_numero, indicePregunta + 1, preguntas.size),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(espacio))

        if (segundosPorPregunta > 0) {
            Text(
                text = stringResource(R.string.hq_tiempo_restante, tiempoRestante),
                fontWeight = FontWeight.Bold,
                color = if (tiempoRestante <= 3) colorResource(R.color.hq_error)
                else MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(espacio))
        }

        // 1.ª animación: entrada y salida de preguntas.
        AnimatedContent(
            targetState = Pair(indicePregunta, turno),
            transitionSpec = {
                (slideInHorizontally { it / 2 } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 2 } + fadeOut())
            },
            label = "animacionPregunta"
        ) { (indice, _) ->
            Text(
                text = preguntas[indice].texto,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(espacioGrande))

        // Las preguntas sin audio no muestran este panel.
        if (preguntaActual.tieneAudio) {
            PanelAudio(audio, preguntaActual.infoUrl)
            Spacer(Modifier.height(espacioGrande))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(espacio)
        ) {
            BotonRespuesta(
                texto = stringResource(R.string.hq_verdadero),
                valor = true,
                respuestaCorrecta = preguntaActual.resultado,
                respondida = respuestaSeleccionada != null || tiempoAgotado,
                modifier = Modifier.weight(1f),
                onClick = { responder(true) }
            )
            BotonRespuesta(
                texto = stringResource(R.string.hq_falso),
                valor = false,
                respuestaCorrecta = preguntaActual.resultado,
                respondida = respuestaSeleccionada != null || tiempoAgotado,
                modifier = Modifier.weight(1f),
                onClick = { responder(false) }
            )
        }
        Spacer(Modifier.height(espacio))

        // 2.ª animación: aparece el resultado después de responder.
        AnimatedVisibility(
            visible = tiempoAgotado || respuestaSeleccionada != null,
            enter = scaleIn() + fadeIn(),
            exit = ExitTransition.None
        ) {
            val acierto = respuestaSeleccionada == preguntaActual.resultado
            Text(
                text = when {
                    tiempoAgotado -> stringResource(R.string.hq_tiempo_agotado)
                    acierto -> stringResource(R.string.hq_correcto)
                    else -> stringResource(R.string.hq_incorrecto)
                },
                color = colorResource(
                    if (acierto && !tiempoAgotado) R.color.hq_acierto else R.color.hq_error
                ),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(espacioGrande))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(espacio)
        ) {
            Button(
                onClick = {
                    Estadisticas.registrarClick(context)
                    cambiarPregunta((indicePregunta - 1 + preguntas.size) % preguntas.size)
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.hq_cd_anterior),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.hq_anterior), fontSize = 12.sp)
            }
            Button(
                onClick = {
                    Estadisticas.registrarClick(context)
                    cambiarPregunta((indicePregunta + 1) % preguntas.size)
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Text(stringResource(R.string.hq_siguiente), fontSize = 12.sp)
                Spacer(Modifier.width(4.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = stringResource(R.string.hq_cd_siguiente),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(espacio))

        BotonSecundario(
            texto = stringResource(R.string.hq_aleatoria),
            onClick = {
                Estadisticas.registrarClick(context)
                cambiarPregunta(Random.nextInt(preguntas.size))
            }
        )

        Spacer(Modifier.height(espacio))

        BotonSecundario(
            texto = stringResource(R.string.hq_volver_menu),
            onClick = volver
        )
    }
}

@Composable
fun BotonRespuesta(
    texto: String,
    valor: Boolean,
    respuestaCorrecta: Boolean,
    respondida: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = when {
        !respondida -> MaterialTheme.colorScheme.primary
        valor == respuestaCorrecta -> colorResource(R.color.hq_verde)
        else -> colorResource(R.color.hq_rojo)
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = !respondida,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = color,
            disabledContentColor = Color.White
        )
    ) {
        Text(texto, fontSize = 12.sp)
    }
}
