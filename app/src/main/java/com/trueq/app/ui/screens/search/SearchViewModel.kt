@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueq.app.data.model.CategoriaDTO
import com.trueq.app.data.model.ProductoDTO
import com.trueq.app.data.model.TipoTransaccion
import com.trueq.app.data.model.TipoTransaccionDTO
import com.trueq.app.data.remote.TrueQApi
import com.trueq.app.data.remote.safeApiCall
import com.trueq.app.util.Coordenadas
import com.trueq.app.util.obtenerUbicacionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val termino: String = "",
    val categorias: List<CategoriaDTO> = emptyList(),
    val categoriaId: Int? = null,
    val tiposTransaccion: List<TipoTransaccionDTO> = emptyList(),
    val tipoTransaccion: Int? = null,
    val presupuestoMax: Float = 5000f,
    val distanciaKm: Float = 15f,
    val cargando: Boolean = false,
    val busquedaRealizada: Boolean = false,
    val resultados: List<ProductoDTO> = emptyList(),
    val mostrarFiltros: Boolean = true,
    val ubicacion: Coordenadas? = null
)

class SearchViewModel(private val api: TrueQApi) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun inicializar(context: Context, terminoInicial: String) {
        viewModelScope.launch {
            val ubicacion = obtenerUbicacionActual(context)
            _uiState.value = _uiState.value.copy(ubicacion = ubicacion, termino = terminoInicial)
            cargarCatalogos()
            if (terminoInicial.isNotBlank()) buscar()
        }
    }

    private suspend fun cargarCatalogos() {
        safeApiCall { api.obtenerCategorias() }.onSuccess {
            _uiState.value = _uiState.value.copy(categorias = it.data ?: emptyList())
        }
        safeApiCall { api.obtenerTiposTransaccion() }.onSuccess {
            _uiState.value = _uiState.value.copy(tiposTransaccion = it.data ?: emptyList())
        }
    }

    fun onTerminoChange(v: String) { _uiState.value = _uiState.value.copy(termino = v) }
    fun onCategoriaChange(v: Int?) { _uiState.value = _uiState.value.copy(categoriaId = v) }
    fun onTipoTransaccionChange(v: Int?) { _uiState.value = _uiState.value.copy(tipoTransaccion = v) }
    fun onPresupuestoChange(v: Float) { _uiState.value = _uiState.value.copy(presupuestoMax = v) }
    fun onDistanciaChange(v: Float) { _uiState.value = _uiState.value.copy(distanciaKm = v) }
    fun alternarFiltros() { _uiState.value = _uiState.value.copy(mostrarFiltros = !_uiState.value.mostrarFiltros) }

    fun limpiarFiltros() {
        _uiState.value = _uiState.value.copy(
            categoriaId = null,
            tipoTransaccion = null,
            presupuestoMax = 5000f,
            distanciaKm = 15f
        )
    }

    fun buscar() {
        val estado = _uiState.value
        val ubicacion = estado.ubicacion ?: Coordenadas(19.4326, -99.1332)

        _uiState.value = estado.copy(cargando = true, busquedaRealizada = true)

        viewModelScope.launch {
            val resultado = safeApiCall {
                api.buscarProductos(
                    latitud = ubicacion.latitud,
                    longitud = ubicacion.longitud,
                    radio = estado.distanciaKm.toDouble(),
                    cantidadPorPagina = 30,
                    categoriaId = estado.categoriaId,
                    tipoTransaccion = estado.tipoTransaccion
                )
            }

            resultado.fold(
                onSuccess = { respuesta ->
                    val productos = (respuesta.data?.productos ?: emptyList()).filter { producto ->
                        val dentroPresupuesto = producto.tipoTransaccion == TipoTransaccion.Trueque ||
                            producto.precio <= estado.presupuestoMax
                        val coincideTermino = estado.termino.isBlank() ||
                            producto.titulo.contains(estado.termino, ignoreCase = true)
                        dentroPresupuesto && coincideTermino
                    }
                    _uiState.value = _uiState.value.copy(cargando = false, resultados = productos, mostrarFiltros = false)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(cargando = false)
                }
            )
        }
    }
}
