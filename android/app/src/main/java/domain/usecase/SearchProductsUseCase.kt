package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.caas.app.domain.repository.ProductRepository

class SearchProductsUseCase(
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(businessId: String, query: String): Result<List<Product>> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (query.isBlank()) return productRepository.getProductsByBusiness(businessId)
        return productRepository.searchProductsByName(businessId, query)
    }
}
