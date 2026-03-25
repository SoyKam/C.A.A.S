package com.caas.app.data.source

import android.util.Log
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
        Log.d("BRANCH_DEBUG", "=== createBranch ===")
        Log.d("BRANCH_DEBUG", "businessId: '${branch.businessId}'")
        Log.d("BRANCH_DEBUG", "branchId:   '${branch.id}'")
        Log.d("BRANCH_DEBUG", "path: businesses/${branch.businessId}/branches/${branch.id}")
        branchesRef(branch.businessId)
            .document(branch.id)
            .set(branch)
            .await()
        Log.d("BRANCH_DEBUG", "write completed OK")
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
        Log.d("BRANCH_DEBUG", "=== getBranchesByBusinessId ===")
        Log.d("BRANCH_DEBUG", "querying businessId: '$businessId'")
        val result = branchesRef(businessId)
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .toObjects(Branch::class.java)
        Log.d("BRANCH_DEBUG", "found ${result.size} branches")
        return result
    }

    suspend fun getBranchById(businessId: String, branchId: String): Branch? {
        return branchesRef(businessId)
            .document(branchId)
            .get()
            .await()
            .toObject(Branch::class.java)
    }
}
