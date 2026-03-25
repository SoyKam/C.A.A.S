package com.caas.app.data.model

import com.google.firebase.firestore.PropertyName

data class Branch(
    val id: String = "",
    val businessId: String = "",
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    @get:PropertyName("isActive") val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
