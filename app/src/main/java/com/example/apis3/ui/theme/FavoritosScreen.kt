package com.example.apis3.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apis3.presentacion.FavoritosViewModel

@Composable
fun FavoritosScreen(
    onVolver: () -> Unit,
    viewModel: FavoritosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("Favoritos", style = MaterialTheme.typography.headlineSmall)
        }

        when {
            // Estado 1: cargando
            uiState.cargando -> CircularProgressIndicator()

            // Estado 2: vacío
            uiState.favoritos.isEmpty() ->
                Text("Aún no tienes favoritos. Toca el corazón de una tarjeta para guardarla.")

            // Estado 3: con datos
            else -> uiState.favoritos.forEach { f ->
                TarjetaVista(
                    t = f.tarjeta,
                    esFavorito = true,
                    onFavoritoClick = { viewModel.quitar(f.clave) }
                )
            }
        }
    }
}