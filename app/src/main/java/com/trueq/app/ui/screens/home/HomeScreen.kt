@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.trueq.app.di.appViewModel
import com.trueq.app.ui.components.BottomNavBar
import com.trueq.app.ui.components.NotificationBell
import com.trueq.app.ui.components.ProductCard
import com.trueq.app.util.calcularDistanciaKm
import com.trueq.app.util.iconoCategoria

@Composable
fun HomeScreen(
    rutaActual: String,
    noLeidas: Int,
    onNavegar: (String) -> Unit,
    onAbrirNotificaciones: () -> Unit,
    onAbrirProducto: (String) -> Unit,
    onBuscar: (String) -> Unit
) {
    val viewModel = appViewModel { HomeViewModel(it.api) }
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var termino by remember { mutableStateOf("") }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.cargarInicial(context)
    }

    LaunchedEffect(Unit) {
        lanzadorPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("TrueQ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = { NotificationBell(noLeidas, onAbrirNotificaciones) }
            )
        },
        bottomBar = { BottomNavBar(rutaActual, onNavegar) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            OutlinedTextField(
                value = termino,
                onValueChange = { termino = it },
                placeholder = { Text("¿Qué quieres intercambiar hoy?") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onBuscar(termino) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CategoriaChip(
                        nombre = "Todos",
                        icono = Icons.Filled.Category,
                        activo = estado.categoriaSeleccionada == null,
                        onClick = { viewModel.seleccionarCategoria(null) }
                    )
                }
                items(estado.categorias) { categoria ->
                    CategoriaChip(
                        nombre = categoria.nombre,
                        icono = iconoCategoria(categoria.nombre),
                        activo = estado.categoriaSeleccionada == categoria.id,
                        onClick = { viewModel.seleccionarCategoria(categoria.id) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Explorar cerca de ti", style = MaterialTheme.typography.headlineMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onBuscar("") }
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(" Filtros", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                }
            }

            when {
                estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                estado.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(estado.error!!, color = MaterialTheme.colorScheme.error)
                }
                estado.productos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay artículos cerca de ti todavía.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(estado.productos) { producto ->
                        val distancia = estado.ubicacion?.let {
                            calcularDistanciaKm(it.latitud, it.longitud, producto.latitud, producto.longitud)
                        }
                        ProductCard(producto = producto, distanciaKm = distancia, onClick = { onAbrirProducto(producto.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriaChip(
    nombre: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    activo: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    if (activo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icono,
                contentDescription = nombre,
                tint = if (activo) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(nombre, style = MaterialTheme.typography.labelSmall)
    }
}
