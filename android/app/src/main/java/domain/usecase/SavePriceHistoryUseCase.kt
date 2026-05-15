package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.PriceHistory
import com.caas.app.data.model.Product
import com.caas.app.domain.repository.PriceHistoryRepository
import com.google.firebase.auth.FirebaseAuth

class SavePriceHistoryUseCase(
    private val priceHistoryRepository: PriceHistoryRepository
) {

    suspend operator fun invoke(previous: Product, updated: Product): Result<Unit> {
        val costChanged = previous.costPrice != updated.costPrice
        val saleChanged = previous.salePrice != updated.salePrice

        if (!costChanged && !saleChanged) return Result.Success(Unit)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val historyId = "${updated.id}_${System.nanoTime()}"

        val history = PriceHistory(
            id = historyId,
            businessId = updated.businessId,
            productId = updated.id,
            productName = updated.name,
            previousCostPrice = previous.costPrice,
            newCostPrice = updated.costPrice,
            previousSalePrice = previous.salePrice,
            newSalePrice = updated.salePrice,
            changedAt = System.currentTimeMillis(),
            changedBy = currentUser?.uid ?: "",
            changedByEmail = currentUser?.email ?: ""
        )

        return priceHistoryRepository.savePriceHistory(history)
    }
}
