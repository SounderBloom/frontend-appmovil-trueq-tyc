@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trueq.app.data.model.*
import com.trueq.app.data.remote.resolverUrlArchivo
import com.trueq.app.di.appViewModel
import com.trueq.app.ui.components.TopBarBack
import com.trueq.app.util.etiquetaDireccionMonto
import com.trueq.app.util.etiquetaTipoOferta
import com.trueq.app.util.etiquetaTipoTransaccion
import com.trueq.app.util.formatearPrecio
import kotlinx.coroutines.launch

@Composable
fun ChatConversationScreen(
    chatId: String,
    autoAbrirOferta: Boolean,
    onVolver: () -> Unit
) {
    val viewModel = appViewModel { ChatConversationViewModel(it.api) }
    val estado by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(chatId) { viewModel.cargar(chatId, autoAbrirOferta) }

    LaunchedEffect(estado.mensajes.size) {
        if (estado.mensajes.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(estado.mensajes.size - 1) }
        }
    }

    Scaffold(
        topBar = { TopBarBack(titulo = estado.chat?.nombreProductoSnapshot ?: "Conversación", onVolver = onVolver) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                estado.chat == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No encontramos esta conversación.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    val chat = estado.chat!!

                    // Barra de info del producto
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val imagen = resolverUrlArchivo(chat.imagenProductoSnapshot)
                        Box(
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceContainerLowest),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imagen != null) {
                                AsyncImage(model = imagen, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Icon(Icons.Filled.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(chat.nombreProductoSnapshot, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            Text(
                                "Intercambio: ${etiquetaTipoTransaccion(chat.tipoTransaccionProductoSnapshot)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val tieneOfertaPendiente = estado.propuestas.any { it.estado == EstadoPropuesta.Pendiente }
                        val esDonacion = chat.tipoTransaccionProductoSnapshot == TipoTransaccion.Donar
                        if (!chat.esVendedor && !tieneOfertaPendiente) {
                            AssistChip(
                                onClick = { viewModel.abrirFormularioOferta() },
                                label = { Text(if (esDonacion) "Pedir donación" else "Proponer") },
                                leadingIcon = {
                                    Icon(
                                        if (esDonacion) Icons.Filled.VolunteerActivism else Icons.Filled.SwapHoriz,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }

                    // Mensajes + ofertas
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (estado.mensajes.isEmpty()) {
                            item {
                                Text(
                                    "Aún no hay mensajes. ¡Escribe el primero!",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        items(estado.mensajes) { mensaje ->
                            if (mensaje.emisor == EmisorMensaje.Sistema) {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(50)) {
                                        Text(
                                            mensaje.contenido,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            } else {
                                val propio = viewModel.esMensajePropio(mensaje)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (propio) Arrangement.End else Arrangement.Start) {
                                    Surface(
                                        color = if (propio) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp, topEnd = 16.dp,
                                            bottomStart = if (propio) 16.dp else 4.dp,
                                            bottomEnd = if (propio) 4.dp else 16.dp
                                        ),
                                        border = if (!propio) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                                    ) {
                                        Text(
                                            mensaje.contenido,
                                            color = if (propio) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .widthIn(max = 280.dp)
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        items(estado.propuestas) { propuesta ->
                            TarjetaOferta(
                                propuesta = propuesta,
                                esVendedor = chat.esVendedor,
                                onAceptar = { viewModel.responderOferta(propuesta, true) },
                                onRechazar = { viewModel.responderOferta(propuesta, false) },
                                onCalificar = { viewModel.abrirCalificar(propuesta) }
                            )
                        }
                    }

                    if (estado.mostrarFormularioOferta) {
                        PanelNuevaOferta(estado, viewModel)
                    }

                    if (estado.propuestaParaCalificar != null) {
                        PanelCalificar(estado, viewModel)
                    }

                    // Entrada de texto
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = estado.texto,
                            onValueChange = viewModel::onTextoChange,
                            placeholder = { Text("Escribe un mensaje...") },
                            modifier = Modifier.weight(1f),
                            maxLines = 4
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledIconButton(
                            onClick = { viewModel.enviarMensaje() },
                            enabled = !estado.enviando && estado.texto.isNotBlank()
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = "Enviar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaOferta(
    propuesta: PropuestaDTO,
    esVendedor: Boolean,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onCalificar: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(etiquetaTipoOferta(propuesta.tipoOferta).uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val (colorFondo, colorTexto, texto) = when (propuesta.estado) {
                    EstadoPropuesta.Aceptada -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary, "Aceptada")
                    EstadoPropuesta.Rechazada -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, "Rechazada")
                    else -> Triple(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.onSurfaceVariant, "Pendiente")
                }
                Surface(color = colorFondo, shape = RoundedCornerShape(50)) {
                    Text(texto, color = colorTexto, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            if (propuesta.tipoOferta == TipoOferta.Compra) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(10.dp)).padding(8.dp)
                ) {
                    Text(propuesta.productoSolicitadoTitulo, modifier = Modifier.weight(1f), maxLines = 1)
                    Text(formatearPrecio(propuesta.monto ?: 0.0), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            } else if (propuesta.tipoOferta == TipoOferta.SolicitudDonacion) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(10.dp)).padding(8.dp)
                ) {
                    Icon(Icons.Filled.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(propuesta.productoSolicitadoTitulo, modifier = Modifier.weight(1f), maxLines = 1)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        propuesta.productoOfrecidoTitulo ?: "",
                        modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(10.dp)).padding(8.dp),
                        maxLines = 1
                    )
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp))
                    Text(
                        propuesta.productoSolicitadoTitulo,
                        modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(10.dp)).padding(8.dp),
                        maxLines = 1
                    )
                }
                if (propuesta.tipoOferta == TipoOferta.TruequeConDiferencia) {
                    Spacer(Modifier.height(6.dp))
                    val texto = if (propuesta.direccionMonto == DireccionMonto.ProponentePagaAlVendedor) "que pone el proponente" else "que el proponente pide a cambio"
                    Text(
                        "+ ${formatearPrecio(propuesta.monto ?: 0.0)} $texto",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            if (propuesta.mensaje.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("\"${propuesta.mensaje}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (esVendedor && propuesta.estado == EstadoPropuesta.Pendiente) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onRechazar, modifier = Modifier.weight(1f)) { Text("Rechazar") }
                    Button(onClick = onAceptar, modifier = Modifier.weight(1f)) { Text("Aceptar") }
                }
            }

            if (propuesta.puedeCalificar) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onCalificar, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Calificar al vendedor")
                }
            }
        }
    }
}

@Composable
private fun PanelNuevaOferta(estado: ChatConversationUiState, viewModel: ChatConversationViewModel) {
    val esDonacion = estado.tipoOferta == TipoOferta.SolicitudDonacion && viewModel.esProductoDeDonacionActual()

    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp).heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (esDonacion) "Pedir la donación" else "Nueva oferta", style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = { viewModel.cerrarFormularioOferta() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }

            if (esDonacion) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Icon(Icons.Filled.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Este artículo es una donación. Le vas a pedir al vendedor que te lo done a ti; él puede aceptar o rechazar tu solicitud.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = estado.tipoOferta == TipoOferta.Trueque, onClick = { viewModel.onTipoOfertaChange(TipoOferta.Trueque) }, label = { Text("Trueque") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = estado.tipoOferta == TipoOferta.Compra, onClick = { viewModel.onTipoOfertaChange(TipoOferta.Compra) }, label = { Text("Comprar") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = estado.tipoOferta == TipoOferta.TruequeConDiferencia, onClick = { viewModel.onTipoOfertaChange(TipoOferta.TruequeConDiferencia) }, label = { Text("Trueque + $") }, modifier = Modifier.weight(1f))
                }
            }

            if (viewModel.requiereProductoActual()) {
                Spacer(Modifier.height(10.dp))
                Text("Tu artículo a ofrecer", style = MaterialTheme.typography.labelMedium)
                if (estado.misProductosDisponibles.isEmpty()) {
                    Text("No tienes artículos disponibles para ofrecer.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    estado.misProductosDisponibles.forEach { producto ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, if (estado.productoOfrecidoId == producto.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { viewModel.onProductoOfrecidoChange(producto.id) }
                                .padding(8.dp)
                        ) {
                            RadioButton(selected = estado.productoOfrecidoId == producto.id, onClick = { viewModel.onProductoOfrecidoChange(producto.id) })
                            Text(producto.titulo, maxLines = 1)
                        }
                    }
                }
            }

            if (viewModel.requiereMontoActual()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    if (estado.tipoOferta == TipoOferta.Compra) "Monto que ofreces" else "Diferencia en efectivo",
                    style = MaterialTheme.typography.labelMedium
                )
                OutlinedTextField(
                    value = estado.monto,
                    onValueChange = viewModel::onMontoChange,
                    leadingIcon = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (estado.tipoOferta == TipoOferta.TruequeConDiferencia) {
                    Spacer(Modifier.height(6.dp))
                    listOf(DireccionMonto.ProponentePagaAlVendedor, DireccionMonto.VendedorPagaAlProponente).forEach { opcion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, if (estado.direccionMonto == opcion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { viewModel.onDireccionMontoChange(opcion) }
                                .padding(8.dp)
                        ) {
                            RadioButton(selected = estado.direccionMonto == opcion, onClick = { viewModel.onDireccionMontoChange(opcion) })
                            Text(etiquetaDireccionMonto(opcion), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = estado.mensajeOferta,
                onValueChange = viewModel::onMensajeOfertaChange,
                placeholder = {
                    Text(
                        if (esDonacion) "Cuéntale al vendedor por qué te gustaría recibirlo (opcional)"
                        else "Cuéntale al vendedor por qué es buena oferta (opcional)"
                    )
                },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            if (estado.errorOferta != null) {
                Spacer(Modifier.height(6.dp))
                Text(estado.errorOferta, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { viewModel.enviarOferta() },
                enabled = !estado.enviandoOferta,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (estado.enviandoOferta) "Enviando..."
                    else if (esDonacion) "Pedir la donación"
                    else "Enviar oferta"
                )
            }
        }
    }
}

@Composable
private fun PanelCalificar(estado: ChatConversationUiState, viewModel: ChatConversationViewModel) {
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Calificar al vendedor", style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = { viewModel.cerrarCalificar() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }

            if (estado.calificacionEnviada) {
                Text("¡Gracias por tu calificación!", color = MaterialTheme.colorScheme.tertiary)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    (1..5).forEach { n ->
                        IconButton(onClick = { viewModel.onEstrellasChange(n) }) {
                            Icon(
                                if (n <= estado.estrellas) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = estado.comentario,
                    onValueChange = viewModel::onComentarioChange,
                    placeholder = { Text("Cuenta cómo fue tu experiencia (opcional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.enviarCalificacion() },
                    enabled = !estado.enviandoCalificacion && estado.estrellas >= 1,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.enviandoCalificacion) "Enviando..." else "Enviar calificación")
                }
            }
        }
    }
}
