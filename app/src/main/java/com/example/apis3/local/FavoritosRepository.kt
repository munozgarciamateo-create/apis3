package com.example.apis3.local

import android.util.Log
import com.example.apis3.entidad.Favorito
import com.example.apis3.entidad.Tarjeta
import com.example.apis3.nube.FavoritosNube
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room (SQLite en el teléfono) sigue siendo la fuente única de verdad: la pantalla solo lee de Room.
 * SQLite Cloud es una copia en la nube: si no hay internet, la app funciona igual
 * y la próxima vez que se llame a sincronizar() se ponen al día.
 */
class FavoritosRepository(
    private val dao: FavoritoDao,
    private val nube: FavoritosNube = FavoritosNube()
) {

    fun observarFavoritos(): Flow<List<Favorito>> =
        dao.observarFavoritos().map { lista -> lista.map { it.aFavorito() } }

    fun observarClaves(): Flow<Set<String>> =
        dao.observarClaves().map { it.toSet() }

    suspend fun agregar(clave: String, tarjeta: Tarjeta) {
        val entidad = tarjeta.aFavoritoEntity(clave)
        dao.insertar(entidad)                  // 1. primero local (instantáneo)
        enLaNube { nube.guardar(entidad) }     // 2. luego la nube (si se puede)
    }

    suspend fun quitar(clave: String) {
        dao.eliminarPorClave(clave)
        enLaNube { nube.eliminar(clave) }
    }

    /** Baja a Room lo que esté en la nube y sube lo que solo esté en el teléfono. */
    suspend fun sincronizar() = enLaNube {
        val remotos = nube.todos()
        val clavesRemotas = remotos.map { it.clave }.toSet()
        remotos.forEach { dao.insertar(it) }
        dao.obtenerTodos()
            .filter { it.clave !in clavesRemotas }
            .forEach { nube.guardar(it) }
    }

    private suspend fun enLaNube(accion: suspend () -> Unit) {
        if (!nube.activa) {
            Log.w("SQLiteCloud", "Nube desactivada: falta sqlitecloud.functionsUrl o sqlitecloud.apiKey en local.properties")
            return
        }
        try {
            accion()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("SQLiteCloud", "No se pudo sincronizar con la Edge Function", e)
        }
    }
}
