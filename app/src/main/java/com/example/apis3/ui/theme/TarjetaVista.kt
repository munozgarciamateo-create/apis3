package com.example.apis3.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apis3.entidad.Tarjeta

@Composable
fun TarjetaVista(
    t: Tarjeta,
    esFavorito: Boolean,
    onFavoritoClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = t.imagen,
                    contentDescription = t.titulo,
                    modifier = Modifier.size(180.dp)
                )
                Text(t.titulo, style = MaterialTheme.typography.titleLarge)
                Text(t.subtitulo, style = MaterialTheme.typography.bodyMedium)
                t.datos.forEach { (etiqueta, valor) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("$etiqueta:", fontWeight = FontWeight.Bold)
                        Text(valor)
                    }
                }
            }
            IconButton(
                onClick = onFavoritoClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (esFavorito) "Quitar de favoritos" else "Agregar a favoritos",
                    tint = if (esFavorito) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}