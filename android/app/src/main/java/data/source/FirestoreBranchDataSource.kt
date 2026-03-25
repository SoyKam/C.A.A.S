package com.caas.app.data.source

import com.caas.app.data.model.Branch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreBranchDataSource(
    private val firestore: FirebaseFirestore
) {

    private fun branchesRef(businessId: String) =
        firestore.collection("businesses")
            .document(businessId)
            .collection("branches")

    suspend fun createBranch(branch: Branch) {
        branchesRef(branch.businessId)
            .document(branch.id)
            .set(branch)
            .await()
    }

    suspend fun updateBranch(branch: Branch) {
        branchesRef(branch.businessId)
            .document(branch.id)
            .set(branch)
            .await()
    }

    suspend fun deleteBranch(businessId: String, branchId: String) {
        branchesRef(businessId)
            .document(branchId)
            .update("isActive", false)
            .await()
    }

    suspend fun getBranchesByBusinessId(businessId: String): List<Branch> {
        return try {
            branchesRef(businessId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
                .toObjects(Branch::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBranchById(businessId: String, branchId: String): Branch? {
        return try {
            branchesRef(businessId)
                .document(branchId)
                .get()
                .await()
                .toObject(Branch::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
