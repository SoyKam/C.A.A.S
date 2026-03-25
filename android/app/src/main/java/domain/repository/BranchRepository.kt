package com.caas.app.domain.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch

interface BranchRepository {

    suspend fun createBranch(branch: Branch): Result<Branch>

    suspend fun updateBranch(branch: Branch): Result<Branch>

    suspend fun deleteBranch(businessId: String, branchId: String): Result<Unit>

    suspend fun getBranchesByBusinessId(businessId: String): Result<List<Branch>>

    suspend fun getBranchById(businessId: String, branchId: String): Result<Branch>
}
