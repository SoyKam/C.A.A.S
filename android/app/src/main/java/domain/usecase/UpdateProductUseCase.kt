package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.caas.app.domain.repository.ProductRepository

class UpdateProductUseCase(
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(
        businessId: String,
        productId: String,
        name: String,
        sku: String,
        category: String,
        costPrice: Double,
        salePrice: Double,
        imageUrl: String
    ): Result<Product> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (productId.isBlank()) return Result.Error("El ID del producto es requerido")
        if (name.isBlank()) return Result.Error("El nombre del producto es requerido")
        if (sku.isBlank()) return Result.Error("El SKU es requerido")
        if (category.isBlank()) return Result.Error("La categoría es requerida")
        if (costPrice < 0) return Result.Error("El precio de costo no puede ser negativo")
        if (salePrice < 0) return Result.Error("El precio de venta no puede ser negativo")
        if (salePrice < costPrice) return Result.Error("El precio de venta debe ser mayor o igual al precio de costo")

        val existingResult = productRepository.getProductById(businessId, productId)
        if (existingResult is Result.Error) return existingResult
        val existing = (existingResult as Result.Success).data
            ?: return Result.Error("Producto no encontrado")

        val updated = existing.copy(
            name = name,
            sku = sku,
            category = category,
            costPrice = costPrice,
            salePrice = salePrice,
            imageUrl = imageUrl,
            updatedAt = System.currentTimeMillis()
        )

        return productRepository.updateProduct(updated)
    }
}
