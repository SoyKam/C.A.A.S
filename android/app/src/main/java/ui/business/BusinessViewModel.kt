package com.caas.app.ui.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.data.repository.BusinessRepositoryImpl
import com.caas.app.data.source.FirestoreBusinessDataSource
import com.caas.app.domain.repository.BusinessRepository
import com.caas.app.domain.usecase.CreateBusinessUseCase
import com.caas.app.domain.usecase.UpdateBusinessUseCase
import com.caas.app.domain.usecase.GetBusinessesByOwnerUseCase
import com.caas.app.domain.usecase.GetBusinessByIdUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BusinessViewModel : ViewModel() {

    // Instanciación manual del repositorio
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val dataSource = FirestoreBusinessDataSource(firestore)
    private val repository: BusinessRepository = BusinessRepositoryImpl(dataSource)

    private val createBusinessUseCase = CreateBusinessUseCase(repository, firebaseAuth)
    private val updateBusinessUseCase = UpdateBusinessUseCase(repository)
    private val getBusinessesByOwnerUseCase = GetBusinessesByOwnerUseCase(repository, firebaseAuth)
    private val getBusinessByIdUseCase = GetBusinessByIdUseCase(repository)

    // Estados existentes
    private val _createBusinessState = MutableStateFlow<Result<Business>?>(null)
    val createBusinessState: StateFlow<Result<Business>?> = _createBusinessState.asStateFlow()

    private val _updateBusinessState = MutableStateFlow<Result<Business>?>(null)
    val updateBusinessState: StateFlow<Result<Business>?> = _updateBusinessState.asStateFlow()

    // Estados para listar y obtener negocios
    private val _businessListState = MutableStateFlow<Result<List<Business>>?>(null)
    val businessListState: StateFlow<Result<List<Business>>?> = _businessListState.asStateFlow()

    private val _businessState = MutableStateFlow<Result<Business?>?>(null)
    val businessState: StateFlow<Result<Business?>?> = _businessState.asStateFlow()

    /**
     * Crea un nuevo negocio.
     */
    fun createBusiness(name: String, sector: String, taxId: String) {
        viewModelScope.launch {
            _createBusinessState.value = Result.Loading
            val result = createBusinessUseCase(name, sector, taxId)
            _createBusinessState.value = result
        }
    }

    /**
     * Actualiza la información de un negocio.
     */
    fun updateBusiness(businessId: String, name: String, sector: String, taxId: String) {
        viewModelScope.launch {
            _updateBusinessState.value = Result.Loading
            val result = updateBusinessUseCase(businessId, name, sector, taxId)
            _updateBusinessState.value = result
        }
    }

    /**
     * Obtiene la lista de negocios del usuario autenticado.
     */
    fun getBusinessesByOwner() {
        viewModelScope.launch {
            _businessListState.value = Result.Loading
            val result = getBusinessesByOwnerUseCase()
            _businessListState.value = result
        }
    }

    /**
     * Obtiene el detalle de un negocio por su ID.
     */
    fun getBusiness(businessId: String) {
        viewModelScope.launch {
            _businessState.value = Result.Loading
            val result = getBusinessByIdUseCase(businessId)
            _businessState.value = result
        }
    }

    fun resetCreateState() {
        _createBusinessState.value = null
    }

    fun resetUpdateState() {
        _updateBusinessState.value = null
    }

    fun resetListState() {
        _businessListState.value = null
    }

    fun resetBusinessState() {
        _businessState.value = null
    }
}