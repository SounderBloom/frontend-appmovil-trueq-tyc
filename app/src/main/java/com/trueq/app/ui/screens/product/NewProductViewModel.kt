@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.screens.product

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueq.app.data.model.CategoriaDTO
import com.trueq.app.data.model.TipoTransaccion
import com.trueq.app.data.model.TipoTransaccionDTO
import com.trueq.app.data.remote.TrueQApi
import com.trueq.app.data.remote.safeApiCall
import com.trueq.app.util.obtenerUbicacionActual
import com.trueq.app.util.parteTexto
import com.trueq.app.util.partesFoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewProductUiState(
    val titulo: String = "",
    val precio: String = "",
    val descripcion: String = "",
    val categoriaId: Int? = null,
    val tipoTransaccion: Int = TipoTransaccion.Trueque,
    val categorias: List<CategoriaDTO> = emptyList(),
    val tiposTransaccion: List<TipoTransaccionDTO> = emptyList(),
    // El orden de esta lista ES el campo Orden: la foto en el índice 0 es
    // la principal / se muestra primero en el detalle.
    val fotos: List<Uri> = emptyList(),
    val ubicacionLista: Boolean = false,
    val obteniendoUbicacion: Boolean = true,
    val enviando: Boolean = false,
    val mensaje: String? = null,
    val exito: Boolean = false
)

class NewProductViewModel(private val api: TrueQApi) : ViewModel() {

    private val _uiState = MutableStateFlow(NewProductUiState())
    val uiState: StateFlow<NewProductUiState> = _uiState.asStateFlow()

    private var latitud: Double? = null
    private var longitud: Double? = null

    fun inicializar(context: Context) {
        viewModelScope.launch {
            safeApiCall { api.obtenerCategorias() }.onSuccess {
                _uiState.value = _uiState.value.copy(categorias = it.data ?: emptyList())
            }
            safeApiCall { api.obtenerTiposTransaccion() }.onSuccess {
                _uiState.value = _uiState.value.copy(tiposTransaccion = it.data ?: emptyList())
            }

            val ubicacion = obtenerUbicacionActual(context)
            latitud = ubicacion.latitud
            longitud = ubicacion.longitud
            _uiState.value = _uiState.value.copy(obteniendoUbicacion = false, ubicacionLista = true)
        }
    }

    fun onTituloChange(v: String) { _uiState.value = _uiState.value.copy(titulo = v) }
    fun onPrecioChange(v: String) { _uiState.value = _uiState.value.copy(precio = v) }
    fun onDescripcionChange(v: String) { _uiState.value = _uiState.value.copy(descripcion = v) }
    fun onCategoriaChange(v: Int) { _uiState.value = _uiState.value.copy(categoriaId = v) }
    fun onTipoTransaccionChange(v: Int) { _uiState.value = _uiState.value.copy(tipoTransaccion = v) }

    fun requierePrecio(): Boolean {
        val tipo = _uiState.value.tipoTransaccion
        return tipo == TipoTransaccion.Venta || tipo == TipoTransaccion.TruequeOVenta
    }

    fun agregarFotos(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(fotos = _uiState.value.fotos + uris)
    }

    fun quitarFoto(indice: Int) {
        _uiState.value = _uiState.value.copy(fotos = _uiState.value.fotos.toMutableList().apply { removeAt(indice) })
    }

    fun moverFoto(indice: Int, direccion: Int) {
        val destino = indice + direccion
        val lista = _uiState.value.fotos.toMutableList()
        if (destino < 0 || destino >= lista.size) return
        val temp = lista[indice]
        lista[indice] = lista[destino]
        lista[destino] = temp
        _uiState.value = _uiState.value.copy(fotos = lista)
    }

    fun publicar(context: Context) {
        val estado = _uiState.value

        if (latitud == null || longitud == null) {
            _uiState.value = estado.copy(mensaje = "Debes permitir el acceso a tu ubicación para publicar.")
            return
        }
        if (estado.categoriaId == null) {
            _uiState.value = estado.copy(mensaje = "Selecciona una categoría.")
            return
        }
        if (estado.titulo.isBlank() || estado.descripcion.isBlank()) {
            _uiState.value = estado.copy(mensaje = "Completa el título y la descripción.")
            return
        }

        _uiState.value = estado.copy(enviando = true, mensaje = null)

        viewModelScope.launch {
            val precio = if (requierePrecio()) (estado.precio.toDoubleOrNull() ?: 0.0) else 0.0

            val partes = mutableListOf(
                parteTexto("Titulo", estado.titulo),
                parteTexto("Precio", precio.toString()),
                parteTexto("Descripcion", estado.descripcion),
                parteTexto("CategoriaId", estado.categoriaId.toString()),
                parteTexto("TipoTransaccion", estado.tipoTransaccion.toString()),
                parteTexto("Latitud", latitud.toString()),
                parteTexto("Longitud", longitud.toString())
            )
            estado.fotos.forEachIndexed { indice, uri ->
                partes += partesFoto(context, uri, indice, indice + 1)
            }

            val resultado = safeApiCall { api.crearProducto(partes) }

            resultado.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(enviando = false, mensaje = "Producto publicado correctamente.", exito = true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(enviando = false, mensaje = error.message ?: "No fue posible publicar el producto.")
                }
            )
        }
    }
}
