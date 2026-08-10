package com.trueq.app.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val BUSCAR = "buscar"
    const val MENSAJES = "mensajes"
    const val CHAT_PATTERN = "chat/{chatId}?proponer={proponer}"
    const val NOTIFICACIONES = "notificaciones"
    const val PERFIL = "perfil"
    const val NUEVO_PRODUCTO = "productos/nuevo"
    const val DETALLE_PRODUCTO_PATTERN = "productos/{id}"

    fun chat(chatId: String, proponer: Boolean = false) = "chat/$chatId?proponer=${if (proponer) 1 else 0}"
    fun detalleProducto(id: String) = "productos/$id"
}
