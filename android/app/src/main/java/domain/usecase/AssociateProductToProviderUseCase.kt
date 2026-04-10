package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.domain.repository.ProviderRepository

class AssociateProductToProviderUseCase(
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

        val providerResult = providerRepository.getProviderById(businessId, providerId)
        if (providerResult is Result.Error) return providerResult
        val provider = (providerResult as? Result.Success)?.data
            ?: return Result.Error("Proveedor no encontrado")

        if (provider.productIds.contains(productId)) {
            return Result.Error("El producto ya está asociado a este proveedor")
        }

        return providerRepository.addProductToProvider(businessId, providerId, productId)
    }
}
