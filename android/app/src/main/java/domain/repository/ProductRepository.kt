package com.caas.app.domain.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Product

interface ProductRepository {

    suspend fun createProduct(product: Product): Result<Product>

    suspend fun updateProduct(product: Product): Result<Product>

    suspend fun deleteProduct(businessId: String, productId: String): Result<Unit>

    suspend fun getProductsByBusiness(businessId: String): Result<List<Product>>

    suspend fun getProductById(businessId: String, productId: String): Result<Product?>

    suspend fun searchProductsByName(businessId: String, query: String): Result<List<Product>>

    suspend fun getProductsByCategory(businessId: String, category: String): Result<List<Product>>
}
