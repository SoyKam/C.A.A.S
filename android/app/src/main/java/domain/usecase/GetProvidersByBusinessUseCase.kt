package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Provider
import com.caas.app.domain.repository.ProviderRepository

class GetProvidersByBusinessUseCase(
    private val providerRepository: ProviderRepository
) {

    suspend operator fun invoke(businessId: String): Result<List<Provider>> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        return providerRepository.getProvidersByBusiness(businessId)
    }
}
