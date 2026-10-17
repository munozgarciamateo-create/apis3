package com.example.apis3.rickmorty

import com.google.gson.annotations.SerializedName

data class RickMortyOrigenDto(
    @SerializedName("name") val nombre: String
)

data class RickMortyPersonajeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val nombre: String,
    @SerializedName("status") val estado: String,
    @SerializedName("species") val especie: String,
    @SerializedName("origin") val origen: RickMortyOrigenDto,
    @SerializedName("image") val imagen: String?,
    @SerializedName("episode") val episodios: List<String>
)

data class RickMortyRespuestaDto(
    @SerializedName("results") val resultados: List<RickMortyPersonajeDto>
)