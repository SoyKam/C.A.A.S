package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.PurchaseOrder
import com.caas.app.data.model.PurchaseOrderStatus
import com.caas.app.data.model.Stock
import com.caas.app.data.model.StockMovement
import com.caas.app.domain.repository.PurchaseOrderRepository
import com.caas.app.domain.repository.StockRepository
import java.util.UUID

/**
 * RF-43: Recibe una orden de compra, actualiza el stock y registra movimientos de entrada.
 */
class ReceivePurchaseOrderUseCase(
    private val purchaseOrderRepository: PurchaseOrderRepository,
    private val stockRepository: StockRepository
) {

    suspend operator fun invoke(
        businessId: String,
        orderId: String,
        receivedBy: String = ""
    ): Result<PurchaseOrder> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (orderId.isBlank()) return Result.Error("El ID de la orden es requerido")

        val orderResult = purchaseOrderRepository.getPurchaseOrderById(businessId, orderId)
        if (orderResult is Result.Error) return orderResult

        val order = (orderResult as Result.Success).data
            ?: return Result.Error("Orden de compra no encontrada")

        if (order.status != PurchaseOrderStatus.SENT) {
            return Result.Error("Solo se pueden recibir órdenes en estado Enviada")
        }

        // Actualizar estado a RECEIVED
        val updateResult = purchaseOrderRepository.updatePurchaseOrderStatus(
            businessId, orderId, PurchaseOrderStatus.RECEIVED
        )
        if (updateResult is Result.Error) return updateResult

        val now = System.currentTimeMillis()
        val branchId = order.branchId

        val stockUpdates = mutableListOf<Stock>()
        val movements = mutableListOf<StockMovement>()

        for (item in order.items) {
            val existingResult = stockRepository.getStockByProduct(businessId, branchId, item.productId)
            val existing = if (existingResult is Result.Success) existingResult.data else null

            val stockId = "${branchId}_${item.productId}"
            val updatedStock = if (existing != null) {
                existing.copy(quantity = existing.quantity + item.quantity, updatedAt = now)
            } else {
                Stock(
                    id = stockId,
                    businessId = businessId,
                    branchId = branchId,
                    productId = item.productId,
                    productName = item.productName,
                    quantity = item.quantity,
                    minStock = 0,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )
            }
            stockUpdates.add(updatedStock)

            movements.add(StockMovement(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                branchId = branchId,
                productId = item.productId,
                productName = item.productName,
                type = MovementType.ENTRY,
                quantity = item.quantity,
                reason = "Recepción de orden de compra #$orderId",
                createdAt = now,
                createdBy = receivedBy
            ))
        }

        val batchResult = stockRepository.updateStocksWithMovements(stockUpdates, movements)
        if (batchResult is Result.Error) return batchResult

        return Result.Success(order.copy(status = PurchaseOrderStatus.RECEIVED, updatedAt = now))
    }
}
