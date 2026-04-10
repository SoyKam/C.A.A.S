package com.caas.app.data.model

import com.google.firebase.firestore.PropertyName

data class StockAlert(
    val id: String = "",
    val businessId: String = "",
    val branchId: String = "",
    val branchName: String = "",
    val productId: String = "",
    val productName: String = "",
    val currentStock: Int = 0,
    val minStock: Int = 0,
    @get:PropertyName("isRead") val isRead: Boolean = false,
    val createdAt: Long = 0L
)
