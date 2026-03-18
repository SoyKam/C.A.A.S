package com.caas.app.data.source

import com.caas.app.core.constants.FirestoreCollections
import com.caas.app.core.constants.FirestoreFields
import com.caas.app.data.model.Business
import com.caas.app.data.model.BusinessMember
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * DataSource para operaciones de Business en Firestore.
 * Maneja la persistencia y recuperación de datos de negocio.
 */
class FirestoreBusinessDataSource(
    private val firestore: FirebaseFirestore
) {

    /**
     * Crea un nuevo negocio en Firestore.
     * Implementa RF-05
     *
     * @param business Objeto Business a persistir
     * @return Business creado
     * @throws Exception si ocurre error en Firestore
     */
    suspend fun createBusiness(business: Business): Business {
        firestore.collection(FirestoreCollections.BUSINESSES)
            .document(business.id)
            .set(business)
            .await()

        return business
    }

    /**
     * Crea el miembro propietario del negocio en subcollection.
     * Implementa RF-07: Asocia el miembro al businessId
     *
     * @param businessId ID del negocio propietario
     * @param member Objeto BusinessMember del propietario
     * @return BusinessMember creado
     * @throws Exception si ocurre error en Firestore
     */
    suspend fun createOwnerMember(businessId: String, member: BusinessMember): BusinessMember {
        firestore.collection(FirestoreCollections.BUSINESSES)
            .document(businessId)
            .collection(FirestoreCollections.MEMBERS)
            .document(member.userId)
            .set(member)
            .await()

        return member
    }

    /**
     * Actualiza la información de un negocio existente.
     * Implementa RF-06: Editar información del negocio
     * Solo actualiza campos editables (name, sector, taxId, updatedAt)
     *
     * @param business Objeto Business con datos actualizados (id es obligatorio)
     * @throws Exception si ocurre error en Firestore
     */
    suspend fun updateBusiness(business: Business) {
        val updateData = mapOf(
            FirestoreFields.NAME to business.name,
            "sector" to business.sector,
            "taxId" to business.taxId,
            FirestoreFields.UPDATED_AT to System.currentTimeMillis()
        )

        firestore.collection(FirestoreCollections.BUSINESSES)
            .document(business.id)
            .update(updateData)
            .await()
    }

    /**
     * Obtiene un negocio por su ID.
     *
     * @param businessId ID del negocio a recuperar
     * @return Business encontrado
     * @throws Exception si ocurre error en Firestore o documento no existe
     */
    suspend fun getBusinessById(businessId: String): Business {
        val snapshot = firestore.collection(FirestoreCollections.BUSINESSES)
            .document(businessId)
            .get()
            .await()

        return snapshot.toObject(Business::class.java)
            ?: throw Exception("Negocio no encontrado con ID: $businessId")
    }
}