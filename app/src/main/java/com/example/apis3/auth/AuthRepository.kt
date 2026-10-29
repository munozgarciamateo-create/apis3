package com.example.apis3.auth

import com.example.apis3.comun.Resultado
import com.example.apis3.entidad.Usuario
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** La única clase de la app que conoce a FirebaseAuth. */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /** Usuario con la sesión abierta ahora mismo, o null si no hay sesión. Es síncrono. */
    fun usuarioActual(): Usuario? = auth.currentUser?.aUsuario()

    /** Emite el usuario cada vez que se inicia o se cierra la sesión (null = sin sesión). */
    fun observarSesion(): Flow<Usuario?> = callbackFlow {
        val oyente = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.aUsuario())
        }
        auth.addAuthStateListener(oyente)
        awaitClose { auth.removeAuthStateListener(oyente) }
    }

    suspend fun iniciarSesion(email: String, contrasena: String): Resultado<Unit> =
        llamar { auth.signInWithEmailAndPassword(email, contrasena).await() }

    suspend fun registrar(email: String, contrasena: String): Resultado<Unit> =
        llamar { auth.createUserWithEmailAndPassword(email, contrasena).await() }

    suspend fun enviarCorreoDeRecuperacion(email: String): Resultado<Unit> =
        llamar { auth.sendPasswordResetEmail(email).await() }

    fun cerrarSesion() = auth.signOut()

    /**
     * Borra la cuenta del usuario en Firebase (Google Play lo exige a las apps que permiten crear cuenta).
     * Si sale bien, el AuthStateListener emite null y la app vuelve sola al login.
     */
    suspend fun eliminarCuenta(): Resultado<Unit> = llamar {
        val usuario = auth.currentUser ?: throw IllegalStateException("No hay sesión abierta")
        usuario.delete().await()
    }

    /** Ejecuta la llamada y traduce cualquier excepción de Firebase a un [Resultado.Error]. */
    private suspend fun llamar(bloque: suspend () -> Unit): Resultado<Unit> =
        try {
            bloque()
            Resultado.Exito(Unit)
        } catch (e: CancellationException) {
            throw e // la cancelación no es un error: debe propagarse
        } catch (e: Exception) {
            Resultado.Error(traducir(e))
        }

    private fun traducir(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil. Usa al menos 6 caracteres."
        // Firebase solo deja borrar la cuenta si la sesión es reciente (va antes que InvalidUser porque hereda de ella)
        is FirebaseAuthRecentLoginRequiredException -> "Por seguridad, cierra sesión, vuelve a entrar e inténtalo de nuevo."
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo."
        is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo."
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
        is FirebaseNetworkException -> "No hay conexión a internet. Revisa tu red e inténtalo de nuevo."
        else -> "No se pudo completar la operación. Inténtalo de nuevo."
    }
}

private fun FirebaseUser.aUsuario() = Usuario(uid = uid, email = email.orEmpty())
