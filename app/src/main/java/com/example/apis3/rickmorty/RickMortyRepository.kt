package com.example.apis3.rickmorty

import com.example.apis3.comun.ApiException
import com.example.apis3.entidad.Tarjeta
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class RickMortyRepository(private val api: RickMortyService = RickMortyApi.service) {
    suspend fun buscar(texto: String): Tarjeta {
        val consulta = texto.trim()
        if (consulta.isEmpty()) throw ApiException("Escribe el nombre de un personaje.")
        try {
            val personaje = api.obtenerPersonaje(consulta).resultados.firstOrNull()
                ?: throw ApiException("No encontramos ese personaje.")
            return personaje.aTarjeta()
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            throw e
        } catch (e: HttpException) {
            throw ApiException(when (e.code()) {
                404 -> "No encontramos ese personaje. Prueba con: rick, morty, summer."
                else -> "El servicio no pudo responder (HTTP ${e.code()}). Intenta nuevamente."
            })
        } catch (e: SocketTimeoutException) {
            throw ApiException("La consulta tardó demasiado. Intenta nuevamente.")
        } catch (e: IOException) {
            throw ApiException("No se pudo conectar. Comprueba tu conexión a Internet.")
        } catch (e: JsonParseException) {
            throw ApiException("El servicio devolvió datos que no se pudieron interpretar.")
        }
    }
}