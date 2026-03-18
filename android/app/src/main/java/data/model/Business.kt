package com.caas.app.data.model

data class Business(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val sector: String = "",
    val taxId: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)