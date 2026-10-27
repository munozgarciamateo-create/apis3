package com.example.apis3.presentacion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apis3.entidad.Favorito
import com.example.apis3.local.AppDatabase
import com.example.apis3.local.FavoritosRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritosUiState(
    val favoritos: List<Favorito> = emptyList(),
    val cargando: Boolean = true
)

class FavoritosViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritosRepository =
        FavoritosRepository(AppDatabase.obtener(application).favoritoDao())

    // Aquí no hay red: todo sale de Room. Por eso funciona sin internet.
    val uiState: StateFlow<FavoritosUiState> = favoritosRepository.observarFavoritos()
        .map { lista -> FavoritosUiState(favoritos = lista, cargando = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritosUiState()
        )

    init {
        // Al abrir Favoritos: trae lo que haya en SQLite Cloud y sube lo pendiente.
        viewModelScope.launch { favoritosRepository.sincronizar() }
    }

    fun quitar(clave: String) {
        viewModelScope.launch { favoritosRepository.quitar(clave) }
    }
}