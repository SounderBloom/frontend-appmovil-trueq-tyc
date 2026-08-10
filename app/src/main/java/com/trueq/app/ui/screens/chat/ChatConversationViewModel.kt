@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueq.app.data.model.*
import com.trueq.app.data.remote.TrueQApi
import com.trueq.app.data.remote.safeApiCall
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatConversationUiState(
    val cargando: Boolean = true,
    val chat: ChatDTO? = null,
    val mensajes: List<MensajeDTO> = emptyList(),
    val propuestas: List<PropuestaDTO> = emptyList(),
    val texto: String = "",
    val enviando: Boolean = false,

    val mostrarFormularioOferta: Boolean = false,
    val tipoOferta: Int = TipoOferta.Trueque,
    val misProductosDisponibles: List<ProductoDTO> = emptyList(),
    val productoOfrecidoId: String? = null,
    val monto: String = "",
    val direccionMonto: Int = DireccionMonto.ProponentePagaAlVendedor,
    val mensajeOferta: String = "",
    val enviandoOferta: Boolean = false,
    val errorOferta: String? = null,

    val propuestaParaCalificar: PropuestaDTO? = null,
    val estrellas: Int = 0,
    val comentario: String = "",
    val enviandoCalificacion: Boolean = false,
    val calificacionEnviada: Boolean = false
)

class ChatConversationViewModel(private val api: TrueQApi) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatConversationUiState())
    val uiState: StateFlow<ChatConversationUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private lateinit var chatId: String

    fun cargar(chatIdParam: String, autoAbrirOferta: Boolean) {
        chatId = chatIdParam

        viewModelScope.launch {
            val resultado = safeApiCall { api.obtenerListaChats(0) }
            val chat = resultado.getOrNull()?.data?.find { it.id == chatIdParam }

            _uiState.value = _uiState.value.copy(cargando = false, chat = chat)

            if (chat != null) {
                cargarMensajes()
                cargarPropuestas()

                if (!chat.esVendedor && autoAbrirOferta && !tieneOfertaPendiente()) {
                    abrirFormularioOferta()
                }

                iniciarPolling()
            }
        }
    }

    private fun tieneOfertaPendiente() =
        _uiState.value.propuestas.any { it.estado == EstadoPropuesta.Pendiente }

    private fun iniciarPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(4000)
                cargarMensajes()
                cargarPropuestas()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    private suspend fun cargarMensajes() {
        safeApiCall { api.obtenerMensajes(chatId) }.onSuccess {
            _uiState.value = _uiState.value.copy(mensajes = it.data ?: emptyList())
        }
    }

    private suspend fun cargarPropuestas() {
        safeApiCall { api.obtenerPropuestasPorChat(chatId) }.onSuccess {
            _uiState.value = _uiState.value.copy(propuestas = it.data ?: emptyList())
        }
    }

    fun onTextoChange(v: String) { _uiState.value = _uiState.value.copy(texto = v) }

    fun enviarMensaje() {
        val texto = _uiState.value.texto.trim()
        if (texto.isBlank()) return

        _uiState.value = _uiState.value.copy(enviando = true)

        viewModelScope.launch {
            val partes = listOf(
                com.trueq.app.util.parteTexto("ChatId", chatId),
                com.trueq.app.util.parteTexto("Mensaje", texto),
                com.trueq.app.util.parteTexto("EsSistema", "false")
            )
            safeApiCall { api.enviarMensaje(partes) }
            _uiState.value = _uiState.value.copy(texto = "", enviando = false)
            cargarMensajes()
        }
    }

    // Un artículo publicado como "Donar" solo admite pedir la donación: sin
    // trueque, compra ni diferencia en efectivo (igual que en el sitio web).
    fun esProductoDeDonacionActual() = _uiState.value.chat?.tipoTransaccionProductoSnapshot == TipoTransaccion.Donar

    fun abrirFormularioOferta() {
        val esDonacion = esProductoDeDonacionActual()

        _uiState.value = _uiState.value.copy(
            mostrarFormularioOferta = true,
            tipoOferta = if (esDonacion) TipoOferta.SolicitudDonacion else TipoOferta.Trueque,
            productoOfrecidoId = null,
            monto = "",
            direccionMonto = DireccionMonto.ProponentePagaAlVendedor,
            mensajeOferta = "",
            errorOferta = null
        )

        if (!esDonacion) {
            viewModelScope.launch {
                safeApiCall { api.obtenerMisProductos() }.onSuccess { respuesta ->
                    val disponibles = (respuesta.data ?: emptyList()).filter { it.disponible }
                    _uiState.value = _uiState.value.copy(misProductosDisponibles = disponibles)
                }
            }
        }
    }

    fun cerrarFormularioOferta() {
        _uiState.value = _uiState.value.copy(mostrarFormularioOferta = false)
    }

    fun onTipoOfertaChange(v: Int) { _uiState.value = _uiState.value.copy(tipoOferta = v) }
    fun onProductoOfrecidoChange(v: String) { _uiState.value = _uiState.value.copy(productoOfrecidoId = v) }
    fun onMontoChange(v: String) { _uiState.value = _uiState.value.copy(monto = v) }
    fun onDireccionMontoChange(v: Int) { _uiState.value = _uiState.value.copy(direccionMonto = v) }
    fun onMensajeOfertaChange(v: String) { _uiState.value = _uiState.value.copy(mensajeOferta = v) }

    private fun requiereProducto(tipo: Int) = tipo == TipoOferta.Trueque || tipo == TipoOferta.TruequeConDiferencia
    private fun requiereMonto(tipo: Int) = tipo == TipoOferta.Compra || tipo == TipoOferta.TruequeConDiferencia

    fun requiereProductoActual() = requiereProducto(_uiState.value.tipoOferta)
    fun requiereMontoActual() = requiereMonto(_uiState.value.tipoOferta)

    fun enviarOferta() {
        val estado = _uiState.value

        if (requiereProducto(estado.tipoOferta) && estado.productoOfrecidoId == null) {
            _uiState.value = estado.copy(errorOferta = "Selecciona uno de tus artículos para ofrecer.")
            return
        }
        val montoDouble = estado.monto.toDoubleOrNull()
        if (requiereMonto(estado.tipoOferta) && (montoDouble == null || montoDouble <= 0)) {
            _uiState.value = estado.copy(errorOferta = "Indica un monto válido.")
            return
        }

        _uiState.value = estado.copy(enviandoOferta = true, errorOferta = null)

        viewModelScope.launch {
            val dto = CrearPropuestaDTO(
                chatId = chatId,
                tipoOferta = estado.tipoOferta,
                productoOfrecidoId = if (requiereProducto(estado.tipoOferta)) estado.productoOfrecidoId else null,
                monto = if (requiereMonto(estado.tipoOferta)) montoDouble else null,
                direccionMonto = if (estado.tipoOferta == TipoOferta.TruequeConDiferencia) estado.direccionMonto else null,
                mensaje = estado.mensajeOferta
            )

            val resultado = safeApiCall { api.crearPropuesta(dto) }

            resultado.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(enviandoOferta = false, mostrarFormularioOferta = false)
                    cargarPropuestas()
                    cargarMensajes()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(enviandoOferta = false, errorOferta = error.message ?: "No fue posible enviar la oferta.")
                }
            )
        }
    }

    fun responderOferta(propuesta: PropuestaDTO, aceptar: Boolean) {
        viewModelScope.launch {
            safeApiCall { api.responderPropuesta(propuesta.id, ResponderPropuestaDTO(aceptar)) }
            cargarPropuestas()
            cargarMensajes()
        }
    }

    fun abrirCalificar(propuesta: PropuestaDTO) {
        _uiState.value = _uiState.value.copy(
            propuestaParaCalificar = propuesta,
            estrellas = 0,
            comentario = "",
            calificacionEnviada = false
        )
    }

    fun cerrarCalificar() {
        _uiState.value = _uiState.value.copy(propuestaParaCalificar = null)
    }

    fun onEstrellasChange(v: Int) { _uiState.value = _uiState.value.copy(estrellas = v) }
    fun onComentarioChange(v: String) { _uiState.value = _uiState.value.copy(comentario = v) }

    fun enviarCalificacion() {
        val propuesta = _uiState.value.propuestaParaCalificar ?: return
        if (_uiState.value.estrellas < 1) return

        _uiState.value = _uiState.value.copy(enviandoCalificacion = true)

        viewModelScope.launch {
            val resultado = safeApiCall {
                api.crearCalificacion(CrearCalificacionDTO(propuesta.id, _uiState.value.estrellas, _uiState.value.comentario))
            }
            resultado.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(enviandoCalificacion = false, calificacionEnviada = true)
                    cargarPropuestas()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(enviandoCalificacion = false)
                }
            )
        }
    }

    fun esMensajePropio(mensaje: MensajeDTO): Boolean {
        val chat = _uiState.value.chat ?: return false
        return (chat.esVendedor && mensaje.emisor == EmisorMensaje.Vendedor) ||
            (!chat.esVendedor && mensaje.emisor == EmisorMensaje.Comprador)
    }
}
