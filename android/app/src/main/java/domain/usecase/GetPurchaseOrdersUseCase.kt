package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.PurchaseOrder
import com.caas.app.data.model.PurchaseOrderStatus
import com.caas.app.domain.repository.PurchaseOrderRepository

/**
 * RF-44: Obtiene órdenes de compra con filtros opcionales.
 * Si un filtro es null, no se aplica ese criterio.
 */
class GetPurchaseOrdersUseCase(
    private val purchaseOrderRepository: PurchaseOrderRepository
) {

    suspend operator fun invoke(
        businessId: String,
        status: PurchaseOrderStatus? = null,
        providerId: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Result<List<PurchaseOrder>> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")

        // Determina la consulta base según filtros disponibles
        val baseResult: Result<List<PurchaseOrder>> = when {
            status != null -> purchaseOrderRepository.getPurchaseOrdersByStatus(businessId, status)
            providerId != null -> purchaseOrderRepository.getPurchaseOrdersByProvider(businessId, providerId)
            startDate != null && endDate != null ->
                purchaseOrderRepository.getPurchaseOrdersByDateRange(businessId, startDate, endDate)
            else -> purchaseOrderRepository.getPurchaseOrdersByBusiness(businessId)
        }

        if (baseResult is Result.Error) return baseResult

        var orders = (baseResult as Result.Success).data

        // Aplica filtros adicionales en memoria
        if (status != null) orders = orders.filter { it.status == status }
        if (!providerId.isNullOrBlank()) orders = orders.filter { it.providerId == providerId }
        if (startDate != null) orders = orders.filter { it.createdAt >= startDate }
        if (endDate != null) orders = orders.filter { it.createdAt <= endDate }

        return Result.Success(orders)
    }
}
