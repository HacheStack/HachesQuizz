package com.hache.hachesquizz

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.view.ContextThemeWrapper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Locale

/**
 * Panel Compose que utiliza ImageButton y SeekBar nativos con AndroidView.
 * Así se cumplen los componentes mencionados en el enunciado de la práctica.
 */
@Composable
fun PanelAudio(audio: ControladorAudio, infoUrl: String?) {
    val contexto = LocalContext.current
    val espacio = dimensionResource(R.dimen.hq_espacio_medio)
    val colorBoton = MaterialTheme.colorScheme.secondary.toArgb()
    val descripcion = stringResource(
        if (audio.reproduciendo) R.string.hq_pausar else R.string.hq_reproducir
    )
    val escala by animateFloatAsState(
        targetValue = if (audio.reproduciendo) 1.13f else 1f,
        animationSpec = spring(),
        label = "animacionBotonPlay"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(espacio)
    ) {
        Text(
            text = stringResource(R.string.hq_audio_disponible),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        if (!audio.cargado) {
            Text(
                text = stringResource(R.string.hq_audio_no_disponible),
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ImageButton nativo con icono que cambia entre play y pausa.
                AndroidView(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.hq_boton_play))
                        .scale(escala),
                    factory = { ctx ->
                        ImageButton(ctx).apply {
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                            setPadding(12, 12, 12, 12)
                        }
                    },
                    update = { boton ->
                        boton.setImageResource(
                            if (audio.reproduciendo) android.R.drawable.ic_media_pause
                            else android.R.drawable.ic_media_play
                        )
                        boton.imageTintList = ColorStateList.valueOf(colorBoton)
                        boton.contentDescription = descripcion
                        boton.setOnClickListener { audio.alternarReproduccion() }
                    }
                )

                Spacer(Modifier.width(espacio))

                // SeekBar nativo. El estilo y los colores están en values XML.
                AndroidView(
                    modifier = Modifier.weight(1f),
                    factory = { ctx ->
                        SeekBar(ContextThemeWrapper(ctx, R.style.HqAudioSeekBar)).apply {
                            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(
                                    seekBar: SeekBar,
                                    progress: Int,
                                    fromUser: Boolean
                                ) {
                                    if (fromUser) audio.moverA(progress)
                                }
                                override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
                                override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
                            })
                        }
                    },
                    update = { barra ->
                        barra.max = audio.duracionMs.coerceAtLeast(1)
                        barra.progress = audio.posicionMs.coerceIn(0, barra.max)
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatearTiempo(audio.posicionMs), color = MaterialTheme.colorScheme.onBackground)
                Text(formatearTiempo(audio.duracionMs), color = MaterialTheme.colorScheme.onBackground)
            }

            BotonSecundario(
                texto = stringResource(
                    if (audio.bucle) R.string.hq_bucle_activado
                    else R.string.hq_bucle_desactivado
                ),
                onClick = { audio.alternarBucle() }
            )
        }

        if (!infoUrl.isNullOrBlank()) {
            BotonSecundario(
                texto = stringResource(R.string.hq_mas_informacion),
                onClick = {
                    try {
                        contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(infoUrl)))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(contexto, R.string.hq_error_web, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

fun formatearTiempo(milisegundos: Int): String {
    val segundos = (milisegundos / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", segundos / 60, segundos % 60)
}
