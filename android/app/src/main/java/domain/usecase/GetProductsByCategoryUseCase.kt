package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.caas.app.domain.repository.ProductRepository

class GetProductsByCategoryUseCase(
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(businessId: String, category: String): Result<List<Product>> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (category.isBlank()) return Result.Error("La categoría es requerida")
        return productRepository.getProductsByCategory(businessId, category)
    }
}
