package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Provider
import com.caas.app.domain.repository.ProviderRepository

class GetProviderByIdUseCase(
    private val providerRepository: ProviderRepository
) {

    suspend operator fun invoke(businessId: String, providerId: String): Result<Provider?> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (providerId.isBlank()) return Result.Error("El ID del proveedor es requerido")
        return providerRepository.getProviderById(businessId, providerId)
    }
}
