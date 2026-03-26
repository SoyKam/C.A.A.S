package com.caas.app.data.source

import com.caas.app.data.model.Stock
import com.caas.app.data.model.StockMovement
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreStockDataSource(
    private val firestore: FirebaseFirestore
) {

    private fun stockRef(businessId: String, branchId: String) =
        firestore.collection("businesses")
            .document(businessId)
            .collection("branches")
            .document(branchId)
            .collection("stock")

    private fun movementsRef(businessId: String, branchId: String) =
        firestore.collection("businesses")
            .document(businessId)
            .collection("branches")
            .document(branchId)
            .collection("movements")

    suspend fun getStockByBranch(businessId: String, branchId: String): List<Stock> {
        return stockRef(businessId, branchId)
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .toObjects(Stock::class.java)
    }

    suspend fun getStockByProduct(businessId: String, branchId: String, productId: String): Stock? {
        val stockId = "${branchId}_${productId}"
        return stockRef(businessId, branchId)
            .document(stockId)
            .get()
            .await()
            .toObject(Stock::class.java)
    }

    suspend fun updateStock(stock: Stock) {
        stockRef(stock.businessId, stock.branchId)
            .document(stock.id)
            .set(stock)
            .await()
    }

    suspend fun registerMovement(movement: StockMovement) {
        movementsRef(movement.businessId, movement.branchId)
            .document(movement.id)
            .set(movement)
            .await()
    }

    suspend fun getMovementsByBranch(businessId: String, branchId: String): List<StockMovement> {
        return movementsRef(businessId, branchId)
            .get()
            .await()
            .toObjects(StockMovement::class.java)
    }

    suspend fun getLowStockByBranch(businessId: String, branchId: String): List<Stock> {
        val allStock = getStockByBranch(businessId, branchId)
        return allStock.filter { it.quantity <= it.minStock }
    }
}
