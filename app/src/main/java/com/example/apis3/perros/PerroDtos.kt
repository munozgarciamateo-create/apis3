package com.example.apis3.perros

import com.google.gson.annotations.SerializedName

data class PerroDto(
    @SerializedName("message") val imagen: String,
    @SerializedName("status") val estado: String
)