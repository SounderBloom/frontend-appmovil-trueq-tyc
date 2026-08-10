package com.trueq.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CalificacionDTO(
    val id: Int,
    val calificadorId: String,
    val calificadorNombre: String = "",
    val estrellas: Int,
    val comentario: String = "",
    val fechaCreacion: String
)

@Serializable
data class ResumenCalificacionesDTO(
    val promedio: Double = 0.0,
    val total: Int = 0,
    val recientes: List<CalificacionDTO> = emptyList()
)

@Serializable
data class CrearCalificacionDTO(
    val propuestaId: String,
    val estrellas: Int,
    val comentario: String = ""
)
