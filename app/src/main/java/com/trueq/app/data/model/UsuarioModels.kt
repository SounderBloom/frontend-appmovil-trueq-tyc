package com.trueq.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PerfilUsuarioDTO(
    val id: String,
    val nombre: String,
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val correo: String,
    val biografia: String = "",
    val fotoPerfilUrl: String = "",
    val fechaRegistro: String,
    val rol: String,
    val truequesRealizados: Int = 0,
    val articulosActivos: Int = 0,
    val promedioCalificacion: Double = 0.0,
    val totalCalificaciones: Int = 0
)
