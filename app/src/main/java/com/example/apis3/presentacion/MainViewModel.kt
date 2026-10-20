package com.example.apis3.presentacion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apis3.busqueda.BusquedaRepository
import com.example.apis3.comun.ApiException
import com.example.apis3.entidad.Tarjeta
import com.example.apis3.local.AppDatabase
import com.example.apis3.local.FavoritosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
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
    val tarjeta: Tarjeta? = null,
    val esFavorito: Boolean = false
)

// Lo único que el ViewModel maneja por su cuenta: el estado de la conexión con la API.
private data class EstadoRed(
    val cargando: Boolean = false,
    val error: String? = null
)

// La búsqueda confirmada (al tocar "Buscar"): decide qué fila de Room se observa.
private data class Busqueda(val fuente: Fuente, val texto: String)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val baseDeDatos = AppDatabase.obtener(application)
    private val favoritosRepository = FavoritosRepository(baseDeDatos.favoritoDao())
    private val busquedaRepository = BusquedaRepository(baseDeDatos.cacheDao())

    private val _fuente = MutableStateFlow(Fuente.POKEMON)
    private val _texto = MutableStateFlow(Fuente.POKEMON.ejemplo)
    private val _busqueda = MutableStateFlow(Busqueda(Fuente.POKEMON, Fuente.POKEMON.ejemplo))
    private val _red = MutableStateFlow(EstadoRed())

    // La tarjeta SIEMPRE sale de Room (fuente única de verdad).
    // Si cambia la búsqueda, se deja de observar la fila anterior y se observa la nueva.
    private val tarjetaGuardada: Flow<Tarjeta?> =
        _busqueda.flatMapLatest { b -> busquedaRepository.observar(b.fuente, b.texto) }

    // Lo que ve la pantalla = lo escrito + la tarjeta de Room + estado de red + favoritos de Room
    val estado: StateFlow<UiState> = combine(
        _fuente,
        _texto,
        tarjetaGuardada,
        _red,
        favoritosRepository.observarClaves()
    ) { fuente, texto, tarjeta, red, claves ->
        UiState(
            fuente = fuente,
            texto = texto,
            cargando = red.cargando,
            error = red.error,
            tarjeta = tarjeta,
            esFavorito = tarjeta?.let { clave(fuente, it) in claves } ?: false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState(cargando = true)
    )

    private var consulta: Job? = null

    init { buscar() }

    fun elegirFuente(fuente: Fuente) {
        _fuente.value = fuente
        _texto.value = fuente.ejemplo
        buscar()
    }

    fun cambiarTexto(valor: String) {
        _texto.value = valor
    }

    fun buscar() {
        consulta?.cancel()
        val b = Busqueda(_fuente.value, _texto.value)
        _busqueda.value = b   // la pantalla empieza a observar esa fila de Room
        consulta = viewModelScope.launch {
            // 1. Ya está guardada: se muestra al instante y NO se llama a la API.
            if (busquedaRepository.hayGuardado(b.fuente, b.texto)) {
                _red.value = EstadoRed()
                return@launch
            }
            // 2. No está guardada: la API solo sirve para llenar Room.
            _red.value = EstadoRed(cargando = true)
            try {
                busquedaRepository.actualizar(b.fuente, b.texto)
                _red.value = EstadoRed()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _red.value = EstadoRed(error = e.message)
            } catch (e: Exception) {
                _red.value = EstadoRed(error = "No se pudo mostrar la información. Intenta nuevamente.")
            }
        }
    }

    // Si ya era favorito lo quita; si no, lo guarda. Room avisa y el corazón cambia solo.
    fun alternarFavorito() {
        val e = estado.value
        val tarjeta = e.tarjeta ?: return
        val clave = clave(e.fuente, tarjeta)
        viewModelScope.launch {
            if (e.esFavorito) favoritosRepository.quitar(clave)
            else favoritosRepository.agregar(clave, tarjeta)
        }
    }
}

// Ej: "POKEMON:pikachu". Identifica a cada tarjeta entre las 3 APIs.
private fun clave(fuente: Fuente, tarjeta: Tarjeta): String =
    "${fuente.name}:${tarjeta.titulo.trim().lowercase()}"