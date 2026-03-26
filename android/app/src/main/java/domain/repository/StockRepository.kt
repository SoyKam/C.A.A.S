package com.caas.app.domain.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Stock
import com.caas.app.data.model.StockMovement

interface StockRepository {

    suspend fun getStockByBranch(businessId: String, branchId: String): Result<List<Stock>>

    suspend fun getStockByProduct(businessId: String, branchId: String, productId: String): Result<Stock?>

    suspend fun updateStock(stock: Stock): Result<Stock>

    suspend fun registerMovement(movement: StockMovement): Result<StockMovement>

    suspend fun getMovementsByBranch(businessId: String, branchId: String): Result<List<StockMovement>>

    suspend fun getLowStockByBranch(businessId: String, branchId: String): Result<List<Stock>>
}
