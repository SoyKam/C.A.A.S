package com.caas.app.domain.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.PriceHistory

interface PriceHistoryRepository {
    suspend fun savePriceHistory(history: PriceHistory): Result<Unit>
    suspend fun getPriceHistory(businessId: String, productId: String): Result<List<PriceHistory>>
}
