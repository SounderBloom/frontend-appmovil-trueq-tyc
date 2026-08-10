package com.trueq.app.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun iconoCategoria(nombre: String): ImageVector = when (nombre.trim().lowercase()) {
    "ropa", "moda" -> Icons.Filled.Checkroom
    "electronica", "electrónica" -> Icons.Filled.Devices
    "hogar" -> Icons.Filled.Home
    "alimentos" -> Icons.Filled.Restaurant
    "accesorios" -> Icons.Filled.Watch
    "deportes" -> Icons.Filled.SportsSoccer
    "herramientas" -> Icons.Filled.Construction
    "mascotas" -> Icons.Filled.Pets
    "libros" -> Icons.Filled.MenuBook
    "juguetes" -> Icons.Filled.Toys
    else -> Icons.Filled.Category
}
