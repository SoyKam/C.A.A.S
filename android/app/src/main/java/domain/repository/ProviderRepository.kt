package com.caas.app.domain.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Provider

interface ProviderRepository {

    suspend fun createProvider(provider: Provider): Result<Provider>

    suspend fun updateProvider(provider: Provider): Result<Provider>

    suspend fun deleteProvider(businessId: String, providerId: String): Result<Unit>

    suspend fun getProvidersByBusiness(businessId: String): Result<List<Provider>>

    suspend fun getProviderById(businessId: String, providerId: String): Result<Provider?>

    suspend fun getProvidersByProduct(businessId: String, productId: String): Result<List<Provider>>

    suspend fun addProductToProvider(businessId: String, providerId: String, productId: String): Result<Unit>

    suspend fun removeProductFromProvider(businessId: String, providerId: String, productId: String): Result<Unit>
}
