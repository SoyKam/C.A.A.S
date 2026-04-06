package com.caas.app.data.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Provider
import com.caas.app.data.source.FirestoreProviderDataSource
import com.caas.app.domain.repository.ProviderRepository

class ProviderRepositoryImpl(
    private val dataSource: FirestoreProviderDataSource
) : ProviderRepository {

    override suspend fun createProvider(provider: Provider): Result<Provider> {
        return try {
            if (provider.id.isBlank()) return Result.Error("El ID del proveedor es requerido")
            dataSource.createProvider(provider)
            Result.Success(provider)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al crear el proveedor", e)
        }
    }

    override suspend fun updateProvider(provider: Provider): Result<Provider> {
        return try {
            if (provider.id.isBlank()) return Result.Error("El ID del proveedor es requerido")
            dataSource.updateProvider(provider)
            Result.Success(provider)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar el proveedor", e)
        }
    }

    override suspend fun deleteProvider(businessId: String, providerId: String): Result<Unit> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (providerId.isBlank()) return Result.Error("El ID del proveedor es requerido")
            dataSource.deleteProvider(businessId, providerId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al eliminar el proveedor", e)
        }
    }

    override suspend fun getProvidersByBusiness(businessId: String): Result<List<Provider>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            Result.Success(dataSource.getProvidersByBusiness(businessId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener los proveedores", e)
        }
    }

    override suspend fun getProviderById(businessId: String, providerId: String): Result<Provider?> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (providerId.isBlank()) return Result.Error("El ID del proveedor es requerido")
            Result.Success(dataSource.getProviderById(businessId, providerId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener el proveedor", e)
        }
    }

    override suspend fun getProvidersByProduct(businessId: String, productId: String): Result<List<Provider>> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
            Result.Success(dataSource.getProvidersByProduct(businessId, productId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener proveedores por producto", e)
        }
    }

    override suspend fun addProductToProvider(businessId: String, providerId: String, productId: String): Result<Unit> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (providerId.isBlank()) return Result.Error("El ID del proveedor es requerido")
            if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
            dataSource.addProductToProvider(businessId, providerId, productId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al asociar el producto", e)
        }
    }

    override suspend fun removeProductFromProvider(businessId: String, providerId: String, productId: String): Result<Unit> {
        return try {
            if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
            if (providerId.isBlank()) return Result.Error("El ID del proveedor es requerido")
            if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
            dataSource.removeProductFromProvider(businessId, providerId, productId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al desasociar el producto", e)
        }
    }
}
