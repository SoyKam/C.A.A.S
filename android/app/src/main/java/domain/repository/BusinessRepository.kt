package com.caas.app.domain.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.data.model.BusinessMember

/**
 * Interfaz del repositorio de Business.
 * Define los contratos para operaciones de negocio en la capa de dominio.
 */
interface BusinessRepository {

    /**
     * Crea un nuevo negocio con su propietario (miembro inicial).
     * Implementa RF-05: Crear negocio
     * Implementa RF-07: Asociar contenido al businessId del usuario autenticado
     */
    suspend fun createBusinessWithOwner(
        business: Business,
        ownerMember: BusinessMember
    ): Result<Business>

    /**
     * Actualiza la información de un negocio existente.
     * Implementa RF-06: Editar información del negocio
     */
    suspend fun updateBusiness(business: Business): Result<Business>

    /**
     * Obtiene un negocio por su ID.
     */
    suspend fun getBusinessById(businessId: String): Result<Business>

    /**
     * Obtiene todos los negocios de un propietario específico.
     */
    suspend fun getBusinessesByOwnerId(ownerId: String): Result<List<Business>>
}