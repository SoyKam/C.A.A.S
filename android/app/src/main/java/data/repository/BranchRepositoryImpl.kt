package com.caas.app.data.repository

import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.data.source.FirestoreBranchDataSource
import com.caas.app.domain.repository.BranchRepository

class BranchRepositoryImpl(
    private val dataSource: FirestoreBranchDataSource
) : BranchRepository {

    override suspend fun createBranch(branch: Branch): Result<Branch> {
        return try {
            dataSource.createBranch(branch)
            Result.Success(branch)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Error al crear la sucursal",
                throwable = e
            )
        }
    }

    override suspend fun updateBranch(branch: Branch): Result<Branch> {
        return try {
            if (branch.id.isBlank()) {
                return Result.Error("El ID de la sucursal es requerido para actualizar")
            }
            dataSource.updateBranch(branch)
            Result.Success(branch.copy(updatedAt = System.currentTimeMillis()))
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Error al actualizar la sucursal",
                throwable = e
            )
        }
    }

    override suspend fun deleteBranch(businessId: String, branchId: String): Result<Unit> {
        return try {
            if (businessId.isBlank()) {
                return Result.Error("El ID del negocio es requerido")
            }
            if (branchId.isBlank()) {
                return Result.Error("El ID de la sucursal es requerido")
            }
            dataSource.deleteBranch(businessId, branchId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Error al eliminar la sucursal",
                throwable = e
            )
        }
    }

    override suspend fun getBranchesByBusinessId(businessId: String): Result<List<Branch>> {
        return try {
            if (businessId.isBlank()) {
                return Result.Error("El ID del negocio es requerido")
            }
            val branches = dataSource.getBranchesByBusinessId(businessId)
            Result.Success(branches)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Error al obtener las sucursales",
                throwable = e
            )
        }
    }

    override suspend fun getBranchById(businessId: String, branchId: String): Result<Branch> {
        return try {
            if (businessId.isBlank()) {
                return Result.Error("El ID del negocio es requerido")
            }
            if (branchId.isBlank()) {
                return Result.Error("El ID de la sucursal es requerido")
            }
            val branch = dataSource.getBranchById(businessId, branchId)
            if (branch != null) {
                Result.Success(branch)
            } else {
                Result.Error("Sucursal no encontrada")
            }
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Error al obtener la sucursal",
                throwable = e
            )
        }
    }
}
