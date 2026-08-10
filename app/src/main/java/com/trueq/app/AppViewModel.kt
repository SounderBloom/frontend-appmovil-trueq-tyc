package com.trueq.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueq.app.data.auth.TokenManager
import com.trueq.app.data.remote.TrueQApi
import com.trueq.app.data.remote.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado compartido a nivel de toda la app: si hay sesión activa (para
 * decidir la pantalla inicial, igual que el guard del router en el sitio
 * web) y el conteo de notificaciones no leídas para el badge de la campana.
 */
class AppViewModel(
    private val api: TrueQApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _estaAutenticado = MutableStateFlow<Boolean?>(null)
    val estaAutenticado: StateFlow<Boolean?> = _estaAutenticado.asStateFlow()

    private val _noLeidas = MutableStateFlow(0)
    val noLeidas: StateFlow<Int> = _noLeidas.asStateFlow()

    init {
        viewModelScope.launch {
            _estaAutenticado.value = tokenManager.estaAutenticado()
        }
    }

    fun refrescarSesion() {
        viewModelScope.launch {
            _estaAutenticado.value = tokenManager.estaAutenticado()
        }
    }

    fun refrescarNoLeidas() {
        viewModelScope.launch {
            safeApiCall { api.contarNoLeidas() }.onSuccess { _noLeidas.value = it.data ?: 0 }
        }
    }

    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            tokenManager.logout()
            _estaAutenticado.value = false
            alTerminar()
        }
    }
}
