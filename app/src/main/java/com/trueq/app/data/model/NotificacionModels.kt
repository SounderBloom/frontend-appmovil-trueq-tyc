package com.trueq.app.data.model

import kotlinx.serialization.Serializable

// Espejo de Data/Notificacion/TipoNotificacion.cs
object TipoNotificacion {
    const val Sistema = 0
    const val MensajeNuevo = 1
    const val PropuestaRecibida = 2
    const val PropuestaRespondida = 3
    const val CalificacionRecibida = 4
}

@Serializable
data class NotificacionDTO(
    val id: Int,
    val titulo: String,
    val contenido: String,
    val leida: Boolean,
    val tipo: Int,
    val referenciaId: String? = null,
    val fechaCreacion: String,
    val urlImagenIcono: String = ""
)
