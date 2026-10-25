package com.hache.hachesquizz

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Práctica 4 - Apartado 4.1: Sistema de identificación de usuarios.
 * Gestiona el acceso mediante un HashMap con credenciales predefinidas.
 */
object Autenticacion {
    // HashMap didáctico con usuarios y contraseñas autorizados (mínimo 3).
    val usuariosValidos: HashMap<String, String> = hashMapOf(
        "alumno" to "pgl2026",
        "profesor" to "cesarmanrique",
        "hache" to "dam2026"
    )

    /**
     * Valida usuario y contraseña admitiendo pequeñas variaciones de teclado móvil
     * (espacios accidentales al final o mayúscula inicial automática).
     */
    fun validar(usuario: String, pass: String): Boolean {
        val u = usuario.trim().lowercase()
        val p = pass.trim()
        val passGuardada = usuariosValidos[u] ?: return false
        return passGuardada == p || passGuardada.equals(p, ignoreCase = true)
    }
}

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var modoOscuro by rememberSaveable {
                mutableStateOf(Configuracion.esOscuro(this@LoginActivity))
            }

            TemaHaches(oscuro = modoOscuro) {
                PantallaLogin(
                    modoOscuro = modoOscuro,
                    cambiarTema = { nuevo ->
                        modoOscuro = nuevo
                        Configuracion.guardarTema(this@LoginActivity, nuevo)
                    },
                    validarUsuario = { usuario, password ->
                        Autenticacion.validar(usuario, password)
                    },
                    onLoginExitoso = { usuario ->
                        val intent = Intent(this@LoginActivity, BienvenidaActivity::class.java).apply {
                            putExtra("usuario_actual", usuario.trim())
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaLogin(
    modoOscuro: Boolean,
    cambiarTema: (Boolean) -> Unit,
    validarUsuario: (String, String) -> Boolean,
    onLoginExitoso: (String) -> Unit
) {
    var usuario by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMensaje by rememberSaveable { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val intentarLogin = {
        val u = usuario.trim()
        val p = password.trim()
        if (u.isBlank() || p.isBlank()) {
            errorMensaje = "Por favor, completa todos los campos"
        } else if (validarUsuario(u, p)) {
            errorMensaje = null
            onLoginExitoso(u)
        } else {
            errorMensaje = "Usuario o contraseña incorrectos. Inténtalo de nuevo."
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
    ) {
        val alturaDisponible = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Interruptor de modo oscuro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Modo oscuro",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = modoOscuro,
                    onCheckedChange = cambiarTema
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = (alturaDisponible - 48.dp - 56.dp).coerceAtLeast(0.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_haches_quizz),
                    contentDescription = "Logo de Hache's Quizz",
                    modifier = Modifier.size(110.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Hache's Quizz",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text(
                    text = "INICIAR SESIÓN",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(28.dp))

                // Campo Usuario: Desactiva auto-capitalización para no convertir 'alumno' en 'Alumno'
                OutlinedTextField(
                    value = usuario,
                    onValueChange = {
                        usuario = it
                        if (errorMensaje != null) errorMensaje = null
                    },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                // Campo Contraseña: Ojo para mostrar/ocultar y KeyboardType.Password sin mayúscula inicial
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (errorMensaje != null) errorMensaje = null
                    },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val desc = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = desc)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        intentarLogin()
                    }),
                    modifier = Modifier.fillMaxWidth()
                )

                // Mensaje de error
                AnimatedVisibility(
                    visible = errorMensaje != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMensaje ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        intentarLogin()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(20.dp))

                // Botones rápidos para autocompletar en el móvil con 1 solo toque
                Text(
                    text = "Acceso rápido de prueba (toca uno):",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SuggestionChip(
                        onClick = {
                            usuario = "alumno"
                            password = "pgl2026"
                            errorMensaje = null
                        },
                        label = { Text("alumno", fontSize = 12.sp) }
                    )
                    SuggestionChip(
                        onClick = {
                            usuario = "profesor"
                            password = "cesarmanrique"
                            errorMensaje = null
                        },
                        label = { Text("profesor", fontSize = 12.sp) }
                    )
                    SuggestionChip(
                        onClick = {
                            usuario = "hache"
                            password = "dam2026"
                            errorMensaje = null
                        },
                        label = { Text("hache", fontSize = 12.sp) }
                    )
                }
            }
        }
    }
}
