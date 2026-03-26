package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.caas.app.domain.repository.ProductRepository

class CreateProductUseCase(
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(
        businessId: String,
        name: String,
        sku: String,
        category: String,
        costPrice: Double,
        salePrice: Double,
        imageUrl: String
    ): Result<Product> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (name.isBlank()) return Result.Error("El nombre del producto es requerido")
        if (sku.isBlank()) return Result.Error("El SKU es requerido")
        if (category.isBlank()) return Result.Error("La categoría es requerida")
        if (costPrice < 0) return Result.Error("El precio de costo no puede ser negativo")
        if (salePrice < 0) return Result.Error("El precio de venta no puede ser negativo")
        if (salePrice < costPrice) return Result.Error("El precio de venta debe ser mayor o igual al precio de costo")

        val now = System.currentTimeMillis()
        val productId = "${businessId}_${System.nanoTime()}"

        val product = Product(
            id = productId,
            businessId = businessId,
            name = name,
            sku = sku,
            category = category,
            costPrice = costPrice,
            salePrice = salePrice,
            imageUrl = imageUrl,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        return productRepository.createProduct(product)
    }
}
