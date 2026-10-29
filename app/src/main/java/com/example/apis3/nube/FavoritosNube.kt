package com.example.apis3.nube

import com.example.apis3.local.FavoritoEntity
import com.google.gson.JsonObject

/**
 * Favoritos en SQLite Cloud a través de 3 Edge Functions escritas en SQL
 * (el código está en la carpeta sqlitecloud/ del proyecto):
 *   favoritos-listar, favoritos-guardar, favoritos-eliminar
 */
class FavoritosNube(private val cliente: EdgeFunctionCliente = EdgeFunctionCliente) {

    val activa: Boolean get() = cliente.configurado

    suspend fun guardar(f: FavoritoEntity) {
        cliente.llamar(
            "favoritos-guardar",
            mapOf(
                "clave" to f.clave,
                "titulo" to f.titulo,
                "subtitulo" to f.subtitulo,
                "imagen" to (f.imagen ?: ""),   // "" se guarda como NULL (NULLIF en el SQL)
                "datosJson" to f.datosJson,
                "agregadoEn" to f.agregadoEn
            )
        )
    }

    suspend fun eliminar(clave: String) {
        cliente.llamar("favoritos-eliminar", mapOf("clave" to clave))
    }

    suspend fun todos(): List<FavoritoEntity> {
        val data = cliente.llamar("favoritos-listar")
        if (data == null || !data.isJsonArray) return emptyList()
        return data.asJsonArray.filter { it.isJsonObject }.map { it.asJsonObject.aEntity() }
    }
}

private fun JsonObject.texto(campo: String): String? =
    get(campo)?.takeUnless { it.isJsonNull }?.asString

private fun JsonObject.aEntity() = FavoritoEntity(
    clave = texto("clave").orEmpty(),
    titulo = texto("titulo").orEmpty(),
    subtitulo = texto("subtitulo").orEmpty(),
    imagen = texto("imagen")?.takeIf { it.isNotBlank() },
    datosJson = texto("datosJson") ?: "[]",
    agregadoEn = get("agregadoEn")?.takeUnless { it.isJsonNull }?.asLong ?: System.currentTimeMillis()
)
