package com.trueq.app.data.remote

import com.trueq.app.data.auth.TokenManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

// Origen del backend (sin /api). Usa 10.0.2.2 para el emulador de Android,
// que apunta al "localhost" de tu PC. Si pruebas en un dispositivo físico o
// despliegas la API en otro lado, cambia esto.
const val API_ORIGIN = "https://api-trueq-final.onrender.com"
private const val API_BASE_URL = "$API_ORIGIN/api/"

val jsonConfig = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

/**
 * Resuelve una ruta relativa devuelta por el backend (p. ej.
 * "/Uploads/Productos/x.webp") a una URL absoluta contra el origen de la API.
 * Equivalente a resolverUrlArchivo() en src/services/api.ts.
 */
fun resolverUrlArchivo(ruta: String?): String? {
    if (ruta.isNullOrBlank()) return null
    if (ruta.startsWith("http://") || ruta.startsWith("https://")) return ruta
    return if (ruta.startsWith("/")) "$API_ORIGIN$ruta" else "$API_ORIGIN/$ruta"
}

object RetrofitProvider {

    fun crear(tokenManager: TokenManager): TrueQApi {

        val authInterceptor = okhttp3.Interceptor { chain ->
            val token = tokenManager.tokenActualBloqueante()
            val request = if (!token.isNullOrBlank()) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(client)
            .addConverterFactory(jsonConfig.asConverterFactory(contentType))
            .build()
            .create(TrueQApi::class.java)
    }
}
