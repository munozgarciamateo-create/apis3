package com.example.apis3.busqueda

import com.example.apis3.entidad.Tarjeta
import com.example.apis3.local.CacheDao
import com.example.apis3.local.aCacheEntity
import com.example.apis3.local.aTarjeta
import com.example.apis3.perros.PerroRepository
import com.example.apis3.pokemon.PokemonRepository
import com.example.apis3.presentacion.Fuente
import com.example.apis3.rickmorty.RickMortyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BusquedaRepository(
    private val cacheDao: CacheDao,
    private val pokemon: PokemonRepository = PokemonRepository(),
    private val rickMorty: RickMortyRepository = RickMortyRepository(),
    private val perro: PerroRepository = PerroRepository()
) {
    // Lo que ve el usuario: lo que haya guardado en Room, con o sin internet.
    fun observar(fuente: Fuente, texto: String): Flow<Tarjeta?> =
        cacheDao.observar(clave(fuente, texto)).map { it?.aTarjeta() }

    suspend fun hayGuardado(fuente: Fuente, texto: String): Boolean =
        cacheDao.obtener(clave(fuente, texto)) != null

    // Pide a la API y GUARDA en Room. No devuelve la tarjeta: Room la emite sola.
    // Si falla, lanza ApiException (el ViewModel la muestra como error).
    suspend fun actualizar(fuente: Fuente, texto: String) {
        val tarjeta = when (fuente) {
            Fuente.POKEMON -> pokemon.buscar(texto)
            Fuente.RICKMORTY -> rickMorty.buscar(texto)
            Fuente.PERROS -> perro.buscar(texto)
        }
        cacheDao.insertar(tarjeta.aCacheEntity(clave(fuente, texto)))
    }

    // Ej: "POKEMON:pikachu"
    private fun clave(fuente: Fuente, texto: String) = "${fuente.name}:${texto.trim().lowercase()}"
}