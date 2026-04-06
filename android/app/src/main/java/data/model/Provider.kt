package com.caas.app.data.model

import com.google.firebase.firestore.PropertyName

data class Provider(
    val id: String = "",
    val businessId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val productIds: List<String> = emptyList(),
    @get:PropertyName("isActive") val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
