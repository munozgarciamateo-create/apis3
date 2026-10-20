package com.example.apis3.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_busquedas")
data class CacheEntity(
    @PrimaryKey val clave: String,      // ej: "POKEMON:pikachu" (lo que se buscó)
    val titulo: String,
    val subtitulo: String,
    val imagen: String?,
    val datosJson: String
)