package com.caas.app.domain.model

/**
 * Modelo de dominio para el resumen de inventario por sucursal (RF-21).
 * Combina datos de Stock y Product para la vista de resumen.
 */
data class InventorySummaryItem(
    val stockId: String,
    val productId: String,
    val productName: String,
    val sku: String,
    val quantity: Int,
    val minStock: Int,
    val isCritical: Boolean
)
