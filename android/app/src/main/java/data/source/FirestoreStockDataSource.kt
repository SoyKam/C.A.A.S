package com.caas.app.data.source

import com.caas.app.data.model.MovementType
import com.caas.app.data.model.Stock
import com.caas.app.data.model.StockAlert
import com.caas.app.data.model.StockMovement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(StockMovement::class.java)
    }

    suspend fun getMovementsByType(
        businessId: String,
        branchId: String,
        type: MovementType
    ): List<StockMovement> {
        return movementsRef(businessId, branchId)
            .whereEqualTo("type", type.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(StockMovement::class.java)
    }

    suspend fun getMovementsByDateRange(
        businessId: String,
        branchId: String,
        startDate: Long,
        endDate: Long
    ): List<StockMovement> {
        return movementsRef(businessId, branchId)
            .whereGreaterThanOrEqualTo("createdAt", startDate)
            .whereLessThanOrEqualTo("createdAt", endDate)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(StockMovement::class.java)
    }

    suspend fun getMovementsByProduct(
        businessId: String,
        branchId: String,
        productId: String
    ): List<StockMovement> {
        return movementsRef(businessId, branchId)
            .whereEqualTo("productId", productId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(StockMovement::class.java)
    }

    suspend fun getLowStockByBranch(businessId: String, branchId: String): List<Stock> {
        val allStock = getStockByBranch(businessId, branchId)
        return allStock.filter { it.quantity <= it.minStock }
    }

    suspend fun getAllLowStockByBusiness(businessId: String): List<Stock> {
        val branchDocs = firestore.collection("businesses")
            .document(businessId)
            .collection("branches")
            .whereEqualTo("isActive", true)
            .get()
            .await()
        return branchDocs.documents.flatMap { branchDoc ->
            getLowStockByBranch(businessId, branchDoc.id)
        }
    }

    private fun alertsRef(businessId: String) =
        firestore.collection("businesses")
            .document(businessId)
            .collection("stockAlerts")

    suspend fun saveStockAlert(alert: StockAlert) {
        alertsRef(alert.businessId)
            .document(alert.id)
            .set(alert)
            .await()
    }

    suspend fun getUnreadAlerts(businessId: String): List<StockAlert> {
        return alertsRef(businessId)
            .whereEqualTo("isRead", false)
            .get()
            .await()
            .toObjects(StockAlert::class.java)
    }

    suspend fun markAlertAsRead(businessId: String, alertId: String) {
        alertsRef(businessId)
            .document(alertId)
            .update("isRead", true)
            .await()
    }
}
