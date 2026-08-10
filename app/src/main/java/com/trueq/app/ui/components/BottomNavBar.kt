@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.trueq.app.navigation.Routes
import com.trueq.app.ui.theme.OnPrimaryContainer
import com.trueq.app.ui.theme.PrimaryContainer

private data class ItemNav(val ruta: String, val etiqueta: String, val icono: ImageVector, val iconoActivo: ImageVector)

private val items = listOf(
    ItemNav(Routes.HOME, "Home", Icons.Outlined.Home, Icons.Filled.Home),
    ItemNav(Routes.BUSCAR, "Buscar", Icons.Outlined.Search, Icons.Filled.Search),
    ItemNav(Routes.NUEVO_PRODUCTO, "Publicar", Icons.Outlined.AddCircle, Icons.Filled.AddCircle),
    ItemNav(Routes.MENSAJES, "Chat", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble),
    ItemNav(Routes.PERFIL, "Perfil", Icons.Outlined.Person, Icons.Filled.Person),
)

@Composable
fun BottomNavBar(rutaActual: String?, onNavegar: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Deja espacio para la barra de navegación del sistema
                // (gestos o botones), que si no se pisa con nuestra barra
                // inferior al usar edge-to-edge.
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val activo = rutaActual == item.ruta
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .let {
                            if (activo) it.background(PrimaryContainer) else it
                        }
                        .clickable { onNavegar(item.ruta) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (activo) item.iconoActivo else item.icono,
                        contentDescription = item.etiqueta,
                        tint = if (activo) OnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (activo) OnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
