package com.caas.app.data.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Stock
import com.caas.app.data.model.StockMovement
import com.caas.app.data.source.FirestoreStockDataSource
import com.caas.app.domain.repository.StockRepository

class StockRepositoryImpl(
    private val dataSource: FirestoreStockDataSource
) : StockRepository {

    override suspend fun getStockByBranch(businessId: String, branchId: String): Result<List<Stock>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (branchId.isBlank()) return Result.Error("El ID de la sucursal es requerido")
            Result.Success(dataSource.getStockByBranch(businessId, branchId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener el stock", e)
        }
    }

    override suspend fun getStockByProduct(
        businessId: String,
        branchId: String,
        productId: String
    ): Result<Stock?> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (branchId.isBlank()) return Result.Error("El ID de la sucursal es requerido")
            if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
            Result.Success(dataSource.getStockByProduct(businessId, branchId, productId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener el stock del producto", e)
        }
    }

    override suspend fun updateStock(stock: Stock): Result<Stock> {
        return try {
            if (stock.id.isBlank()) return Result.Error("El ID del stock es requerido")
            dataSource.updateStock(stock)
            Result.Success(stock)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar el stock", e)
        }
    }

    override suspend fun registerMovement(movement: StockMovement): Result<StockMovement> {
        return try {
            if (movement.id.isBlank()) return Result.Error("El ID del movimiento es requerido")
            dataSource.registerMovement(movement)
            Result.Success(movement)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al registrar el movimiento", e)
        }
    }

    override suspend fun getMovementsByBranch(
        businessId: String,
        branchId: String
    ): Result<List<StockMovement>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (branchId.isBlank()) return Result.Error("El ID de la sucursal es requerido")
            Result.Success(dataSource.getMovementsByBranch(businessId, branchId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener los movimientos", e)
        }
    }

    override suspend fun getLowStockByBranch(
        businessId: String,
        branchId: String
    ): Result<List<Stock>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (branchId.isBlank()) return Result.Error("El ID de la sucursal es requerido")
            Result.Success(dataSource.getLowStockByBranch(businessId, branchId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener alertas de stock", e)
        }
    }
}
