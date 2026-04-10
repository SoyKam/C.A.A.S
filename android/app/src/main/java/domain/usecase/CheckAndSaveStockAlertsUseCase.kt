package com.caas.app.domain.usecase

import android.content.Context
import com.caas.app.core.notifications.StockAlertNotificationHelper
import com.caas.app.core.result.Result
import com.caas.app.data.model.StockAlert
import com.caas.app.domain.repository.BranchRepository
import com.caas.app.domain.repository.StockRepository

class CheckAndSaveStockAlertsUseCase(
    private val stockRepository: StockRepository,
    private val branchRepository: BranchRepository,
    private val context: Context
) {
    suspend operator fun invoke(businessId: String): Result<Unit> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")

        val branchNameMap: Map<String, String> = when (val r = branchRepository.getBranchesByBusinessId(businessId)) {
            is Result.Success -> r.data.associate { it.id to it.name }
            else -> emptyMap()
        }

        val lowStockItems = when (val r = stockRepository.getAllLowStockByBusiness(businessId)) {
            is Result.Success -> r.data
            is Result.Error -> return Result.Error(r.message)
            is Result.Loading -> return Result.Error("Estado inesperado")
        }

        val unreadAlertIds: Set<String> = when (val r = stockRepository.getUnreadAlerts(businessId)) {
            is Result.Success -> r.data.map { it.id }.toSet()
            else -> emptySet()
        }

        for (stock in lowStockItems) {
            val alertId = "${businessId}_${stock.productId}_${stock.branchId}"
            if (alertId in unreadAlertIds) continue

            val branchName = branchNameMap[stock.branchId] ?: stock.branchId

            val alert = StockAlert(
                id = alertId,
                businessId = businessId,
                branchId = stock.branchId,
                branchName = branchName,
                productId = stock.productId,
                productName = stock.productName,
                currentStock = stock.quantity,
                minStock = stock.minStock,
                isRead = false,
                createdAt = System.currentTimeMillis()
            )

            stockRepository.saveStockAlert(alert)

            StockAlertNotificationHelper.showLowStockNotification(
                context = context,
                productName = stock.productName,
                branchName = branchName,
                currentStock = stock.quantity,
                notificationId = alertId.hashCode()
            )
        }

        return Result.Success(Unit)
    }
}
