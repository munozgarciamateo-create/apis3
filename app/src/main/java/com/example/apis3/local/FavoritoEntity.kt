package com.example.apis3.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favoritos")
data class FavoritoEntity(
    @PrimaryKey val clave: String,      // ej: "POKEMON:pikachu"
    val titulo: String,
    val subtitulo: String,
    val imagen: String?,
    val datosJson: String,
    val agregadoEn: Long = System.currentTimeMillis()
)