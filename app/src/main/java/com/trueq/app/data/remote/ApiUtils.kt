package com.trueq.app.data.remote

import com.trueq.app.data.model.ResponseWrapper
import kotlinx.serialization.json.JsonElement
import retrofit2.HttpException
import java.io.IOException

/**
 * Ejecuta una llamada suspend de Retrofit y devuelve un Result con el
 * mensaje de error legible que ya manda el backend en ResponseWrapper.Message
 * (equivalente a leer err.response?.data?.message en el sitio web).
 */
suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
    return try {
        Result.success(call())
    } catch (e: HttpException) {
        val mensaje = try {
            val body = e.response()?.errorBody()?.string()
            body?.let {
                jsonConfig.decodeFromString(ResponseWrapper.serializer(JsonElement.serializer()), it).message
            }
        } catch (parseError: Exception) {
            null
        }
        Result.failure(Exception(mensaje?.ifBlank { null } ?: "Ocurrió un error inesperado."))
    } catch (e: IOException) {
        Result.failure(Exception("No fue posible conectar con el servidor."))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
