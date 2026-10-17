package com.example.apis3.pokemon

import com.example.apis3.entidad.Tarjeta

fun PokemonDto.aTarjeta(): Tarjeta {
    fun stat(nombre: String) =
        stats.firstOrNull { it.stat.nombre == nombre }?.valor?.toString() ?: "-"

    return Tarjeta(
        titulo = nombre.replaceFirstChar { it.uppercase() },
        subtitulo = "Pokémon #$id",
        imagen = sprites.frente,
        datos = listOf(
            "Tipos" to tipos.sortedBy { it.posicion }.joinToString(", ") { it.tipo.nombre },
            "Altura" to "${altura / 10.0} m",
            "Peso" to "${peso / 10.0} kg",
            "HP" to stat("hp"),
            "Ataque" to stat("attack"),
            "Velocidad" to stat("speed")
        )
    )
}