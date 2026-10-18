
package com.hache.hachesquizz

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.IOException

class AgregarPregunta : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TemaHaches(
                oscuro = Configuracion.esOscuro(this)
            ) {
                PantallaAgregarPregunta(
                    volver = { finish() }
                )
            }
        }
    }
}

@Composable
fun PantallaAgregarPregunta(volver: () -> Unit) {

    val context = LocalContext.current

    // Texto introducido por el usuario
    var texto by rememberSaveable {
        mutableStateOf("")
    }

    // Respuesta correcta de la pregunta
    var resultado by rememberSaveable {
        mutableStateOf(true)
    }

    // Categoría de la pregunta
    var modoNinos by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // TÍTULO
        Text(
            text = "Hache's Quizz",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Text(
            text = "AÑADIR PREGUNTA",
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(12.dp))

        // ENUNCIADO
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = {
                Text("Escribe tu pregunta")
            },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(Modifier.height(8.dp))

        // RESPUESTA CORRECTA
        Text(
            text = "Respuesta correcta",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = resultado,
                    onClick = { resultado = true }
                )
                Text(
                    "Verdadero",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = !resultado,
                    onClick = { resultado = false }
                )
                Text(
                    "Falso",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // CATEGORÍA
        Text(
            text = "Categoría de la pregunta",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = !modoNinos,
                    onClick = { modoNinos = false }
                )
                Text(
                    "Clásico",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = modoNinos,
                    onClick = { modoNinos = true }
                )
                Text(
                    "Niñ@s",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // GUARDAR PREGUNTA
        Button(
            onClick = {

                if (texto.isBlank()) {

                    Toast.makeText(
                        context,
                        "Debes escribir una pregunta",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    try {

                        val nuevaPregunta = Pregunta(
                            texto = texto,
                            resultado = resultado
                        )

                        RepositorioPreguntas.guardar(
                            context,
                            modoNinos,
                            nuevaPregunta
                        )

                        Toast.makeText(
                            context,
                            "Pregunta guardada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Limpiar el formulario
                        texto = ""
                        resultado = true

                    } catch (e: IOException) {

                        Toast.makeText(
                            context,
                            "Error al guardar la pregunta",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GUARDAR PREGUNTA")
        }

        Spacer(Modifier.height(12.dp))

        // VOLVER AL MENÚ
        Button(
            onClick = volver,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text("VOLVER")
        }
    }
}

