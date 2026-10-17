package com.example.apis3.entidad

data class Tarjeta(
    val titulo: String,
    val subtitulo: String,
    val imagen: String?,
    val datos: List<Pair<String, String>>
)