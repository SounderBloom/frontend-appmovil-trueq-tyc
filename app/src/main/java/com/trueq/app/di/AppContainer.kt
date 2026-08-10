package com.trueq.app.di

import android.content.Context
import com.trueq.app.data.auth.TokenManager
import com.trueq.app.data.remote.RetrofitProvider
import com.trueq.app.data.remote.TrueQApi

/**
 * Contenedor de dependencias manual y simple (sin Hilt, para reducir puntos
 * de falla en un proyecto que no se pudo compilar/probar en este entorno).
 */
class AppContainer(context: Context) {
    val tokenManager: TokenManager = TokenManager(context.applicationContext)
    val api: TrueQApi = RetrofitProvider.crear(tokenManager)
}
