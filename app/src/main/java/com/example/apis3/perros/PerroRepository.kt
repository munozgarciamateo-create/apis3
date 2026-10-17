package com.example.apis3.perros

import com.example.apis3.comun.ApiException
import com.example.apis3.entidad.Tarjeta
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class PerroRepository(private val api: PerroService = PerroApi.service) {
    suspend fun buscar(texto: String): Tarjeta {
        val consulta = texto.trim().lowercase()
        if (consulta.isEmpty()) throw ApiException("Escribe una raza de perro.")
        try {
            return api.obtenerPerro(consulta).aTarjeta(consulta)
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            throw ApiException(when (e.code()) {
                404 -> "No encontramos esa raza. Prueba con: husky, labrador, poodle, hound."
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