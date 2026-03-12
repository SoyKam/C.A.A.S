package com.caas.app.domain.model

/**
 * Entidad de dominio que representa un usuario.
 * Independiente de Firebase y frameworks externos.
 */
data class User(
    val uid: String,
    val email: String,
    val name: String
)