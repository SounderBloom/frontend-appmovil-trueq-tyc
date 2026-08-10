@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueq.app.data.auth.TokenManager
import com.trueq.app.data.model.CalificacionDTO
import com.trueq.app.data.model.PerfilUsuarioDTO
import com.trueq.app.data.model.ProductoDTO
import com.trueq.app.data.remote.TrueQApi
import com.trueq.app.data.remote.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val perfil: PerfilUsuarioDTO? = null,
    val misProductos: List<ProductoDTO> = emptyList(),
    val resenas: List<CalificacionDTO> = emptyList(),
    val cerrandoSesion: Boolean = false
)

class ProfileViewModel(
    private val api: TrueQApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun cargar() {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)

        viewModelScope.launch {
            val perfilResultado = safeApiCall { api.miPerfil() }
            val productosResultado = safeApiCall { api.obtenerMisProductos() }

            val perfil = perfilResultado.getOrNull()?.data
            val productos = productosResultado.getOrNull()?.data ?: emptyList()

            if (perfil == null) {
                _uiState.value = _uiState.value.copy(cargando = false, error = "No fue posible cargar tu perfil.")
                return@launch
            }

            val resenas = safeApiCall { api.obtenerCalificacionesDeUsuario(perfil.id) }
                .getOrNull()?.data?.recientes ?: emptyList()

            _uiState.value = _uiState.value.copy(
                cargando = false,
                perfil = perfil,
                misProductos = productos,
                resenas = resenas
            )
        }
    }

    fun cerrarSesion(alTerminar: () -> Unit) {
        _uiState.value = _uiState.value.copy(cerrandoSesion = true)
        viewModelScope.launch {
            tokenManager.logout()
            alTerminar()
        }
    }
}
