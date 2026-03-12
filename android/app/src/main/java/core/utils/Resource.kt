package com.caas.app.core.utils

/**
Sealed class que representa los diferentes estados de una operación asíncrona
 usado en toda la app para comunicar el resultado de operaciones de red/BD.
 */
sealed class Resource<out T> {
    /**
     Estado de carga en progreso.
     */
    data object Loading : Resource<Nothing>()

    /**
     Estado de operación exitosa con datos.
     */
    data class Success<out T>(val data: T) : Resource<T>()

    /**
     Estado de error con mensaje descriptivo.
     */
    data class Error(val message: String) : Resource<Nothing>()
}