package com.caas.app.domain.usecase

import android.util.Patterns
import com.caas.app.core.result.Result
import com.caas.app.data.model.Provider
import com.caas.app.domain.repository.ProviderRepository

class CreateProviderUseCase(
    private val providerRepository: ProviderRepository
) {

    suspend operator fun invoke(
        businessId: String,
        name: String,
        phone: String,
        email: String,
        productIds: List<String> = emptyList()
    ): Result<Provider> {
        if (businessId.isBlank()) return Result.Error("El ID del negocio es requerido")
        if (name.isBlank()) return Result.Error("El nombre del proveedor es requerido")
        if (phone.isBlank()) return Result.Error("El teléfono del proveedor es requerido")
        if (email.isBlank()) return Result.Error("El correo electrónico es requerido")
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return Result.Error("El formato del correo no es válido")

        val now = System.currentTimeMillis()
        val providerId = "${businessId}_${System.nanoTime()}"

        val provider = Provider(
            id = providerId,
            businessId = businessId,
            name = name,
            phone = phone,
            email = email,
            productIds = productIds,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        return providerRepository.createProvider(provider)
    }
}
