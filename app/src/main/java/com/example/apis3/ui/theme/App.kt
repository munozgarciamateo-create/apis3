package com.example.apis3.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.apis3.presentacion.AuthViewModel
import com.example.apis3.presentacion.Fuente
import com.example.apis3.presentacion.MainViewModel

private const val RUTA_LOGIN = "login"
private const val RUTA_BUSCADOR = "buscador"
private const val RUTA_FAVORITOS = "favoritos"

/**
 * El AuthViewModel se crea una sola vez (a nivel de la Activity) y se pasa a las pantallas.
 * Si cada destino usara viewModel() por su cuenta, el login actualizaría una sesión que App no mira.
 */
@Composable
fun App(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val usuario by authViewModel.usuario.collectAsState()
    val haySesion = usuario != null

    NavHost(
        navController = navController,
        startDestination = if (haySesion) RUTA_BUSCADOR else RUTA_LOGIN
    ) {
        composable(RUTA_LOGIN) {
            LoginScreen(viewModel = authViewModel)
        }
        composable(RUTA_BUSCADOR) {
            BuscadorScreen(
                onVerFavoritos = { navController.navigate(RUTA_FAVORITOS) },
                correoUsuario = usuario?.email.orEmpty(),
                onCerrarSesion = authViewModel::cerrarSesion
            )
        }
        composable(RUTA_FAVORITOS) {
            FavoritosScreen(
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Reacción a los cambios de sesión: al entrar va al buscador; al salir, al login.
    // `popUpTo(0)` vacía la pila para que "Atrás" no devuelva a una pantalla de otra sesión.
    LaunchedEffect(haySesion) {
        val destinoActual = navController.currentDestination?.route
        if (haySesion && destinoActual == RUTA_LOGIN) {
            navController.navigate(RUTA_BUSCADOR) { popUpTo(0) }
        } else if (!haySesion && destinoActual != RUTA_LOGIN) {
            navController.navigate(RUTA_LOGIN) { popUpTo(0) }
        }
    }
}

@Composable
private fun BuscadorScreen(
    onVerFavoritos: () -> Unit,
    correoUsuario: String,
    onCerrarSesion: () -> Unit,
    vm: MainViewModel = viewModel()
) {
    val e by vm.estado.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("3 APIs, una pantalla", style = MaterialTheme.typography.headlineSmall)
                if (correoUsuario.isNotEmpty()) {
                    Text(
                        correoUsuario,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onVerFavoritos) {
                Icon(Icons.Default.Favorite, contentDescription = "Ver favoritos", tint = Color(0xFFE53935))
            }
            IconButton(onClick = onCerrarSesion) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Fuente.entries.forEach { f ->
                FilterChip(
                    selected = e.fuente == f,
                    onClick = { vm.elegirFuente(f) },
                    label = { Text(f.titulo) }
                )
            }
        }

        OutlinedTextField(
            value = e.texto,
            onValueChange = vm::cambiarTexto,
            label = { Text("Buscar (ej: ${e.fuente.ejemplo})") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { vm.buscar() }),
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = { vm.buscar() }, modifier = Modifier.fillMaxWidth()) {
            Text("Buscar")
        }

        if (e.cargando) CircularProgressIndicator()

        e.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = { vm.buscar() }) { Text("Reintentar") }
        }

        e.tarjeta?.let {
            TarjetaVista(
                t = it,
                esFavorito = e.esFavorito,
                onFavoritoClick = vm::alternarFavorito
            )
        }
    }
}