@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.trueq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trueq.app.data.remote.resolverUrlArchivo

@Composable
fun PhotoCarousel(fotos: List<String>, alturaDp: Int = 300) {
    val urls = fotos.mapNotNull { resolverUrlArchivo(it) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(alturaDp.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (urls.isEmpty()) {
            Icon(
                Icons.Filled.Image,
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center).size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Box
        }

        val pagerState = rememberPagerState(pageCount = { urls.size })

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pagina ->
            AsyncImage(
                model = urls[pagina],
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (urls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(urls.size) { indice ->
                    val activo = indice == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (activo) 20.dp else 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (activo) Color.White else Color.White.copy(alpha = 0.5f))
                    )
                }
            }

            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Text(
                    "${pagerState.currentPage + 1}/${urls.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
