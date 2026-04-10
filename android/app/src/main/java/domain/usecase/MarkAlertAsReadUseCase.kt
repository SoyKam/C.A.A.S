package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.domain.repository.StockRepository

class MarkAlertAsReadUseCase(
    private val stockRepository: StockRepository
) {
    suspend operator fun invoke(businessId: String, alertId: String): Result<Unit> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (alertId.isBlank()) return Result.Error("El ID de la alerta es requerido")
        return stockRepository.markAlertAsRead(businessId, alertId)
    }
}
