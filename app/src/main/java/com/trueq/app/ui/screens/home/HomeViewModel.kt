@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueq.app.data.model.CategoriaDTO
import com.trueq.app.data.model.ProductoDTO
import com.trueq.app.data.remote.TrueQApi
import com.trueq.app.data.remote.safeApiCall
import com.trueq.app.util.Coordenadas
import com.trueq.app.util.obtenerUbicacionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val categorias: List<CategoriaDTO> = emptyList(),
    val categoriaSeleccionada: Int? = null,
    val productos: List<ProductoDTO> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null,
    val ubicacion: Coordenadas? = null
)

class HomeViewModel(private val api: TrueQApi) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun cargarInicial(context: Context) {
        viewModelScope.launch {
            val ubicacion = obtenerUbicacionActual(context)
            _uiState.value = _uiState.value.copy(ubicacion = ubicacion)
            cargarCategorias()
            cargarProductos()
        }
    }

    private suspend fun cargarCategorias() {
        safeApiCall { api.obtenerCategorias() }.onSuccess { respuesta ->
            _uiState.value = _uiState.value.copy(categorias = respuesta.data ?: emptyList())
        }
    }

    fun seleccionarCategoria(categoriaId: Int?) {
        _uiState.value = _uiState.value.copy(categoriaSeleccionada = categoriaId)
        viewModelScope.launch { cargarProductos() }
    }

    private suspend fun cargarProductos() {
        val ubicacion = _uiState.value.ubicacion ?: Coordenadas(19.4326, -99.1332)
        _uiState.value = _uiState.value.copy(cargando = true, error = null)

        val resultado = safeApiCall {
            api.buscarProductos(
                latitud = ubicacion.latitud,
                longitud = ubicacion.longitud,
                radio = 50.0,
                cantidadPorPagina = 20,
                categoriaId = _uiState.value.categoriaSeleccionada
            )
        }

        resultado.fold(
            onSuccess = { respuesta ->
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    productos = respuesta.data?.productos ?: emptyList()
                )
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(cargando = false, error = "No fue posible cargar los artículos cercanos.")
            }
        )
    }
}
