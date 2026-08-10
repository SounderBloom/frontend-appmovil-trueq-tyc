@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trueq.app.data.model.TipoTransaccion
import com.trueq.app.di.appViewModel
import com.trueq.app.ui.components.PhotoCarousel
import com.trueq.app.ui.components.TopBarBack
import com.trueq.app.util.etiquetaTipoTransaccion
import com.trueq.app.util.formatearFechaRelativa
import com.trueq.app.util.formatearPrecio

@Composable
fun ProductDetailScreen(
    productoId: String,
    onVolver: () -> Unit,
    onIrAlChat: (chatId: String, proponer: Boolean) -> Unit
) {
    val viewModel = appViewModel { ProductDetailViewModel(it.api) }
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(productoId) { viewModel.cargar(productoId) }

    LaunchedEffect(estado.chatListoId) {
        val valor = estado.chatListoId ?: return@LaunchedEffect
        val (chatId, proponer) = valor.split("|")
        onIrAlChat(chatId, proponer == "1")
        viewModel.consumirNavegacionChat()
    }

    val producto = estado.producto

    Scaffold(
        topBar = { TopBarBack(titulo = producto?.titulo ?: "Artículo", onVolver = onVolver) },
        bottomBar = {
            if (producto != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.irAlChat(false) },
                        enabled = !estado.iniciandoAccion,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Enviar mensaje", maxLines = 1)
                    }
                    Button(
                        onClick = { viewModel.irAlChat(true) },
                        enabled = !estado.iniciandoAccion,
                        modifier = Modifier.weight(1f)
                    ) {
                        val esDonacion = producto.tipoTransaccion == TipoTransaccion.Donar
                        Icon(
                            if (esDonacion) Icons.Filled.VolunteerActivism else Icons.Filled.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (esDonacion) "Pedir la donación" else "Proponer oferta", maxLines = 1)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                estado.noEncontrado || producto == null -> Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("No pudimos encontrar este artículo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    val esTrueque = producto.tipoTransaccion == TipoTransaccion.Trueque ||
                        producto.tipoTransaccion == TipoTransaccion.TruequeOVenta ||
                        producto.tipoTransaccion == TipoTransaccion.Donar

                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        PhotoCarousel(fotos = producto.fotos)

                        Column(modifier = Modifier.padding(16.dp)) {
                            if (producto.nombreCategoria != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        producto.nombreCategoria,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            Text(producto.titulo, style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(6.dp))

                            if (esTrueque) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(etiquetaTipoTransaccion(producto.tipoTransaccion), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            } else {
                                Text(formatearPrecio(producto.precio), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            }

                            Spacer(Modifier.height(16.dp))
                            Text("DESCRIPCIÓN DETALLADA", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(producto.descripcion, style = MaterialTheme.typography.bodyMedium)

                            val resumen = estado.calificacionVendedor
                            if (resumen != null) {
                                Spacer(Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    if (resumen.total > 0) {
                                        Text("${resumen.promedio} ", fontWeight = FontWeight.Bold)
                                        Text("(${resumen.total} reseñas del vendedor)", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        Text("Vendedor sin reseñas todavía", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Publicado ${formatearFechaRelativa(producto.fechaPublicacion)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (estado.mensajeAccion != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(estado.mensajeAccion!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}
