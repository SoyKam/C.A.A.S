package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Provider
import com.caas.app.domain.repository.ProviderRepository

class GetProvidersByProductUseCase(
    private val providerRepository: ProviderRepository
) {

    suspend operator fun invoke(businessId: String, productId: String): Result<List<Provider>> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
        return providerRepository.getProvidersByProduct(businessId, productId)
    }
}
