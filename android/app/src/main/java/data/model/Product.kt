package com.caas.app.data.model

import com.google.firebase.firestore.PropertyName

data class Product(
    val id: String = "",
    val businessId: String = "",
    val name: String = "",
    val sku: String = "",
    val category: String = "",
    val costPrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val imageUrl: String = "",
    @get:PropertyName("isActive") val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

