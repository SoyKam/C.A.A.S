package com.caas.app.data.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.data.model.BusinessMember
import com.caas.app.data.source.FirestoreBusinessDataSource
import com.caas.app.domain.repository.BusinessRepository

/**
 * Implementación de BusinessRepository.
 * Maneja la lógica de acceso a datos de negocio con manejo de errores y Result.
 */
class BusinessRepositoryImpl(
    private val dataSource: FirestoreBusinessDataSource
) : BusinessRepository {

    /**
     * Crea un nuevo negocio con su propietario.
     * Implementa RF-05 y RF-07
     *
     * @param business Objeto Business a crear
     * @param ownerMember Objeto BusinessMember del propietario
     * @return Result<Business> con el negocio creado o error
     */
    override suspend fun createBusinessWithOwner(
        business: Business,
        ownerMember: BusinessMember
    ): Result<Business> {
        return try {
            dataSource.createBusiness(business)
            dataSource.createOwnerMember(business.id, ownerMember)
            Result.Success(business)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Error al crear el negocio",
                throwable = e
            )
        }
    }

    /**
     * Actualiza la información de un negocio existente.
     * Implementa RF-06: Editar información del negocio
     *
     * @param business Objeto Business con datos actualizados (id es obligatorio)
     * @return Result<Business> con el negocio actualizado o error
     */
    override suspend fun updateBusiness(business: Business): Result<Business> {
        return try {
            // Validar que el ID no esté vacío
            if (business.id.isBlank()) {
                return Result.Error("El ID del negocio es requerido para actualizar")
            }

            dataSource.updateBusiness(business)
            Result.Success(business.copy(updatedAt = System.currentTimeMillis()))
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Error al actualizar el negocio",
                throwable = e
            )
        }
    }

    /**
     * Obtiene un negocio por su ID.
     *
     * @param businessId ID del negocio a recuperar
     * @return Result<Business> con el negocio encontrado o error
     */
    override suspend fun getBusinessById(businessId: String): Result<Business> {
        return try {
            if (businessId.isBlank()) {
                return Result.Error("El ID del negocio es requerido")
            }

            val business = dataSource.getBusinessById(businessId)
            if (business != null) {
                Result.Success(business)
            } else {
                Result.Error("Negocio no encontrado")
            }
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Error al obtener el negocio",
                throwable = e
            )
        }
    }

    /**
     * Obtiene todos los negocios de un propietario específico.
     *
     * @param ownerId ID del propietario
     * @return Result con la lista de negocios
     */
    override suspend fun getBusinessesByOwnerId(ownerId: String): Result<List<Business>> {
        return try {
            if (ownerId.isBlank()) {
                return Result.Error("El ID del propietario es requerido")
            }
            val businesses = dataSource.getBusinessesByOwnerId(ownerId)
            Result.Success(businesses)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Error al obtener los negocios",
                throwable = e
            )
        }
    }
}