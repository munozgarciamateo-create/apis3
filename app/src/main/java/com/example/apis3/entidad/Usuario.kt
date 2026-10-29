package com.example.apis3.entidad

/** Lo único que la app necesita saber de la persona que inició sesión. La Vista nunca ve un FirebaseUser. */
data class Usuario(
    val uid: String,
    val email: String
)
