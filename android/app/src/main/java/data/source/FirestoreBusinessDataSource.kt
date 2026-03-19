package com.caas.app.data.source

import com.caas.app.data.model.Business
import com.caas.app.data.model.BusinessMember
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * DataSource que accede directamente a Firestore para operaciones de Business.
 */
class FirestoreBusinessDataSource(
    private val firestore: FirebaseFirestore
) {

    /**
     * Crea un nuevo negocio en Firestore.
     * @param business Objeto Business a crear
     */
    suspend fun createBusiness(business: Business) {
        firestore.collection("businesses")
            .document(business.id)
            .set(business)
            .await()
    }

    /**
     * Crea un miembro (propietario) para un negocio.
     * @param businessId ID del negocio
     * @param ownerMember Datos del propietario
     */
    suspend fun createOwnerMember(businessId: String, ownerMember: BusinessMember) {
        firestore.collection("businesses")
            .document(businessId)
            .collection("members")
            .document(ownerMember.userId)
            .set(ownerMember)
            .await()
    }

    /**
     * Actualiza un negocio existente en Firestore.
     * @param business Objeto Business con datos actualizados
     */
    suspend fun updateBusiness(business: Business) {
        firestore.collection("businesses")
            .document(business.id)
            .set(business)
            .await()
    }

    /**
     * Obtiene todos los negocios de un propietario específico.
     * @param ownerId ID del propietario (uid de Firebase Auth)
     * @return Lista de negocios del propietario
     */
    suspend fun getBusinessesByOwnerId(ownerId: String): List<Business> {
        return try {
            firestore.collection("businesses")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
                .toObjects(Business::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene un negocio específico por su ID.
     * @param businessId ID del negocio
     * @return Business o null si no existe
     */
    suspend fun getBusinessById(businessId: String): Business? {
        return try {
            firestore.collection("businesses")
                .document(businessId)
                .get()
                .await()
                .toObject(Business::class.java)
        } catch (e: Exception) {
            null
        }
    }
}