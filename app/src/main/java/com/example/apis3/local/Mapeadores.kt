package com.example.apis3.local

import com.example.apis3.entidad.Favorito
import com.example.apis3.entidad.Tarjeta
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()
private val tipoDatos = object : TypeToken<List<Pair<String, String>>>() {}.type

private fun datosAJson(datos: List<Pair<String, String>>): String = gson.toJson(datos)
private fun jsonADatos(json: String): List<Pair<String, String>> = gson.fromJson(json, tipoDatos)

// ---------- Favoritos ----------

fun Tarjeta.aFavoritoEntity(clave: String): FavoritoEntity = FavoritoEntity(
    clave = clave,
    titulo = titulo,
    subtitulo = subtitulo,
    imagen = imagen,
    datosJson = datosAJson(datos)
)

fun FavoritoEntity.aFavorito(): Favorito = Favorito(
    clave = clave,
    tarjeta = Tarjeta(titulo, subtitulo, imagen, jsonADatos(datosJson))
)

// ---------- Caché de búsquedas ----------

fun Tarjeta.aCacheEntity(clave: String): CacheEntity = CacheEntity(
    clave = clave,
    titulo = titulo,
    subtitulo = subtitulo,
    imagen = imagen,
    datosJson = datosAJson(datos)
)

fun CacheEntity.aTarjeta(): Tarjeta =
    Tarjeta(titulo, subtitulo, imagen, jsonADatos(datosJson))