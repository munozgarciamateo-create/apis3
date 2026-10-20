package com.example.apis3.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheDao {
    // Lectura que queda escuchando: Room vuelve a emitir cada vez que esa fila cambia
    @Query("SELECT * FROM cache_busquedas WHERE clave = :clave")
    fun observar(clave: String): Flow<CacheEntity?>

    // Lectura de una sola vez (para saber si ya hay algo guardado)
    @Query("SELECT * FROM cache_busquedas WHERE clave = :clave")
    suspend fun obtener(clave: String): CacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entidad: CacheEntity)
}