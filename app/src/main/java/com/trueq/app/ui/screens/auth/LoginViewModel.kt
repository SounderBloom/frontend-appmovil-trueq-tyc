@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueq.app.data.auth.TokenManager
import com.trueq.app.data.model.LoginDTO
import com.trueq.app.data.remote.TrueQApi
import com.trueq.app.data.remote.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val exito: Boolean = false
)

class LoginViewModel(
    private val api: TrueQApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(valor: String) {
        _uiState.value = _uiState.value.copy(correo = valor)
    }

    fun onPasswordChange(valor: String) {
        _uiState.value = _uiState.value.copy(password = valor)
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        _uiState.value = estado.copy(cargando = true, error = null)

        viewModelScope.launch {
            val resultado = safeApiCall { api.login(LoginDTO(estado.correo, estado.password)) }

            resultado.fold(
                onSuccess = { respuesta ->
                    val token = respuesta.data
                    if (token != null) {
                        tokenManager.setToken(token)
                        _uiState.value = _uiState.value.copy(cargando = false, exito = true)
                    } else {
                        _uiState.value = _uiState.value.copy(cargando = false, error = "Correo o contraseña incorrectos.")
                    }
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(cargando = false, error = "Correo o contraseña incorrectos.")
                }
            )
        }
    }
}
