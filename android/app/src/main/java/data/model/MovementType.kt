package com.caas.app.data.model

enum class MovementType {
    ENTRY,    // RF-11: entrada de stock
    SALE,     // RF-12: salida por venta
    DAMAGE,   // RF-12: salida por daño
    TRANSFER  // RF-12: salida por traslado
}
