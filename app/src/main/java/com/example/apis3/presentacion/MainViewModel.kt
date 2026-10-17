package com.example.apis3.presentacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apis3.comun.ApiException
import com.example.apis3.entidad.Tarjeta
import com.example.apis3.perros.PerroRepository
import com.example.apis3.pokemon.PokemonRepository
import com.example.apis3.rickmorty.RickMortyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Fuente(val titulo: String, val ejemplo: String) {
    POKEMON("Pokémon", "pikachu"),
    RICKMORTY("Rick & Morty", "rick"),
    PERROS("Perros", "husky")
}

data class UiState(
    val fuente: Fuente = Fuente.POKEMON,
    val texto: String = Fuente.POKEMON.ejemplo,
    val cargando: Boolean = false,
    val error: String? = null,
    val tarjeta: Tarjeta? = null
)

class MainViewModel : ViewModel() {
    private val pokemonRepository = PokemonRepository()
    private val rickMortyRepository = RickMortyRepository()
    private val perroRepository = PerroRepository()

    private val _estado = MutableStateFlow(UiState())
    val estado = _estado.asStateFlow()
    private var consulta: Job? = null

    init { buscar() }

    fun elegirFuente(fuente: Fuente) {
        consulta?.cancel()
        _estado.update { it.copy(fuente = fuente, texto = fuente.ejemplo, tarjeta = null, error = null) }
        buscar()
    }

    fun cambiarTexto(valor: String) {
        _estado.update { it.copy(texto = valor) }
    }

    fun buscar() {
        consulta?.cancel()
        val actual = _estado.value
        _estado.update { it.copy(cargando = true, error = null, tarjeta = null) }
        consulta = viewModelScope.launch {
            try {
                val tarjeta = when (actual.fuente) {
                    Fuente.POKEMON -> pokemonRepository.buscar(actual.texto)
                    Fuente.RICKMORTY -> rickMortyRepository.buscar(actual.texto)
                    Fuente.PERROS -> perroRepository.buscar(actual.texto)
                }
                _estado.update { it.copy(tarjeta = tarjeta, cargando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _estado.update { it.copy(error = e.message, cargando = false) }
            } catch (e: Exception) {
                _estado.update { it.copy(error = "No se pudo mostrar la información. Intenta nuevamente.", cargando = false) }
            }
        }
    }
}