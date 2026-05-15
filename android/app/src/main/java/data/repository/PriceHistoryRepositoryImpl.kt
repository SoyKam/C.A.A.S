package com.caas.app.data.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.PriceHistory
import com.caas.app.data.source.FirestorePriceHistoryDataSource
import com.caas.app.domain.repository.PriceHistoryRepository

class PriceHistoryRepositoryImpl(
    private val dataSource: FirestorePriceHistoryDataSource
) : PriceHistoryRepository {

    override suspend fun savePriceHistory(history: PriceHistory): Result<Unit> {
        return try {
            dataSource.savePriceHistory(history)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al guardar el historial de precios", e)
        }
    }

    override suspend fun getPriceHistory(businessId: String, productId: String): Result<List<PriceHistory>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
            Result.Success(dataSource.getPriceHistory(businessId, productId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener el historial de precios", e)
        }
    }
}
