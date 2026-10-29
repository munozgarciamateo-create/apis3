package com.example.apis3.comun

/** Respuesta de una operación: o salió bien (con un valor) o falló (con un mensaje listo para mostrar). */
sealed class Resultado<out T> {
    data class Exito<T>(val valor: T) : Resultado<T>()
    data class Error(val mensaje: String) : Resultado<Nothing>()
}
