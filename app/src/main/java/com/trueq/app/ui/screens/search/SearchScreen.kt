@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.trueq.app.di.appViewModel
import com.trueq.app.ui.components.BottomNavBar
import com.trueq.app.ui.components.ProductCard
import com.trueq.app.ui.components.TopBarBack
import java.util.Locale

@Composable
fun SearchScreen(
    rutaActual: String,
    terminoInicial: String,
    onVolver: () -> Unit,
    onNavegar: (String) -> Unit,
    onAbrirProducto: (String) -> Unit
) {
    val viewModel = appViewModel { SearchViewModel(it.api) }
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.inicializar(context, terminoInicial)
    }

    Scaffold(
        topBar = { TopBarBack(titulo = "Buscar", onVolver = onVolver) },
        bottomBar = { BottomNavBar(rutaActual, onNavegar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = estado.termino,
                onValueChange = viewModel::onTerminoChange,
                placeholder = { Text("Buscar artículos...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Panel de filtros tipo acordeón: se puede ocultar para no
            // estorbar la vista de resultados.
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.alternarFiltros() }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(8.dp))
                            Text("Filtros", style = MaterialTheme.typography.headlineMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Limpiar todo",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.clickable { viewModel.limpiarFiltros() }
                            )
                            Spacer(Modifier.width(12.dp))
                            Icon(
                                Icons.Filled.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.rotate(if (estado.mostrarFiltros) 180f else 0f)
                            )
                        }
                    }

                    AnimatedVisibility(visible = estado.mostrarFiltros, enter = expandVertically(), exit = shrinkVertically()) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).padding(bottom = 16.dp)) {

                            Text("Categoría", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(6.dp))
                            var expandidoCategoria by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = expandidoCategoria, onExpandedChange = { expandidoCategoria = it }) {
                                OutlinedTextField(
                                    value = estado.categorias.find { it.id == estado.categoriaId }?.nombre ?: "Todas las categorías",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = expandidoCategoria, onDismissRequest = { expandidoCategoria = false }) {
                                    DropdownMenuItem(text = { Text("Todas las categorías") }, onClick = {
                                        viewModel.onCategoriaChange(null); expandidoCategoria = false
                                    })
                                    estado.categorias.forEach { categoria ->
                                        DropdownMenuItem(text = { Text(categoria.nombre) }, onClick = {
                                            viewModel.onCategoriaChange(categoria.id); expandidoCategoria = false
                                        })
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            Text("Tipo de transacción", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(6.dp))
                            FlowFiltros(
                                opciones = listOf(null to "Todos") + estado.tiposTransaccion.map { it.id to it.tipoTransaccion },
                                seleccionado = estado.tipoTransaccion,
                                onSeleccionar = viewModel::onTipoTransaccionChange
                            )

                            Spacer(Modifier.height(14.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Presupuesto máx.", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "$" + String.format(Locale("es", "MX"), "%,.0f", estado.presupuestoMax),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            Slider(value = estado.presupuestoMax, onValueChange = viewModel::onPresupuestoChange, valueRange = 0f..20000f)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Distancia", style = MaterialTheme.typography.labelMedium)
                                Text("${estado.distanciaKm.toInt()} km", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                            }
                            Slider(value = estado.distanciaKm, onValueChange = viewModel::onDistanciaChange, valueRange = 1f..100f)

                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.buscar() }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Aplicar filtros")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (estado.busquedaRealizada) {
                Text("Resultados", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))

                when {
                    estado.cargando -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    estado.resultados.isEmpty() -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No encontramos artículos con esos filtros.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    else -> {
                        val filas = estado.resultados.chunked(2)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            filas.forEach { fila ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    fila.forEach { producto ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            ProductCard(producto = producto, onClick = { onAbrirProducto(producto.id) })
                                        }
                                    }
                                    if (fila.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowFiltros(
    opciones: List<Pair<Int?, String>>,
    seleccionado: Int?,
    onSeleccionar: (Int?) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { (valor, etiqueta) ->
            FilterChip(
                selected = seleccionado == valor,
                onClick = { onSeleccionar(valor) },
                label = { Text(etiqueta) }
            )
        }
    }
}
