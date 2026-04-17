package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.StockMovement
import com.caas.app.domain.repository.StockRepository

class GetMovementsByDateRangeUseCase(
    private val stockRepository: StockRepository
) {
    suspend operator fun invoke(
        businessId: String,
        branchId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<StockMovement>> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (branchId.isBlank()) return Result.Error("El ID de la sucursal es requerido")
        if (startDate > endDate) return Result.Error("El rango de fechas es inválido")
        return stockRepository.getMovementsByDateRange(businessId, branchId, startDate, endDate)
    }
}
