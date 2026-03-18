package com.caas.app.data.source

import com.caas.app.core.constants.FirestoreCollections
import com.caas.app.data.model.Business
import com.caas.app.data.model.BusinessMember
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreBusinessDataSource(
    private val firestore: FirebaseFirestore
) {

    suspend fun createBusiness(business: Business): Business {
        firestore.collection(FirestoreCollections.BUSINESSES)
            .document(business.id)
            .set(business)
            .await()

        return business
    }

    suspend fun createOwnerMember(businessId: String, member: BusinessMember): BusinessMember {
        firestore.collection(FirestoreCollections.BUSINESSES)
            .document(businessId)
            .collection(FirestoreCollections.MEMBERS)
            .document(member.userId)
            .set(member)
            .await()

        return member
    }
}