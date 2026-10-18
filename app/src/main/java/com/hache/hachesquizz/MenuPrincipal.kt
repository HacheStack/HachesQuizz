
package com.hache.hachesquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MenuPrincipal : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            // 0 = Menu principal
            // 1 = Seleccion de modo
            // 2 = Seleccion de dificultad
            var pantalla by rememberSaveable {
                mutableIntStateOf(0)
            }

            var modoNinos by rememberSaveable {
                mutableStateOf(false)
            }

            TemaHaches(
                oscuro = Configuracion.esOscuro(this)
            ) {

                BackHandler(enabled = pantalla != 0) {
                    pantalla--
                }

                when (pantalla) {

                    0 -> PantallaMenu(
                        jugar = {
                            pantalla = 1
                        },
                        volver = {
                            finish()
                        }
                    )

                    1 -> PantallaSeleccionModo(
                        jugar = { ninos ->
                            modoNinos = ninos
                            pantalla = 2
                        },
                        volver = {
                            pantalla = 0
                        }
                    )

                    2 -> PantallaSeleccionTiempo(
                        modoNinos = modoNinos,
                        jugar = { segundos ->

                            val intent = Intent(
                                this@MenuPrincipal,
                                Buzz::class.java
                            )

                            intent.putExtra("modo_ninos", modoNinos)
                            intent.putExtra(
                                "segundos_por_pregunta",
                                segundos
                            )

                            startActivity(intent)
                        },
                        volver = {
                            pantalla = 1
                        }
                    )
                }
            }
        }
    }
}

// MENU PRINCIPAL
@Composable
fun PantallaMenu(
    jugar: () -> Unit,
    volver: () -> Unit
) {

    val context = LocalContext.current

    fun mostrarAviso() {
        Toast.makeText(
            context,
            "Esta pantalla la haremos pronto",
            Toast.LENGTH_SHORT
        ).show()
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

        Text(
            text = "Hache's Quizz",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "MENÚ PRINCIPAL",
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(40.dp))

        // JUGAR
        Button(
            onClick = jugar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("JUGAR")
        }

        Spacer(Modifier.height(12.dp))


        // AÑADIR PREGUNTAS
        Button(
            onClick = {
                val intent = Intent(
                    context,
                    AgregarPregunta::class.java
                )
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("AÑADIR PREGUNTAS")
        }


        Spacer(Modifier.height(12.dp))

        // ESTADISTICAS
        Button(
            onClick = {
                val intent = Intent(
                    context,
                    Resultados::class.java
                )
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ESTADÍSTICAS")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = volver,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text("VOLVER")
        }
    }
}

// SELECCION DE MODO
@Composable
fun PantallaSeleccionModo(
    jugar: (Boolean) -> Unit,
    volver: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Hache's Quizz",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "ELIGE TU MODO DE JUEGO",
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = { jugar(false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("MODO CLÁSICO")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { jugar(true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("MODO NIÑ@S")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = volver,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text("VOLVER")
        }
    }
}

// SELECCION DE DIFICULTAD
@Composable
fun PantallaSeleccionTiempo(
    modoNinos: Boolean,
    jugar: (Int) -> Unit,
    volver: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Hache's Quizz",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = if (modoNinos) {
                "MODO NIÑ@S"
            } else {
                "MODO CLÁSICO"
            },
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "ELIGE LA DIFICULTAD",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(32.dp))

        // SIN TIEMPO
        Button(
            onClick = { jugar(0) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SIN TIEMPO")
        }

        Spacer(Modifier.height(12.dp))

        // FACIL: 15 SEGUNDOS
        Button(
            onClick = { jugar(15) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("FÁCIL - 15 SEGUNDOS")
        }

        Spacer(Modifier.height(12.dp))

        // DIFICIL: 8 SEGUNDOS
        Button(
            onClick = { jugar(8) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("DIFÍCIL - 8 SEGUNDOS")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = volver,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text("VOLVER")
        }
    }
}
