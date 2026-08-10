package com.trueq.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginDTO(
    val correo: String,
    val password: String
)

@Serializable
data class RegisterDTO(
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val correo: String,
    val password: String
)

// Claims decodificados del JWT (equivalente a src/utils/jwt.ts en el sitio web).
data class JwtClaims(
    val nameIdentifier: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val expira: Long?
)
