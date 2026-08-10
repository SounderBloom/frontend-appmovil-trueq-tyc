@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueq.app.data.model.RegisterDTO
import com.trueq.app.data.remote.TrueQApi
import com.trueq.app.data.remote.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val nombre: String = "",
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val correo: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val exito: Boolean = false
)

class RegisterViewModel(private val api: TrueQApi) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNombreChange(v: String) { _uiState.value = _uiState.value.copy(nombre = v) }
    fun onApellidoPaternoChange(v: String) { _uiState.value = _uiState.value.copy(apellidoPaterno = v) }
    fun onApellidoMaternoChange(v: String) { _uiState.value = _uiState.value.copy(apellidoMaterno = v) }
    fun onCorreoChange(v: String) { _uiState.value = _uiState.value.copy(correo = v) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v) }

    fun registrar() {
        val estado = _uiState.value
        _uiState.value = estado.copy(cargando = true, error = null)

        viewModelScope.launch {
            val resultado = safeApiCall {
                api.register(
                    RegisterDTO(
                        nombre = estado.nombre,
                        apellidoPaterno = estado.apellidoPaterno,
                        apellidoMaterno = estado.apellidoMaterno,
                        correo = estado.correo,
                        password = estado.password
                    )
                )
            }

            resultado.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(cargando = false, exito = true) },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = error.message ?: "No fue posible registrar la cuenta."
                    )
                }
            )
        }
    }
}
