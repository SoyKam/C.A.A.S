package com.caas.app.data.model

data class BusinessMember(
    val userId: String = "",
    val businessId: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.EMPLOYEE,
    val branchId: String? = null,
    val isActive: Boolean = true,
    val invitedAt: Long = 0L,
    val joinedAt: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)