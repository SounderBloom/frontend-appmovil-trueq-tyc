package com.trueq.app.data.auth

import android.util.Base64
import com.trueq.app.data.model.JwtClaims
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val CLAIM_NAME_IDENTIFIER = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier"
private const val CLAIM_NAME = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"
private const val CLAIM_EMAIL = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress"
private const val CLAIM_ROLE = "http://schemas.microsoft.com/ws/2008/06/identity/claims/role"

private val json = Json { ignoreUnknownKeys = true }

/**
 * Decodifica un JWT (sin validar la firma, eso lo hace el backend) para leer
 * los claims del usuario autenticado, igual que src/utils/jwt.ts en el
 * sitio web.
 */
fun decodeJwt(token: String): JwtClaims? {
    return try {
        val parts = token.split(".")
        if (parts.size < 2) return null

        val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val payloadJson = String(payloadBytes, Charsets.UTF_8)
        val payload = json.parseToJsonElement(payloadJson) as JsonObject

        fun field(vararg keys: String): String {
            for (key in keys) {
                payload[key]?.jsonPrimitive?.content?.let { return it }
            }
            return ""
        }

        val expira = payload["exp"]?.jsonPrimitive?.content?.toLongOrNull()

        JwtClaims(
            nameIdentifier = field(CLAIM_NAME_IDENTIFIER, "nameid"),
            nombre = field(CLAIM_NAME, "name"),
            correo = field(CLAIM_EMAIL, "email"),
            rol = field(CLAIM_ROLE, "role"),
            expira = expira
        )
    } catch (e: Exception) {
        null
    }
}
