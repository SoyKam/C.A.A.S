package com.caas.app.data.source

import com.caas.app.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreProductDataSource(
    private val firestore: FirebaseFirestore
) {

    private fun productsRef(businessId: String) =
        firestore.collection("businesses")
            .document(businessId)
            .collection("products")

    suspend fun createProduct(product: Product) {
        productsRef(product.businessId)
            .document(product.id)
            .set(product)
            .await()
    }

    suspend fun updateProduct(product: Product) {
        productsRef(product.businessId)
            .document(product.id)
            .set(product)
            .await()
    }

    suspend fun deleteProduct(businessId: String, productId: String) {
        val ref = productsRef(businessId).document(productId)
        val snapshot = ref.get().await()
        val product = snapshot.toObject(Product::class.java) ?: return
        ref.set(product.copy(isActive = false, updatedAt = System.currentTimeMillis())).await()
    }

    suspend fun getProductsByBusiness(businessId: String): List<Product> {
        return productsRef(businessId)
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .toObjects(Product::class.java)
    }

    suspend fun getProductById(businessId: String, productId: String): Product? {
        return productsRef(businessId)
            .document(productId)
            .get()
            .await()
            .toObject(Product::class.java)
    }

    suspend fun searchProductsByName(businessId: String, query: String): List<Product> {
        val all = getProductsByBusiness(businessId)
        return all.filter { it.name.contains(query, ignoreCase = true) }
    }

    suspend fun getProductsByCategory(businessId: String, category: String): List<Product> {
        return productsRef(businessId)
            .whereEqualTo("isActive", true)
            .whereEqualTo("category", category)
            .get()
            .await()
            .toObjects(Product::class.java)
    }
}
