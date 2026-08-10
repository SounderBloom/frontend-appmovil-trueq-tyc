@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueq.app.data.model.NotificacionDTO
import com.trueq.app.data.remote.TrueQApi
import com.trueq.app.data.remote.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val notificaciones: List<NotificacionDTO> = emptyList()
)

class NotificationsViewModel(private val api: TrueQApi) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun cargar() {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            val resultado = safeApiCall { api.obtenerNotificaciones(0) }
            resultado.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(cargando = false, notificaciones = it.data ?: emptyList()) },
                onFailure = { _uiState.value = _uiState.value.copy(cargando = false, error = "No fue posible cargar tus notificaciones.") }
            )
        }
    }

    fun marcarLeida(notificacion: NotificacionDTO) {
        if (notificacion.leida) return
        _uiState.value = _uiState.value.copy(
            notificaciones = _uiState.value.notificaciones.map {
                if (it.id == notificacion.id) it.copy(leida = true) else it
            }
        )
        viewModelScope.launch { safeApiCall { api.marcarNotificacionLeida(notificacion.id) } }
    }

    fun marcarTodasLeidas() {
        _uiState.value = _uiState.value.copy(notificaciones = _uiState.value.notificaciones.map { it.copy(leida = true) })
        viewModelScope.launch { safeApiCall { api.marcarTodasLeidas() } }
    }
}
