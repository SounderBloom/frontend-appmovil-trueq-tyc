package com.trueq.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoriaDTO(
    val id: Int,
    val nombre: String
)

@Serializable
data class TipoTransaccionDTO(
    val id: Int,
    val tipoTransaccion: String
)

@Serializable
data class ProductoDTO(
    val id: String,
    val titulo: String,
    val precio: Double,
    val descripcion: String,
    val disponible: Boolean,
    val fechaPublicacion: String,
    val tipoTransaccion: Int,
    val vendedorId: String,
    val categoriaId: Int,
    val nombreCategoria: String? = null,
    val latitud: Double,
    val longitud: Double,
    val fotos: List<String> = emptyList()
)

@Serializable
data class PaginatedProductosDTO(
    val productos: List<ProductoDTO>,
    val paginaActual: Int,
    val cantidadPorPagina: Int,
    val totalRegistros: Int,
    val totalPaginas: Int
)

// Espejo de Data/Producto/TipoTransaccion.cs
object TipoTransaccion {
    const val Trueque = 0
    const val Venta = 1
    const val TruequeOVenta = 2
    const val Donar = 3
}
