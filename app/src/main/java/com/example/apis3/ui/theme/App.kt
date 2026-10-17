package com.example.apis3.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apis3.entidad.Tarjeta
import com.example.apis3.presentacion.Fuente
import com.example.apis3.presentacion.MainViewModel

@Composable
fun App(vm: MainViewModel) {
    val e by vm.estado.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("3 APIs, una pantalla", style = MaterialTheme.typography.headlineSmall)

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
        e.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        e.tarjeta?.let { TarjetaVista(it) }
    }
}

@Composable
private fun TarjetaVista(t: Tarjeta) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
    }
}