package com.trueq.app.data.model

import kotlinx.serialization.Serializable

// Espejo de Data/Propuestas/EstadoPropuesta.cs
object EstadoPropuesta {
    const val Pendiente = 0
    const val Aceptada = 1
    const val Rechazada = 2
}

// Espejo de Data/Propuestas/TipoOferta.cs
object TipoOferta {
    const val Trueque = 0
    const val Compra = 1
    const val TruequeConDiferencia = 2
    // Pedir un producto publicado como Donar: sin producto ofrecido ni
    // monto, solo la solicitud para que el vendedor decida a quién dona.
    const val SolicitudDonacion = 3
}

// Espejo de Data/Propuestas/DireccionMonto.cs
object DireccionMonto {
    const val ProponentePagaAlVendedor = 0
    const val VendedorPagaAlProponente = 1
}

@Serializable
data class PropuestaDTO(
    val id: String,
    val chatId: String,
    val productoSolicitadoId: String,
    val productoSolicitadoTitulo: String = "",
    val productoSolicitadoFoto: String? = null,
    val tipoOferta: Int,
    val productoOfrecidoId: String? = null,
    val productoOfrecidoTitulo: String? = null,
    val productoOfrecidoFoto: String? = null,
    val monto: Double? = null,
    val direccionMonto: Int? = null,
    val proponenteId: String,
    val vendedorId: String,
    val mensaje: String = "",
    val estado: Int,
    val fechaCreacion: String,
    val fechaResolucion: String? = null,
    val puedeCalificar: Boolean = false
)

@Serializable
data class CrearPropuestaDTO(
    val chatId: String,
    val tipoOferta: Int,
    val productoOfrecidoId: String? = null,
    val monto: Double? = null,
    val direccionMonto: Int? = null,
    val mensaje: String = ""
)

@Serializable
data class ResponderPropuestaDTO(
    val aceptar: Boolean
)
