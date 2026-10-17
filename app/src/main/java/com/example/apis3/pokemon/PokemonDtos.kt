package com.example.apis3.pokemon

import com.google.gson.annotations.SerializedName

data class PokemonNombreDto(
    @SerializedName("name") val nombre: String
)

data class PokemonTipoDto(
    @SerializedName("slot") val posicion: Int,
    @SerializedName("type") val tipo: PokemonNombreDto
)

data class PokemonStatDto(
    @SerializedName("base_stat") val valor: Int,
    @SerializedName("stat") val stat: PokemonNombreDto
)

data class PokemonSpritesDto(
    @SerializedName("front_default") val frente: String?
)

data class PokemonDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val nombre: String,
    @SerializedName("height") val altura: Int,
    @SerializedName("weight") val peso: Int,
    @SerializedName("sprites") val sprites: PokemonSpritesDto,
    @SerializedName("types") val tipos: List<PokemonTipoDto>,
    @SerializedName("stats") val stats: List<PokemonStatDto>
)