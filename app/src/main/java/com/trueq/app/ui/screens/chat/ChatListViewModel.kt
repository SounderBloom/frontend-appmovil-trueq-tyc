@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueq.app.data.model.ChatDTO
import com.trueq.app.data.remote.TrueQApi
import com.trueq.app.data.remote.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatListUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val chats: List<ChatDTO> = emptyList()
)

class ChatListViewModel(private val api: TrueQApi) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    fun cargar() {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            val resultado = safeApiCall { api.obtenerListaChats(0) }
            resultado.fold(
                onSuccess = { respuesta ->
                    _uiState.value = _uiState.value.copy(cargando = false, chats = respuesta.data ?: emptyList())
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(cargando = false, error = "No fue posible cargar tus mensajes.")
                }
            )
        }
    }
}
