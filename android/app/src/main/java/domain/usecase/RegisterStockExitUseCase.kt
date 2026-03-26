package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.Stock
import com.caas.app.data.model.StockMovement
import com.caas.app.domain.repository.StockRepository

class RegisterStockExitUseCase(
    private val stockRepository: StockRepository
) {

    suspend operator fun invoke(
        businessId: String,
        branchId: String,
        productId: String,
        productName: String,
        quantity: Int,
        type: MovementType,
        reason: String,
        userId: String
    ): Result<Stock> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (branchId.isBlank()) return Result.Error("El ID de la sucursal es requerido")
        if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
        if (productName.isBlank()) return Result.Error("El nombre del producto es requerido")
        if (quantity <= 0) return Result.Error("La cantidad debe ser mayor a 0")
        if (type == MovementType.ENTRY) return Result.Error("Tipo de movimiento inválido para salida")

        val existingResult = stockRepository.getStockByProduct(businessId, branchId, productId)
        if (existingResult is Result.Error) return existingResult

        val existing = (existingResult as Result.Success).data
            ?: return Result.Error("No existe stock registrado para este producto")

        if (existing.quantity - quantity < 0) {
            return Result.Error("Stock insuficiente. Disponible: ${existing.quantity}")
        }

        val now = System.currentTimeMillis()

        val updatedStock = existing.copy(
            quantity = existing.quantity - quantity,
            updatedAt = now
        )

        val updateResult = stockRepository.updateStock(updatedStock)
        if (updateResult is Result.Error) return updateResult

        val movement = StockMovement(
            id = "${branchId}_${System.nanoTime()}",
            businessId = businessId,
            branchId = branchId,
            productId = productId,
            productName = productName,
            type = type,
            quantity = quantity,
            reason = reason.ifBlank { type.name },
            createdAt = now,
            createdBy = userId
        )

        stockRepository.registerMovement(movement)

        return Result.Success(updatedStock)
    }
}
