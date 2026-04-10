package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.StockAlert
import com.caas.app.domain.repository.StockRepository

class GetUnreadAlertsUseCase(
    private val stockRepository: StockRepository
) {
    suspend operator fun invoke(businessId: String): Result<List<StockAlert>> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        return stockRepository.getUnreadAlerts(businessId)
    }
}
