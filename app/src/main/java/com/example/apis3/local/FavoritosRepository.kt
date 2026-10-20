package com.example.apis3.local

import com.example.apis3.entidad.Favorito
import com.example.apis3.entidad.Tarjeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritosRepository(private val dao: FavoritoDao) {

    fun observarFavoritos(): Flow<List<Favorito>> =
        dao.observarFavoritos().map { lista -> lista.map { it.aFavorito() } }

    fun observarClaves(): Flow<Set<String>> =
        dao.observarClaves().map { it.toSet() }

    suspend fun agregar(clave: String, tarjeta: Tarjeta) =
        dao.insertar(tarjeta.aFavoritoEntity(clave))

    suspend fun quitar(clave: String) = dao.eliminarPorClave(clave)
}