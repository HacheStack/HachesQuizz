package com.hache.hachesquizz

import android.content.Intent
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var modoOscuro by rememberSaveable {
                mutableStateOf(Configuracion.esOscuro(this))
            }

            TemaHaches(oscuro = modoOscuro) {
                PantallaInicio(
                    modoOscuro = modoOscuro,
                    cambiarTema = { nuevoValor ->
                        modoOscuro = nuevoValor
                        Configuracion.guardarTema(
                            this, nuevoValor
                        )
                    },
                    aceptar = {
                        startActivity(
                            Intent(
                                this,
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

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


        Spacer(Modifier.weight(1f))


        Image(
            painter = painterResource(R.drawable.logo_haches_quizz),
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

        Spacer(Modifier.weight(1f))
    }
}