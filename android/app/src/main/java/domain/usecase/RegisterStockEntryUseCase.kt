package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.Stock
import com.caas.app.data.model.StockMovement
import com.caas.app.domain.repository.StockRepository
import java.util.UUID

class RegisterStockEntryUseCase(
    private val stockRepository: StockRepository
) {

    suspend operator fun invoke(
        businessId: String,
        branchId: String,
        productId: String,
        productName: String,
        quantity: Int,
        minStock: Int,
        userId: String
    ): Result<Stock> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (branchId.isBlank()) return Result.Error("El ID de la sucursal es requerido")
        if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
        if (productName.isBlank()) return Result.Error("El nombre del producto es requerido")
        if (quantity <= 0) return Result.Error("La cantidad debe ser mayor a 0")
        if (minStock < 0) return Result.Error("El stock mínimo no puede ser negativo")

        val now = System.currentTimeMillis()
        val stockId = "${branchId}_${productId}"

        val existingResult = stockRepository.getStockByProduct(businessId, branchId, productId)
        if (existingResult is Result.Error) return existingResult

        val existing = (existingResult as Result.Success).data

        val updatedStock = if (existing != null) {
            existing.copy(
                quantity = existing.quantity + quantity,
                minStock = minStock,
                updatedAt = now
            )
        } else {
            Stock(
                id = stockId,
                businessId = businessId,
                branchId = branchId,
                productId = productId,
                productName = productName,
                quantity = quantity,
                minStock = minStock,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        }

        val movement = StockMovement(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            branchId = branchId,
            productId = productId,
            productName = productName,
            type = MovementType.ENTRY,
            quantity = quantity,
            reason = "Entrada de stock",
            createdAt = now,
            createdBy = userId
        )

        val batchResult = stockRepository.updateStockWithMovement(updatedStock, movement)
        if (batchResult is Result.Error) return batchResult

        return Result.Success(updatedStock)
    }
}
