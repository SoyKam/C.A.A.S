package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.caas.app.domain.repository.ProductRepository

class GetProductsByBusinessUseCase(
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(businessId: String): Result<List<Product>> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        return productRepository.getProductsByBusiness(businessId)
    }
}
