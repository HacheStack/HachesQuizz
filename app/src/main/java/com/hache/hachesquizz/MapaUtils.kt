package com.hache.hachesquizz

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.MapView

/**
 * Práctica 4: Utilidad para vincular de forma segura el ciclo de vida de MapView con Jetpack Compose.
 * Inicializa de inmediato onCreate(Bundle()) y sincroniza los estados STARTED y RESUMED
 * para garantizar que el motor de renderizado de Google Maps se active correctamente.
 */
@Composable
fun recordarMapViewConCicloDeVida(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // Sincronizar inmediatamente con el ciclo de vida actual de la actividad
        val estadoActual = lifecycleOwner.lifecycle.currentState
        if (estadoActual.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onStart()
        }
        if (estadoActual.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    return mapView
}
