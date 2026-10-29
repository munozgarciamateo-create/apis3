package com.example.apis3.presentacion

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apis3.auth.AuthRepository
import com.example.apis3.comun.Resultado
import com.example.apis3.entidad.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado inmutable del formulario de inicio de sesión y registro. */
data class AuthUiState(
    val email: String = "",
    val contrasena: String = "",
    val modoRegistro: Boolean = false,
    val mostrarContrasena: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val info: String? = null
)

class AuthViewModel(
    private val repositorio: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Usuario con sesión abierta (null si no hay). El valor inicial sale de `usuarioActual()`,
     * así la app sabe desde el primer fotograma si debe mostrar el login o el buscador.
     */
    val usuario: StateFlow<Usuario?> = repositorio.observarSesion()
        .stateIn(viewModelScope, SharingStarted.Eagerly, repositorio.usuarioActual())

    fun onEmailCambia(valor: String) = _uiState.update { it.copy(email = valor, error = null, info = null) }

    fun onContrasenaCambia(valor: String) = _uiState.update { it.copy(contrasena = valor, error = null, info = null) }

    fun alternarMostrarContrasena() = _uiState.update { it.copy(mostrarContrasena = !it.mostrarContrasena) }

    fun alternarModo() = _uiState.update { it.copy(modoRegistro = !it.modoRegistro, error = null, info = null) }

    fun enviar() {
        val estado = _uiState.value
        val email = estado.email.trim()
        validar(email, estado.contrasena)?.let { mensaje ->
            _uiState.update { it.copy(error = mensaje) }
            return
        }
        _uiState.update { it.copy(cargando = true, error = null, info = null) }
        viewModelScope.launch {
            val resultado = if (estado.modoRegistro) {
                repositorio.registrar(email, estado.contrasena)
            } else {
                repositorio.iniciarSesion(email, estado.contrasena)
            }
            // Si sale bien no hay nada más que hacer aquí: `usuario` cambia y la navegación reacciona.
            _uiState.update {
                when (resultado) {
                    is Resultado.Exito -> it.copy(cargando = false, contrasena = "")
                    is Resultado.Error -> it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun recuperarContrasena() {
        val email = _uiState.value.email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(error = "Escribe tu correo para enviarte el enlace de recuperación.") }
            return
        }
        _uiState.update { it.copy(cargando = true, error = null, info = null) }
        viewModelScope.launch {
            val resultado = repositorio.enviarCorreoDeRecuperacion(email)
            _uiState.update {
                when (resultado) {
                    is Resultado.Exito -> it.copy(cargando = false, info = "Te enviamos un correo para restablecer la contraseña.")
                    is Resultado.Error -> it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun cerrarSesion() {
        repositorio.cerrarSesion()
        _uiState.value = AuthUiState()
    }

    /** Elimina la cuenta. Si sale bien, `usuario` pasa a null y la navegación vuelve al login. */
    fun eliminarCuenta() {
        _uiState.update { it.copy(cargando = true, error = null, info = null) }
        viewModelScope.launch {
            when (val resultado = repositorio.eliminarCuenta()) {
                is Resultado.Exito -> _uiState.value = AuthUiState(info = "Tu cuenta fue eliminada.")
                is Resultado.Error -> _uiState.update { it.copy(cargando = false, error = resultado.mensaje) }
            }
        }
    }

    fun limpiarMensajes() = _uiState.update { it.copy(error = null, info = null) }

    /** Validación en el teléfono: ahorra una llamada a Firebase cuando el dato es obviamente malo. */
    private fun validar(email: String, contrasena: String): String? = when {
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Escribe un correo válido."
        contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
        else -> null
    }
}
