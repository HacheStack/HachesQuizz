
package com.hache.hachesquizz

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            var modoOscuro by rememberSaveable {
                mutableStateOf(
                    Configuracion.esOscuro(this@MainActivity)
                )
            }

            TemaHaches(oscuro = modoOscuro) {

                PantallaInicio(
                    modoOscuro = modoOscuro,

                    cambiarTema = { nuevoValor ->
                        modoOscuro = nuevoValor

                        Configuracion.guardarTema(
                            this@MainActivity,
                            nuevoValor
                        )
                    },

                    aceptar = {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                MenuPrincipal::class.java
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaInicio(
    modoOscuro: Boolean,
    cambiarTema: (Boolean) -> Unit,
    aceptar: () -> Unit
) {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {

        val alturaDisponible = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // INTERRUPTOR DE MODO OSCURO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Modo oscuro",
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(Modifier.width(8.dp))

                Switch(
                    checked = modoOscuro,
                    onCheckedChange = cambiarTema
                )
            }

            // CONTENIDO CENTRAL
            // Se centra en vertical cuando hay espacio.
            // En horizontal permite desplazarse.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = (alturaDisponible - 48.dp - 56.dp)
                            .coerceAtLeast(0.dp)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(
                        R.drawable.logo_haches_quizz
                    ),
                    contentDescription = "Logo de Hache's Quizz",
                    modifier = Modifier.size(140.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Hache's Quizz",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "¿Cuánto sabes realmente?",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(48.dp))

                Button(
                    onClick = aceptar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ACEPTAR")
                }
            }
        }
    }
}
