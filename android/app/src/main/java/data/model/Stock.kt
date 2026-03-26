package com.caas.app.data.model

import com.google.firebase.firestore.PropertyName

data class Stock(
    val id: String = "",
    val businessId: String = "",
    val branchId: String = "",
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val minStock: Int = 0,
    @get:PropertyName("isActive") val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
