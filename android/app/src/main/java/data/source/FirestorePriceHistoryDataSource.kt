package com.caas.app.data.source

import com.caas.app.core.constants.FirestoreCollections
import com.caas.app.data.model.PriceHistory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestorePriceHistoryDataSource(
    private val firestore: FirebaseFirestore
) {

    private fun priceHistoryRef(businessId: String, productId: String) =
        firestore.collection(FirestoreCollections.BUSINESSES)
            .document(businessId)
            .collection(FirestoreCollections.SubCollections.PRODUCTS_SUB)
            .document(productId)
            .collection(FirestoreCollections.PRICE_HISTORY)

    suspend fun savePriceHistory(history: PriceHistory) {
        priceHistoryRef(history.businessId, history.productId)
            .document(history.id)
            .set(history)
            .await()
    }

    suspend fun getPriceHistory(businessId: String, productId: String): List<PriceHistory> {
        return priceHistoryRef(businessId, productId)
            .orderBy("changedAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(PriceHistory::class.java)
    }
}
