package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.domain.repository.ProductRepository

class DeleteProductUseCase(
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(businessId: String, productId: String): Result<Unit> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
        return productRepository.deleteProduct(businessId, productId)
    }
}
