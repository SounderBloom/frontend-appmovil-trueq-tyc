@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trueq.app.data.model.ProductoDTO
import com.trueq.app.data.model.TipoTransaccion
import com.trueq.app.data.remote.resolverUrlArchivo
import com.trueq.app.util.etiquetaTipoTransaccion
import com.trueq.app.util.formatearFechaRelativa
import com.trueq.app.util.formatearPrecio
import java.util.Locale

@Composable
fun ProductCard(
    producto: ProductoDTO,
    distanciaKm: Double? = null,
    onClick: () -> Unit
) {
    val esTrueque = producto.tipoTransaccion == TipoTransaccion.Trueque ||
        producto.tipoTransaccion == TipoTransaccion.TruequeOVenta ||
        producto.tipoTransaccion == TipoTransaccion.Donar
    val imagen = resolverUrlArchivo(producto.fotos.firstOrNull())

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (imagen != null) {
                AsyncImage(
                    model = imagen,
                    contentDescription = producto.titulo,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (distanciaKm != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            String.format(Locale("es", "MX"), "%.1f km", distanciaKm),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                producto.titulo,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (esTrueque) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            etiquetaTipoTransaccion(producto.tipoTransaccion).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        formatearPrecio(producto.precio),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    formatearFechaRelativa(producto.fechaPublicacion),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
