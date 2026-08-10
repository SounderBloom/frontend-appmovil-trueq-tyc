@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.trueq.app.data.model.NotificacionDTO
import com.trueq.app.data.model.TipoNotificacion
import com.trueq.app.di.appViewModel
import com.trueq.app.ui.components.BottomNavBar
import com.trueq.app.ui.components.TopBarBack
import com.trueq.app.util.formatearFechaRelativa

private fun iconoPorTipo(tipo: Int): ImageVector = when (tipo) {
    TipoNotificacion.MensajeNuevo -> Icons.Filled.ChatBubble
    TipoNotificacion.PropuestaRecibida -> Icons.Filled.SwapHoriz
    TipoNotificacion.PropuestaRespondida -> Icons.Filled.CheckCircle
    TipoNotificacion.CalificacionRecibida -> Icons.Filled.Star
    else -> Icons.Filled.Notifications
}

@Composable
fun NotificationsScreen(
    rutaActual: String,
    onVolver: () -> Unit,
    onNavegar: (String) -> Unit,
    onAbrirChat: (String) -> Unit,
    onNotificacionesActualizadas: () -> Unit
) {
    val viewModel = appViewModel { NotificationsViewModel(it.api) }
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargar() }
    DisposableEffect(Unit) { onDispose { onNotificacionesActualizadas() } }

    Scaffold(
        topBar = {
            TopBarBack(titulo = "Notificaciones", onVolver = onVolver) {
                if (estado.notificaciones.any { !it.leida }) {
                    TextButton(onClick = { viewModel.marcarTodasLeidas() }) {
                        Text("Marcar todo como leído")
                    }
                }
            }
        },
        bottomBar = { BottomNavBar(rutaActual, onNavegar) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                estado.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(estado.error!!, color = MaterialTheme.colorScheme.error)
                }
                estado.notificaciones.isEmpty() -> Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.NotificationsOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Sin notificaciones por ahora", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Te avisaremos aquí cuando pase algo en TrueQ.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                else -> LazyColumn {
                    items(estado.notificaciones) { notificacion ->
                        FilaNotificacion(
                            notificacion = notificacion,
                            onClick = {
                                viewModel.marcarLeida(notificacion)
                                if (notificacion.tipo == TipoNotificacion.MensajeNuevo && notificacion.referenciaId != null) {
                                    onAbrirChat(notificacion.referenciaId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaNotificacion(notificacion: NotificacionDTO, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notificacion.leida) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconoPorTipo(notificacion.tipo), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(notificacion.titulo, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                Text(formatearFechaRelativa(notificacion.fechaCreacion), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(notificacion.contenido, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!notificacion.leida) {
            Spacer(Modifier.width(6.dp))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        }
    }
}
