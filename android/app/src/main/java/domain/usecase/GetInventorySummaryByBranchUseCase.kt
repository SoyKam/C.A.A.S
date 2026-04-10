package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.domain.model.InventorySummaryItem
import com.caas.app.domain.repository.ProductRepository
import com.caas.app.domain.repository.StockRepository

/**
 * UseCase para obtener el resumen de inventario de una sucursal (RF-21).
 * Une datos de stock con datos de productos para construir la vista de resumen.
 * Marca items con isCritical = true cuando quantity < minStock.
 */
class GetInventorySummaryByBranchUseCase(
    private val stockRepository: StockRepository,
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(
        businessId: String,
        branchId: String
    ): Result<List<InventorySummaryItem>> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (branchId.isBlank()) return Result.Error("El ID de la sucursal es requerido")

        return try {
            val stockResult = stockRepository.getStockByBranch(businessId, branchId)
            if (stockResult is Result.Error) return stockResult

            val stocks = (stockResult as Result.Success).data

            // Carga productos en un solo query para obtener SKU
            val productMap = when (val productsResult = productRepository.getProductsByBusiness(businessId)) {
                is Result.Success -> productsResult.data.associateBy { it.id }
                else -> emptyMap()
            }

            val summaryItems = stocks.map { stock ->
                val product = productMap[stock.productId]
                InventorySummaryItem(
                    stockId = stock.id,
                    productId = stock.productId,
                    productName = stock.productName,
                    sku = product?.sku.orEmpty(),
                    quantity = stock.quantity,
                    minStock = stock.minStock,
                    isCritical = stock.quantity < stock.minStock
                )
            }.sortedWith(
                compareByDescending<InventorySummaryItem> { it.isCritical }
                    .thenBy { it.productName.lowercase() }
            )

            Result.Success(summaryItems)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener el resumen de inventario", e)
        }
    }
}
