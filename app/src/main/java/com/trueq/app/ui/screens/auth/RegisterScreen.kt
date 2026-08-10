@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.trueq.app.di.appViewModel
import com.trueq.app.util.URL_AVISO_DE_PRIVACIDAD
import com.trueq.app.util.URL_TERMINOS_Y_CONDICIONES

@Composable
fun RegisterScreen(
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit
) {
    val viewModel = appViewModel { RegisterViewModel(it.api) }
    val estado by viewModel.uiState.collectAsState()
    var mostrarPassword by remember { mutableStateOf(false) }
    var aceptaTerminos by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(estado.exito) {
        if (estado.exito) onRegistroExitoso()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(8.dp))
        Text("Crea tu cuenta", style = MaterialTheme.typography.headlineMedium)
        Text("Comienza tu viaje de intercambio hoy mismo.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = estado.nombre,
            onValueChange = viewModel::onNombreChange,
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = estado.apellidoPaterno,
                onValueChange = viewModel::onApellidoPaternoChange,
                label = { Text("Apellido paterno") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = estado.apellidoMaterno,
                onValueChange = viewModel::onApellidoMaternoChange,
                label = { Text("Apellido materno") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = estado.correo,
            onValueChange = viewModel::onCorreoChange,
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = estado.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                    Icon(if (mostrarPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                }
            },
            visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (estado.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(estado.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = aceptaTerminos, onCheckedChange = { aceptaTerminos = it })

            val colorEnlace = MaterialTheme.colorScheme.primary
            val textoAviso = buildAnnotatedString {
                append("He leído y acepto los ")
                pushStringAnnotation(tag = "TERMINOS", annotation = URL_TERMINOS_Y_CONDICIONES)
                withStyle(SpanStyle(color = colorEnlace, textDecoration = TextDecoration.Underline)) {
                    append("Términos y Condiciones")
                }
                pop()
                append(" y el ")
                pushStringAnnotation(tag = "PRIVACIDAD", annotation = URL_AVISO_DE_PRIVACIDAD)
                withStyle(SpanStyle(color = colorEnlace, textDecoration = TextDecoration.Underline)) {
                    append("Aviso de Privacidad")
                }
                pop()
                append(" de TrueQ.")
            }

            ClickableText(
                text = textoAviso,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(top = 14.dp),
                onClick = { posicion ->
                    textoAviso.getStringAnnotations(start = posicion, end = posicion).firstOrNull()?.let { anotacion ->
                        uriHandler.openUri(anotacion.item)
                    }
                }
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = viewModel::registrar,
            enabled = !estado.cargando && aceptaTerminos,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (estado.cargando) "Creando cuenta..." else "Registrarse")
        }

        Spacer(Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("¿Ya tienes cuenta? ", style = MaterialTheme.typography.bodySmall)
            Text(
                "Iniciar sesión",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onIrALogin)
            )
        }
    }
}
