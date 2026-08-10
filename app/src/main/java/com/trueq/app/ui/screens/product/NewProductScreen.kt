@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.product

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trueq.app.di.appViewModel
import com.trueq.app.ui.components.BottomNavBar
import com.trueq.app.ui.components.TopBarBack

@Composable
fun NewProductScreen(
    rutaActual: String,
    onVolver: () -> Unit,
    onNavegar: (String) -> Unit,
    onPublicado: () -> Unit
) {
    val viewModel = appViewModel { NewProductViewModel(it.api) }
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.inicializar(context) }
    LaunchedEffect(estado.exito) { if (estado.exito) onPublicado() }

    val selectorFotos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> if (uris.isNotEmpty()) viewModel.agregarFotos(uris) }

    Scaffold(
        topBar = { TopBarBack(titulo = "Publicar nuevo artículo", onVolver = onVolver) },
        bottomBar = { BottomNavBar(rutaActual, onNavegar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Fotos", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))

            FotosGrid(
                uris = estado.fotos,
                onQuitar = viewModel::quitarFoto,
                onMover = viewModel::moverFoto,
                onAgregar = { selectorFotos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Usa las flechas para ordenarlas. La marcada \"Principal\" es la que se ve primero en el detalle y como miniatura.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = estado.titulo,
                onValueChange = viewModel::onTituloChange,
                label = { Text("Título del artículo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            var expandidoCategoria by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expandidoCategoria, onExpandedChange = { expandidoCategoria = it }) {
                OutlinedTextField(
                    value = estado.categorias.find { it.id == estado.categoriaId }?.nombre ?: "Selecciona una categoría",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandidoCategoria, onDismissRequest = { expandidoCategoria = false }) {
                    estado.categorias.forEach { categoria ->
                        DropdownMenuItem(text = { Text(categoria.nombre) }, onClick = {
                            viewModel.onCategoriaChange(categoria.id); expandidoCategoria = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("Tipo de transacción", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            estado.tiposTransaccion.forEach { tipo ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, if (estado.tipoTransaccion == tipo.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { viewModel.onTipoTransaccionChange(tipo.id) }
                        .padding(12.dp)
                ) {
                    RadioButton(selected = estado.tipoTransaccion == tipo.id, onClick = { viewModel.onTipoTransaccionChange(tipo.id) })
                    Text(tipo.tipoTransaccion)
                }
                Spacer(Modifier.height(6.dp))
            }

            if (viewModel.requierePrecio()) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = estado.precio,
                    onValueChange = viewModel::onPrecioChange,
                    label = { Text("Precio sugerido") },
                    leadingIcon = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(12.dp)
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        estado.obteniendoUbicacion -> "Obteniendo ubicación..."
                        estado.ubicacionLista -> "Ubicación actual obtenida correctamente."
                        else -> "Debes permitir el acceso a tu ubicación para publicar."
                    },
                    color = if (!estado.obteniendoUbicacion && !estado.ubicacionLista) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (estado.mensaje != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    estado.mensaje!!,
                    color = if (estado.mensaje!!.contains("correctamente")) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { viewModel.publicar(context) },
                enabled = !estado.enviando && estado.ubicacionLista,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (estado.enviando) "Publicando..." else "Publicar artículo")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FotosGrid(
    uris: List<android.net.Uri>,
    onQuitar: (Int) -> Unit,
    onMover: (Int, Int) -> Unit,
    onAgregar: () -> Unit
) {
    val columnas = 3
    val filas = (uris.size + 1 + columnas - 1) / columnas

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (fila in 0 until filas) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until columnas) {
                    val indice = fila * columnas + col
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            indice < uris.size -> FotoTile(
                                uri = uris[indice],
                                esPrincipal = indice == 0,
                                puedeIzquierda = indice > 0,
                                puedeDerecha = indice < uris.size - 1,
                                onQuitar = { onQuitar(indice) },
                                onMoverIzquierda = { onMover(indice, -1) },
                                onMoverDerecha = { onMover(indice, 1) }
                            )
                            indice == uris.size -> AgregarFotoTile(onClick = onAgregar)
                            else -> Spacer(Modifier.aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FotoTile(
    uri: android.net.Uri,
    esPrincipal: Boolean,
    puedeIzquierda: Boolean,
    puedeDerecha: Boolean,
    onQuitar: () -> Unit,
    onMoverIzquierda: () -> Unit,
    onMoverDerecha: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

        Surface(color = Color.Black.copy(alpha = 0.55f), shape = RoundedCornerShape(6.dp), modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) {
            Text(
                if (esPrincipal) "Principal" else "",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        IconButton(onClick = onQuitar, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Quitar", tint = Color.White)
        }

        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)) {
            IconButton(onClick = onMoverIzquierda, enabled = puedeIzquierda, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Mover antes", tint = Color.White)
            }
            IconButton(onClick = onMoverDerecha, enabled = puedeDerecha, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Mover después", tint = Color.White)
            }
        }
    }
}

@Composable
private fun AgregarFotoTile(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Icon(Icons.Filled.AddAPhoto, contentDescription = "Añadir foto", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Añadir", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
