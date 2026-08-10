@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trueq.app.di.appViewModel
import com.trueq.app.ui.components.BottomNavBar
import com.trueq.app.ui.components.NotificationBell
import com.trueq.app.ui.components.ProductCard

@Composable
fun ProfileScreen(
    rutaActual: String,
    noLeidas: Int,
    onNavegar: (String) -> Unit,
    onAbrirNotificaciones: () -> Unit,
    onAbrirProducto: (String) -> Unit,
    onSesionCerrada: () -> Unit
) {
    val viewModel = appViewModel { ProfileViewModel(it.api, it.tokenManager) }
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TrueQ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                actions = { NotificationBell(noLeidas, onAbrirNotificaciones) }
            )
        },
        bottomBar = { BottomNavBar(rutaActual, onNavegar) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                estado.error != null || estado.perfil == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(estado.error ?: "No se encontró tu perfil.", color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    val perfil = estado.perfil!!
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {

                        // Encabezado del perfil
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.shapes.large).padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    perfil.nombre.trim().take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("${perfil.nombre} ${perfil.apellidoPaterno}", style = MaterialTheme.typography.headlineMedium)
                            Text(perfil.correo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            if (perfil.totalCalificaciones > 0) {
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${perfil.promedioCalificacion}", fontWeight = FontWeight.Bold)
                                    Text(" (${perfil.totalCalificaciones} reseñas)", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            if (perfil.biografia.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(perfil.biografia, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }

                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                                EstadisticaPerfil(valor = perfil.truequesRealizados.toString(), etiqueta = "Trueques realizados")
                                EstadisticaPerfil(valor = perfil.articulosActivos.toString(), etiqueta = "Artículos activos")
                            }

                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.cerrarSesion(onSesionCerrada) },
                                enabled = !estado.cerrandoSesion,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Cerrar sesión")
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("Mis artículos", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(8.dp))
                        if (estado.misProductos.isEmpty()) {
                            Text("Aún no has publicado ningún artículo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            estado.misProductos.chunked(2).forEach { fila ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                    fila.forEach { producto ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            ProductCard(producto = producto, onClick = { onAbrirProducto(producto.id) })
                                        }
                                    }
                                    if (fila.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Reseñas", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(8.dp))
                        if (estado.resenas.isEmpty()) {
                            Text("Todavía no tienes reseñas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            estado.resenas.forEach { resena ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.shapes.medium)
                                        .padding(12.dp)
                                        .padding(bottom = 8.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(resena.calificadorNombre, style = MaterialTheme.typography.labelMedium)
                                        Row {
                                            (1..5).forEach { n ->
                                                Icon(
                                                    if (n <= resena.estrellas) Icons.Filled.Star else Icons.Filled.StarBorder,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (resena.comentario.isNotBlank()) {
                                        Text(resena.comentario, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadisticaPerfil(valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.headlineMedium)
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
