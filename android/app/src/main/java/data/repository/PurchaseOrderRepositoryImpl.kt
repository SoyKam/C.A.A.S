package com.caas.app.data.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.PurchaseOrder
import com.caas.app.data.model.PurchaseOrderStatus
import com.caas.app.data.source.FirestorePurchaseOrderDataSource
import com.caas.app.domain.repository.PurchaseOrderRepository

/**
 * Implementación de PurchaseOrderRepository.
 * Envuelve cada operación en try/catch devolviendo Result.
 */
class PurchaseOrderRepositoryImpl(
    private val dataSource: FirestorePurchaseOrderDataSource
) : PurchaseOrderRepository {

    override suspend fun createPurchaseOrder(order: PurchaseOrder): Result<PurchaseOrder> {
        return try {
            if (order.id.isBlank()) return Result.Error("El ID de la orden es requerido")
            dataSource.createPurchaseOrder(order)
            Result.Success(order)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al crear la orden de compra", e)
        }
    }

    override suspend fun updatePurchaseOrderStatus(
        businessId: String,
        orderId: String,
        status: PurchaseOrderStatus
    ): Result<Unit> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (orderId.isBlank()) return Result.Error("El ID de la orden es requerido")
            dataSource.updatePurchaseOrderStatus(businessId, orderId, status)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar el estado de la orden", e)
        }
    }

    override suspend fun getPurchaseOrdersByBusiness(businessId: String): Result<List<PurchaseOrder>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            Result.Success(dataSource.getPurchaseOrdersByBusiness(businessId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener las órdenes de compra", e)
        }
    }

    override suspend fun getPurchaseOrdersByStatus(
        businessId: String,
        status: PurchaseOrderStatus
    ): Result<List<PurchaseOrder>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            Result.Success(dataSource.getPurchaseOrdersByStatus(businessId, status))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al filtrar órdenes por estado", e)
        }
    }

    override suspend fun getPurchaseOrdersByProvider(
        businessId: String,
        providerId: String
    ): Result<List<PurchaseOrder>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (providerId.isBlank()) return Result.Error("El ID del proveedor es requerido")
            Result.Success(dataSource.getPurchaseOrdersByProvider(businessId, providerId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al filtrar órdenes por proveedor", e)
        }
    }

    override suspend fun getPurchaseOrdersByDateRange(
        businessId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<PurchaseOrder>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            Result.Success(dataSource.getPurchaseOrdersByDateRange(businessId, startDate, endDate))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al filtrar órdenes por fecha", e)
        }
    }

    override suspend fun getPurchaseOrderById(
        businessId: String,
        orderId: String
    ): Result<PurchaseOrder?> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (orderId.isBlank()) return Result.Error("El ID de la orden es requerido")
            Result.Success(dataSource.getPurchaseOrderById(businessId, orderId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener la orden de compra", e)
        }
    }
}
