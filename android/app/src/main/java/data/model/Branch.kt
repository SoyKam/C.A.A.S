package com.caas.app.data.model

data class Branch(
    val id: String = "",
    val businessId: String = "",
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
