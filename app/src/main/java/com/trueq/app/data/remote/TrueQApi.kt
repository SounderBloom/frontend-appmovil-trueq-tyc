package com.trueq.app.data.remote

import com.trueq.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface TrueQApi {

    // ---------- Auth ----------
    @POST("Auth/login")
    suspend fun login(@Body dto: LoginDTO): ResponseWrapper<String>

    @POST("Auth/register")
    suspend fun register(@Body dto: RegisterDTO): ResponseWrapper<Boolean>

    // ---------- Usuarios ----------
    @GET("Usuarios/MiPerfil")
    suspend fun miPerfil(): ResponseWrapper<PerfilUsuarioDTO>

    // ---------- Categorias ----------
    @GET("Categorias/ObtenerTodas")
    suspend fun obtenerCategorias(): ResponseWrapper<List<CategoriaDTO>>

    // ---------- Productos ----------
    @GET("Productos/TiposTransaccion")
    suspend fun obtenerTiposTransaccion(): ResponseWrapper<List<TipoTransaccionDTO>>

    @Multipart
    @POST("Productos/Crear")
    suspend fun crearProducto(@Part parts: List<MultipartBody.Part>): ResponseWrapper<Boolean>

    @GET("Productos/Buscar")
    suspend fun buscarProductos(
        @Query("latitud") latitud: Double,
        @Query("longitud") longitud: Double,
        @Query("radio") radio: Double,
        @Query("pagina") pagina: Int = 1,
        @Query("cantidadPorPagina") cantidadPorPagina: Int = 20,
        @Query("categoriaId") categoriaId: Int? = null,
        @Query("tipoTransaccion") tipoTransaccion: Int? = null
    ): ResponseWrapper<PaginatedProductosDTO>

    @GET("Productos/MisProductos")
    suspend fun obtenerMisProductos(): ResponseWrapper<List<ProductoDTO>>

    @GET("Productos/Obtener/{id}")
    suspend fun obtenerProductoPorId(@Path("id") id: String): ResponseWrapper<ProductoDTO>

    @DELETE("Productos/Eliminar")
    suspend fun eliminarProducto(@Query("ProductoId") productoId: String): ResponseWrapper<Boolean>

    // ---------- Chats ----------
    @GET("Chats/ObtenerListaChats")
    suspend fun obtenerListaChats(@Query("pagina") pagina: Int = 0): ResponseWrapper<List<ChatDTO>>

    @POST("Chats/Crear/{productoId}")
    suspend fun crearChat(@Path("productoId") productoId: String): ResponseWrapper<String>

    @GET("Chats/ObtenerMensajes/{chatId}")
    suspend fun obtenerMensajes(@Path("chatId") chatId: String): ResponseWrapper<List<MensajeDTO>>

    @Multipart
    @POST("Chats/EnviarMensaje")
    suspend fun enviarMensaje(@Part parts: List<MultipartBody.Part>): ResponseWrapper<Boolean>

    @DELETE("Chats/Borrar/{chatId}")
    suspend fun borrarChat(@Path("chatId") chatId: String): ResponseWrapper<Boolean>

    // ---------- Propuestas ----------
    @POST("Propuestas/Crear")
    suspend fun crearPropuesta(@Body dto: CrearPropuestaDTO): ResponseWrapper<PropuestaDTO>

    @GET("Propuestas/ObtenerPorChat/{chatId}")
    suspend fun obtenerPropuestasPorChat(@Path("chatId") chatId: String): ResponseWrapper<List<PropuestaDTO>>

    @POST("Propuestas/Responder/{propuestaId}")
    suspend fun responderPropuesta(
        @Path("propuestaId") propuestaId: String,
        @Body dto: ResponderPropuestaDTO
    ): ResponseWrapper<Boolean>

    @GET("Propuestas/PendientesDeCalificar")
    suspend fun propuestasPendientesDeCalificar(): ResponseWrapper<List<PropuestaDTO>>

    // ---------- Calificaciones ----------
    @POST("Calificaciones/Crear")
    suspend fun crearCalificacion(@Body dto: CrearCalificacionDTO): ResponseWrapper<Boolean>

    @GET("Calificaciones/ObtenerDeUsuario/{usuarioId}")
    suspend fun obtenerCalificacionesDeUsuario(@Path("usuarioId") usuarioId: String): ResponseWrapper<ResumenCalificacionesDTO>

    // ---------- Notificaciones ----------
    @GET("Notificaciones/Obtener")
    suspend fun obtenerNotificaciones(@Query("pagina") pagina: Int = 0): ResponseWrapper<List<NotificacionDTO>>

    @GET("Notificaciones/ContarNoLeidas")
    suspend fun contarNoLeidas(): ResponseWrapper<Int>

    @POST("Notificaciones/MarcarLeida/{id}")
    suspend fun marcarNotificacionLeida(@Path("id") id: Int): ResponseWrapper<Boolean>

    @POST("Notificaciones/MarcarTodasLeidas")
    suspend fun marcarTodasLeidas(): ResponseWrapper<Boolean>
}
