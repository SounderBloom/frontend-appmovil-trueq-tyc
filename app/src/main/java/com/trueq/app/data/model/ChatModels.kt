package com.trueq.app.data.model

import kotlinx.serialization.Serializable

// Espejo de Data/Chats/EmisorMensaje.cs / EstadoMensaje.cs
object EmisorMensaje {
    const val Sistema = 0
    const val Vendedor = 1
    const val Comprador = 2
}

@Serializable
data class MensajeDTO(
    val id: Int,
    val contenido: String,
    val fechaEnvio: String,
    val emisor: Int,
    val estado: Int,
    val tieneArchivos: Boolean = false
)

@Serializable
data class ChatDTO(
    val id: String,
    val productoId: String? = null,
    val nombreProductoSnapshot: String = "",
    val imagenProductoSnapshot: String = "",
    val tipoTransaccionProductoSnapshot: Int = 0,
    val urlFotoUsuario: String = "",
    val ultimoMensaje: MensajeDTO? = null,
    val ultimoMovimiento: String = "",
    val esVendedor: Boolean = false
)
