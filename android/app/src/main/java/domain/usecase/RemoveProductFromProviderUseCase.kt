package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.domain.repository.ProviderRepository

class RemoveProductFromProviderUseCase(
    private val providerRepository: ProviderRepository
) {

    suspend operator fun invoke(
        businessId: String,
        providerId: String,
        productId: String
    ): Result<Unit> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (providerId.isBlank()) return Result.Error("El ID del proveedor es requerido")
        if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
        return providerRepository.removeProductFromProvider(businessId, providerId, productId)
    }
}
