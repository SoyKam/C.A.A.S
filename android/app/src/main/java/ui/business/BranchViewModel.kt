package com.caas.app.ui.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.data.repository.BranchRepositoryImpl
import com.caas.app.data.source.FirestoreBranchDataSource
import com.caas.app.domain.repository.BranchRepository
import com.caas.app.domain.usecase.CreateBranchUseCase
import com.caas.app.domain.usecase.UpdateBranchUseCase
import com.caas.app.domain.usecase.DeleteBranchUseCase
import com.caas.app.domain.usecase.GetBranchesByBusinessUseCase
import com.caas.app.domain.usecase.GetBranchByIdUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BranchViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val dataSource = FirestoreBranchDataSource(firestore)
    private val repository: BranchRepository = BranchRepositoryImpl(dataSource)

    private val createBranchUseCase = CreateBranchUseCase(repository)
    private val updateBranchUseCase = UpdateBranchUseCase(repository)
    private val deleteBranchUseCase = DeleteBranchUseCase(repository)
    private val getBranchesByBusinessUseCase = GetBranchesByBusinessUseCase(repository)
    private val getBranchByIdUseCase = GetBranchByIdUseCase(repository)

    private val _createBranchState = MutableStateFlow<Result<Branch>?>(null)
    val createBranchState: StateFlow<Result<Branch>?> = _createBranchState.asStateFlow()

    private val _updateBranchState = MutableStateFlow<Result<Branch>?>(null)
    val updateBranchState: StateFlow<Result<Branch>?> = _updateBranchState.asStateFlow()

    private val _deleteBranchState = MutableStateFlow<Result<Unit>?>(null)
    val deleteBranchState: StateFlow<Result<Unit>?> = _deleteBranchState.asStateFlow()

    private val _branchListState = MutableStateFlow<Result<List<Branch>>?>(null)
    val branchListState: StateFlow<Result<List<Branch>>?> = _branchListState.asStateFlow()

    private val _branchState = MutableStateFlow<Result<Branch>?>(null)
    val branchState: StateFlow<Result<Branch>?> = _branchState.asStateFlow()

    fun createBranch(businessId: String, name: String, address: String, phone: String) {
        viewModelScope.launch {
            _createBranchState.value = Result.Loading
            _createBranchState.value = createBranchUseCase(businessId, name, address, phone)
        }
    }

    fun updateBranch(
        branchId: String,
        businessId: String,
        name: String,
        address: String,
        phone: String,
        createdAt: Long
    ) {
        viewModelScope.launch {
            _updateBranchState.value = Result.Loading
            _updateBranchState.value = updateBranchUseCase(branchId, businessId, name, address, phone, createdAt)
        }
    }

    fun deleteBranch(businessId: String, branchId: String) {
        viewModelScope.launch {
            _deleteBranchState.value = Result.Loading
            _deleteBranchState.value = deleteBranchUseCase(businessId, branchId)
        }
    }

    fun getBranchesByBusiness(businessId: String) {
        viewModelScope.launch {
            _branchListState.value = Result.Loading
            _branchListState.value = getBranchesByBusinessUseCase(businessId)
        }
    }

    fun getBranchById(businessId: String, branchId: String) {
        viewModelScope.launch {
            _branchState.value = Result.Loading
            _branchState.value = getBranchByIdUseCase(businessId, branchId)
        }
    }

    fun resetCreateState() { _createBranchState.value = null }
    fun resetUpdateState() { _updateBranchState.value = null }
    fun resetDeleteState() { _deleteBranchState.value = null }
    fun resetListState() { _branchListState.value = null }
    fun resetBranchState() { _branchState.value = null }
}
