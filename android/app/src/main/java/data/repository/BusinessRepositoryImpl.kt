package com.caas.app.data.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.data.model.BusinessMember
import com.caas.app.data.source.FirestoreBusinessDataSource
import com.caas.app.domain.repository.BusinessRepository

class BusinessRepositoryImpl(
    private val dataSource: FirestoreBusinessDataSource
) : BusinessRepository {

    override suspend fun createBusinessWithOwner(
        business: Business,
        ownerMember: BusinessMember
    ): Result<Business> {
        return try {
            dataSource.createBusiness(business)
            dataSource.createOwnerMember(business.id, ownerMember)
            Result.Success(business)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Error al crear el negocio",
                throwable = e
            )
        }
    }
}