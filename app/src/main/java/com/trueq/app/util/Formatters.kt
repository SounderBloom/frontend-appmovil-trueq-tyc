package com.trueq.app.util

import com.trueq.app.data.model.DireccionMonto
import com.trueq.app.data.model.TipoOferta
import com.trueq.app.data.model.TipoTransaccion
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun etiquetaTipoTransaccion(tipo: Int): String = when (tipo) {
    TipoTransaccion.Trueque -> "Trueque"
    TipoTransaccion.Venta -> "Venta"
    TipoTransaccion.TruequeOVenta -> "Trueque o Venta"
    TipoTransaccion.Donar -> "Donación"
    else -> "Trueque"
}

private val formatoPrecio: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

fun formatearPrecio(precio: Double): String = formatoPrecio.format(precio)

fun formatearFechaRelativa(fechaIso: String): String {
    return try {
        val fecha = Instant.parse(if (fechaIso.endsWith("Z")) fechaIso else "${fechaIso}Z")
        val ahora = Instant.now()
        val minutos = (ahora.epochSecond - fecha.epochSecond) / 60

        when {
            minutos < 1 -> "Ahora"
            minutos < 60 -> "Hace $minutos min"
            minutos < 60 * 24 -> "Hace ${minutos / 60} h"
            minutos < 60 * 24 * 2 -> "Ayer"
            minutos < 60 * 24 * 7 -> "Hace ${minutos / (60 * 24)} días"
            else -> DateTimeFormatter.ofPattern("d MMM", Locale("es", "MX"))
                .withZone(ZoneId.systemDefault())
                .format(fecha)
        }
    } catch (e: Exception) {
        fechaIso
    }
}

fun formatearHora(fechaIso: String): String {
    return try {
        val fecha = Instant.parse(if (fechaIso.endsWith("Z")) fechaIso else "${fechaIso}Z")
        DateTimeFormatter.ofPattern("h:mm a", Locale("es", "MX"))
            .withZone(ZoneId.systemDefault())
            .format(fecha)
    } catch (e: Exception) {
        ""
    }
}

fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

fun etiquetaTipoOferta(tipo: Int): String = when (tipo) {
    TipoOferta.Compra -> "Oferta de compra"
    TipoOferta.TruequeConDiferencia -> "Trueque + diferencia"
    TipoOferta.SolicitudDonacion -> "Solicitud de donación"
    else -> "Trueque"
}

fun etiquetaDireccionMonto(direccion: Int): String =
    if (direccion == DireccionMonto.ProponentePagaAlVendedor)
        "Yo pongo la diferencia en efectivo"
    else
        "Pido que el vendedor me pague la diferencia"
