package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.PurchaseOrderStatus
import com.caas.app.domain.repository.PurchaseOrderRepository

/**
 * RF-42: Actualiza el estado de una orden de compra validando las transiciones permitidas.
 *
 * Transiciones:
 *   PENDING  → SENT, CANCELLED
 *   SENT     → RECEIVED, CANCELLED
 *   RECEIVED → (sin cambios)
 *   CANCELLED→ (sin cambios)
 */
class UpdatePurchaseOrderStatusUseCase(
    private val purchaseOrderRepository: PurchaseOrderRepository
) {

    suspend operator fun invoke(
        businessId: String,
        orderId: String,
        newStatus: PurchaseOrderStatus
    ): Result<Unit> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (orderId.isBlank()) return Result.Error("El ID de la orden es requerido")

        val orderResult = purchaseOrderRepository.getPurchaseOrderById(businessId, orderId)
        if (orderResult is Result.Error) return orderResult

        val order = (orderResult as Result.Success).data
            ?: return Result.Error("Orden de compra no encontrada")

        val validTransition = when (order.status) {
            PurchaseOrderStatus.PENDING -> newStatus == PurchaseOrderStatus.SENT || newStatus == PurchaseOrderStatus.CANCELLED
            PurchaseOrderStatus.SENT -> newStatus == PurchaseOrderStatus.RECEIVED || newStatus == PurchaseOrderStatus.CANCELLED
            PurchaseOrderStatus.RECEIVED -> false
            PurchaseOrderStatus.CANCELLED -> false
        }

        if (!validTransition) {
            return Result.Error("Transición de estado no permitida: ${order.status} → $newStatus")
        }

        return purchaseOrderRepository.updatePurchaseOrderStatus(businessId, orderId, newStatus)
    }
}
