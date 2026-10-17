package com.example.apis3.perros

import com.example.apis3.entidad.Tarjeta

fun PerroDto.aTarjeta(raza: String): Tarjeta = Tarjeta(
    titulo = raza.replaceFirstChar { it.uppercase() },
    subtitulo = "Raza de perro",
    imagen = imagen,
    datos = listOf(
        "Estado" to estado
    )
)