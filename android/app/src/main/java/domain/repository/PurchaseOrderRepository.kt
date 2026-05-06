package com.caas.app.domain.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.PurchaseOrder
import com.caas.app.data.model.PurchaseOrderStatus

/**
 * Interfaz del repositorio de órdenes de compra.
 * Implementa RF-40 a RF-44.
 */
interface PurchaseOrderRepository {

    suspend fun createPurchaseOrder(order: PurchaseOrder): Result<PurchaseOrder>

    suspend fun updatePurchaseOrderStatus(
        businessId: String,
        orderId: String,
        status: PurchaseOrderStatus
    ): Result<Unit>

    suspend fun getPurchaseOrdersByBusiness(businessId: String): Result<List<PurchaseOrder>>

    suspend fun getPurchaseOrdersByStatus(
        businessId: String,
        status: PurchaseOrderStatus
    ): Result<List<PurchaseOrder>>

    suspend fun getPurchaseOrdersByProvider(
        businessId: String,
        providerId: String
    ): Result<List<PurchaseOrder>>

    suspend fun getPurchaseOrdersByDateRange(
        businessId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<PurchaseOrder>>

    suspend fun getPurchaseOrderById(businessId: String, orderId: String): Result<PurchaseOrder?>
}
