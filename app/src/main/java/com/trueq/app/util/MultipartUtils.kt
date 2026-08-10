package com.trueq.app.util

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

fun parteTexto(nombre: String, valor: String): MultipartBody.Part {
    val body = valor.toRequestBody("text/plain".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(nombre, null, body)
}

/** Lee una imagen elegida por el usuario (Uri de content://) y la convierte
 * en las dos partes multipart que espera el backend: "Fotos[i].Orden" y
 * "Fotos[i].Foto". */
fun partesFoto(context: Context, uri: Uri, indice: Int, orden: Int): List<MultipartBody.Part> {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(uri) ?: "image/jpeg"
    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    val extension = mimeType.substringAfterLast('/').ifBlank { "jpg" }
    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

    return listOf(
        parteTexto("Fotos[$indice].Orden", orden.toString()),
        MultipartBody.Part.createFormData("Fotos[$indice].Foto", "foto_$indice.$extension", requestBody)
    )
}
