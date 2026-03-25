package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.StockMovement
import com.caas.app.domain.repository.StockRepository

class GetMovementsByBranchUseCase(
    private val stockRepository: StockRepository
) {

    suspend operator fun invoke(businessId: String, branchId: String): Result<List<StockMovement>> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (branchId.isBlank()) return Result.Error("El ID de la sucursal es requerido")
        return stockRepository.getMovementsByBranch(businessId, branchId)
    }
}
