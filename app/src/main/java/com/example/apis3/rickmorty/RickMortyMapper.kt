package com.example.apis3.rickmorty

import com.example.apis3.entidad.Tarjeta

fun RickMortyPersonajeDto.aTarjeta(): Tarjeta = Tarjeta(
    titulo = nombre,
    subtitulo = "Personaje #$id",
    imagen = imagen,
    datos = listOf(
        "Estado" to estado,
        "Especie" to especie,
        "Origen" to origen.nombre,
        "Episodios" to episodios.size.toString()
    )
)