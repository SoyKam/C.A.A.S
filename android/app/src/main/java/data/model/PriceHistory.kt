package com.caas.app.data.model

data class PriceHistory(
    val id: String = "",
    val businessId: String = "",
    val productId: String = "",
    val productName: String = "",
    val previousCostPrice: Double = 0.0,
    val newCostPrice: Double = 0.0,
    val previousSalePrice: Double = 0.0,
    val newSalePrice: Double = 0.0,
    val changedAt: Long = 0L,
    val changedBy: String = "",
    val changedByEmail: String = ""
)
