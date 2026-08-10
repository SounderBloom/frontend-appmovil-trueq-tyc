@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueq.app.data.model.ChatDTO
import com.trueq.app.data.model.ProductoDTO
import com.trueq.app.data.model.ResumenCalificacionesDTO
import com.trueq.app.data.remote.TrueQApi
import com.trueq.app.data.remote.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val cargando: Boolean = true,
    val producto: ProductoDTO? = null,
    val noEncontrado: Boolean = false,
    val calificacionVendedor: ResumenCalificacionesDTO? = null,
    val iniciandoAccion: Boolean = false,
    val mensajeAccion: String? = null,
    val chatListoId: String? = null
)

class ProductDetailViewModel(private val api: TrueQApi) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        viewModelScope.launch {
            val resultado = safeApiCall { api.obtenerProductoPorId(id) }

            resultado.fold(
                onSuccess = { respuesta ->
                    val producto = respuesta.data
                    _uiState.value = _uiState.value.copy(cargando = false, producto = producto, noEncontrado = producto == null)

                    if (producto != null) {
                        safeApiCall { api.obtenerCalificacionesDeUsuario(producto.vendedorId) }.onSuccess {
                            _uiState.value = _uiState.value.copy(calificacionVendedor = it.data)
                        }
                    }
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(cargando = false, noEncontrado = true)
                }
            )
        }
    }

    /** Crea o reutiliza el chat con el vendedor y navega, opcionalmente abriendo el compositor de oferta. */
    fun irAlChat(conProponer: Boolean) {
        val producto = _uiState.value.producto ?: return

        _uiState.value = _uiState.value.copy(iniciandoAccion = true, mensajeAccion = null, chatListoId = null)

        viewModelScope.launch {
            val resultado = safeApiCall { api.crearChat(producto.id) }

            resultado.fold(
                onSuccess = { respuesta ->
                    val chatId = respuesta.data
                    _uiState.value = _uiState.value.copy(
                        iniciandoAccion = false,
                        chatListoId = if (chatId != null) "$chatId|${if (conProponer) 1 else 0}" else null
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(iniciandoAccion = false, mensajeAccion = "No fue posible abrir el chat con el vendedor.")
                }
            )
        }
    }

    fun consumirNavegacionChat() {
        _uiState.value = _uiState.value.copy(chatListoId = null)
    }
}
