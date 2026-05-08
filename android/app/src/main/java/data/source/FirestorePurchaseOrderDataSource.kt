package com.caas.app.data.source

import com.caas.app.core.constants.FirestoreCollections
import com.caas.app.data.model.PurchaseOrder
import com.caas.app.data.model.PurchaseOrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * DataSource de Firestore para órdenes de compra.
 * Colección: businesses/{businessId}/purchaseOrders/{orderId}
 */
class FirestorePurchaseOrderDataSource(
    private val firestore: FirebaseFirestore
) {

    private fun ordersRef(businessId: String) =
        firestore.collection(FirestoreCollections.BUSINESSES)
            .document(businessId)
            .collection(FirestoreCollections.PURCHASE_ORDERS)

    suspend fun createPurchaseOrder(order: PurchaseOrder) {
        ordersRef(order.businessId)
            .document(order.id)
            .set(order)
            .await()
    }

    suspend fun updatePurchaseOrderStatus(
        businessId: String,
        orderId: String,
        status: PurchaseOrderStatus
    ) {
        ordersRef(businessId)
            .document(orderId)
            .update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    suspend fun getPurchaseOrdersByBusiness(businessId: String): List<PurchaseOrder> {
        return ordersRef(businessId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(PurchaseOrder::class.java)
    }

    suspend fun getPurchaseOrdersByStatus(
        businessId: String,
        status: PurchaseOrderStatus
    ): List<PurchaseOrder> {
        return ordersRef(businessId)
            .whereEqualTo("status", status.name)
            .get()
            .await()
            .toObjects(PurchaseOrder::class.java)
            .sortedByDescending { it.createdAt }
    }

    suspend fun getPurchaseOrdersByProvider(
        businessId: String,
        providerId: String
    ): List<PurchaseOrder> {
        return ordersRef(businessId)
            .whereEqualTo("providerId", providerId)
            .get()
            .await()
            .toObjects(PurchaseOrder::class.java)
            .sortedByDescending { it.createdAt }
    }

    suspend fun getPurchaseOrdersByDateRange(
        businessId: String,
        startDate: Long,
        endDate: Long
    ): List<PurchaseOrder> {
        return ordersRef(businessId)
            .whereGreaterThanOrEqualTo("createdAt", startDate)
            .whereLessThanOrEqualTo("createdAt", endDate)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(PurchaseOrder::class.java)
    }

    suspend fun getPurchaseOrderById(businessId: String, orderId: String): PurchaseOrder? {
        return ordersRef(businessId)
            .document(orderId)
            .get()
            .await()
            .toObject(PurchaseOrder::class.java)
    }
}
