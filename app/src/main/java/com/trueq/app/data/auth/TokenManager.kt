package com.trueq.app.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.trueq.app.data.model.JwtClaims
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "trueq_sesion")
private val TOKEN_KEY = stringPreferencesKey("token")

/**
 * Persiste el token de sesión (equivalente a localStorage["trueq_token"] en
 * el sitio web) y expone los claims decodificados del usuario actual.
 */
class TokenManager(private val context: Context) {

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }

    val usuarioFlow: Flow<JwtClaims?> = tokenFlow.map { token ->
        token?.let { decodeJwt(it) }
    }

    suspend fun setToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun logout() {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
    }

    /** Lectura síncrona para el interceptor de OkHttp. */
    fun tokenActualBloqueante(): String? = runBlocking { tokenFlow.first() }

    /**
     * Igual que `estaAutenticado` en stores/auth.ts: valida el claim `exp`
     * del JWT en vez de solo comprobar que exista un token guardado, para
     * que un token vencido no bloquee la pantalla de login.
     */
    suspend fun estaAutenticado(): Boolean {
        val token = tokenFlow.first() ?: return false
        val claims = decodeJwt(token) ?: return false
        val expira = claims.expira ?: return true
        return expira * 1000 > System.currentTimeMillis()
    }
}
