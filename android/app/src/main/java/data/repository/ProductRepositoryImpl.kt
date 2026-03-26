package com.caas.app.data.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.caas.app.data.source.FirestoreProductDataSource
import com.caas.app.domain.repository.ProductRepository

class ProductRepositoryImpl(
    private val dataSource: FirestoreProductDataSource
) : ProductRepository {

    override suspend fun createProduct(product: Product): Result<Product> {
        return try {
            if (product.id.isBlank()) return Result.Error("El ID del producto es requerido")
            dataSource.createProduct(product)
            Result.Success(product)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al crear el producto", e)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Product> {
        return try {
            if (product.id.isBlank()) return Result.Error("El ID del producto es requerido")
            dataSource.updateProduct(product)
            Result.Success(product)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar el producto", e)
        }
    }

    override suspend fun deleteProduct(businessId: String, productId: String): Result<Unit> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
            dataSource.deleteProduct(businessId, productId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al eliminar el producto", e)
        }
    }

    override suspend fun getProductsByBusiness(businessId: String): Result<List<Product>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            Result.Success(dataSource.getProductsByBusiness(businessId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener los productos", e)
        }
    }

    override suspend fun getProductById(businessId: String, productId: String): Result<Product?> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
            Result.Success(dataSource.getProductById(businessId, productId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener el producto", e)
        }
    }

    override suspend fun searchProductsByName(businessId: String, query: String): Result<List<Product>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            Result.Success(dataSource.searchProductsByName(businessId, query))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al buscar productos", e)
        }
    }

    override suspend fun getProductsByCategory(businessId: String, category: String): Result<List<Product>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (category.isBlank()) return Result.Error("La categoría es requerida")
            Result.Success(dataSource.getProductsByCategory(businessId, category))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al filtrar por categoría", e)
        }
    }
}
