package com.caas.app.data.model

data class StockMovement(
    val id: String = "",
    val businessId: String = "",
    val branchId: String = "",
    val productId: String = "",
    val productName: String = "",
    val type: MovementType = MovementType.ENTRY,
    val quantity: Int = 0,
    val reason: String = "",
    val createdAt: Long = 0L,
    val createdBy: String = ""
)
