
package com.hache.hachesquizz

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MenuPrincipal : ComponentActivity() {

    private var irAlMenuPrincipal by mutableStateOf(false)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("ir_al_menu_principal", false)) {
            irAlMenuPrincipal = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            // 0 = Menú principal
            // 1 = Selección de modo
            // 2 = Selección de dificultad
            var pantalla by rememberSaveable {
                mutableIntStateOf(0)
            }

            LaunchedEffect(irAlMenuPrincipal) {
                if (irAlMenuPrincipal) {
                    pantalla = 0
                    irAlMenuPrincipal = false
                }
            }

            var modoNinos by rememberSaveable {
                mutableStateOf(false)
            }

            TemaHaches(
                oscuro = Configuracion.esOscuro(this@MenuPrincipal)
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

                            intent.putExtra(
                                "modo_ninos",
                                modoNinos
                            )

                            intent.putExtra(
                                "segundos_por_pregunta",
                                segundos
                            )

                            pantalla = 0
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

// CONTENEDOR COMÚN PARA LAS TRES PANTALLAS
// Mantiene el fondo del tema y permite desplazarse en horizontal.
@Composable
private fun ContenedorMenu(
    contenido: @Composable ColumnScope.() -> Unit
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = (alturaDisponible - 48.dp)
                            .coerceAtLeast(0.dp)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                content = contenido
            )
        }
    }
}

// MENÚ PRINCIPAL
@Composable
fun PantallaMenu(
    jugar: () -> Unit,
    volver: () -> Unit
) {

    val context = LocalContext.current

    ContenedorMenu {

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

        Button(
            onClick = jugar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("JUGAR")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                context.startActivity(
                    Intent(
                        context,
                        AgregarPregunta::class.java
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("AÑADIR PREGUNTAS")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                context.startActivity(
                    Intent(
                        context,
                        Resultados::class.java
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ESTADÍSTICAS")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                context.startActivity(
                    Intent(
                        context,
                        MapaActivity::class.java
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("EXPLORADOR DE MAPAS")
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

// SELECCIÓN DE MODO
@Composable
fun PantallaSeleccionModo(
    jugar: (Boolean) -> Unit,
    volver: () -> Unit
) {

    ContenedorMenu {

        Text(
            text = "Hache's Quizz",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "ELIGE TU MODO DE JUEGO",
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
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

// SELECCIÓN DE DIFICULTAD
@Composable
fun PantallaSeleccionTiempo(
    modoNinos: Boolean,
    jugar: (Int) -> Unit,
    volver: () -> Unit
) {

    ContenedorMenu {

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

        Button(
            onClick = { jugar(0) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SIN TIEMPO")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { jugar(15) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("FÁCIL - 15 SEGUNDOS")
        }

        Spacer(Modifier.height(12.dp))

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
