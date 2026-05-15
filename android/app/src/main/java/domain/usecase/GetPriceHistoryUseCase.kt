package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.PriceHistory
import com.caas.app.domain.repository.PriceHistoryRepository

class GetPriceHistoryUseCase(
    private val priceHistoryRepository: PriceHistoryRepository
) {

    suspend operator fun invoke(businessId: String, productId: String): Result<List<PriceHistory>> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
        return priceHistoryRepository.getPriceHistory(businessId, productId)
    }
}
