package com.caas.app.data.source

import com.caas.app.data.model.Provider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreProviderDataSource(
    private val firestore: FirebaseFirestore
) {

    private fun providersRef(businessId: String) =
        firestore.collection("businesses")
            .document(businessId)
            .collection("providers")

    suspend fun createProvider(provider: Provider) {
        providersRef(provider.businessId)
            .document(provider.id)
            .set(provider)
            .await()
    }

    suspend fun updateProvider(provider: Provider) {
        providersRef(provider.businessId)
            .document(provider.id)
            .set(provider)
            .await()
    }

    suspend fun deleteProvider(businessId: String, providerId: String) {
        val ref = providersRef(businessId).document(providerId)
        val snapshot = ref.get().await()
        val provider = snapshot.toObject(Provider::class.java) ?: return
        ref.set(provider.copy(isActive = false, updatedAt = System.currentTimeMillis())).await()
    }

    suspend fun getProvidersByBusiness(businessId: String): List<Provider> {
        return providersRef(businessId)
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .toObjects(Provider::class.java)
    }

    suspend fun getProviderById(businessId: String, providerId: String): Provider? {
        return providersRef(businessId)
            .document(providerId)
            .get()
            .await()
            .toObject(Provider::class.java)
    }

    suspend fun getProvidersByProduct(businessId: String, productId: String): List<Provider> {
        return providersRef(businessId)
            .whereEqualTo("isActive", true)
            .whereArrayContains("productIds", productId)
            .get()
            .await()
            .toObjects(Provider::class.java)
    }

    suspend fun addProductToProvider(businessId: String, providerId: String, productId: String) {
        providersRef(businessId)
            .document(providerId)
            .update(
                mapOf(
                    "productIds" to FieldValue.arrayUnion(productId),
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    suspend fun removeProductFromProvider(businessId: String, providerId: String, productId: String) {
        providersRef(businessId)
            .document(providerId)
            .update(
                mapOf(
                    "productIds" to FieldValue.arrayRemove(productId),
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }
}
