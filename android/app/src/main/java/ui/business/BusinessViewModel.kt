package com.caas.app.ui.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.data.repository.BusinessRepositoryImpl
import com.caas.app.data.repository.BranchRepositoryImpl
import com.caas.app.data.repository.ProductRepositoryImpl
import com.caas.app.data.repository.StockRepositoryImpl
import com.caas.app.data.source.FirestoreBusinessDataSource
import com.caas.app.data.source.FirestoreBranchDataSource
import com.caas.app.data.source.FirestoreProductDataSource
import com.caas.app.data.source.FirestoreStockDataSource
import com.caas.app.domain.repository.BusinessRepository
import com.caas.app.domain.repository.BranchRepository
import com.caas.app.domain.repository.ProductRepository
import com.caas.app.domain.repository.StockRepository
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
    private val businessDataSource = FirestoreBusinessDataSource(firestore)
    private val businessRepository: BusinessRepository = BusinessRepositoryImpl(businessDataSource)

    private val branchDataSource = FirestoreBranchDataSource(firestore)
    private val branchRepository: BranchRepository = BranchRepositoryImpl(branchDataSource)

    private val productDataSource = FirestoreProductDataSource(firestore)
    private val productRepository: ProductRepository = ProductRepositoryImpl(productDataSource)

    private val stockDataSource = FirestoreStockDataSource(firestore)
    private val stockRepository: StockRepository = StockRepositoryImpl(stockDataSource)

    private val createBusinessUseCase = CreateBusinessUseCase(businessRepository, firebaseAuth)
    private val updateBusinessUseCase = UpdateBusinessUseCase(businessRepository)
    private val getBusinessesByOwnerUseCase = GetBusinessesByOwnerUseCase(businessRepository, firebaseAuth)
    private val getBusinessByIdUseCase = GetBusinessByIdUseCase(businessRepository)

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

    // Estados para conteos
    private val _branchCountState = MutableStateFlow<Int>(0)
    val branchCountState: StateFlow<Int> = _branchCountState.asStateFlow()

    private val _productCountState = MutableStateFlow<Int>(0)
    val productCountState: StateFlow<Int> = _productCountState.asStateFlow()

    private val _totalStockState = MutableStateFlow<Int>(0)
    val totalStockState: StateFlow<Int> = _totalStockState.asStateFlow()

    private val _alertCountState = MutableStateFlow<Int>(0)
    val alertCountState: StateFlow<Int> = _alertCountState.asStateFlow()

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

    /**
     * Obtiene el conteo de sucursales de un negocio.
     */
    fun getBranchCount(businessId: String) {
        viewModelScope.launch {
            when (val result = branchRepository.getBranchesByBusinessId(businessId)) {
                is Result.Success -> {
                    _branchCountState.value = result.data.size
                }
                else -> {
                    _branchCountState.value = 0
                }
            }
        }
    }

    /**
     * Obtiene el conteo de productos de un negocio.
     */
    fun getProductCount(businessId: String) {
        viewModelScope.launch {
            when (val result = productRepository.getProductsByBusiness(businessId)) {
                is Result.Success -> {
                    _productCountState.value = result.data.size
                }
                else -> {
                    _productCountState.value = 0
                }
            }
        }
    }

    /**
     * Obtiene el stock total de un negocio.
     */
    fun getTotalStock(businessId: String) {
        viewModelScope.launch {
            // Obtener todas las sucursales primero
            when (val branchResult = branchRepository.getBranchesByBusinessId(businessId)) {
                is Result.Success -> {
                    var totalStock = 0
                    for (branch in branchResult.data) {
                        when (val stockResult = stockRepository.getStockByBranch(businessId, branch.id)) {
                            is Result.Success -> {
                                totalStock += stockResult.data.sumOf { it.quantity }
                            }
                            else -> {}
                        }
                    }
                    _totalStockState.value = totalStock
                }
                else -> {
                    _totalStockState.value = 0
                }
            }
        }
    }

    /**
     * Obtiene el conteo de alertas de stock de un negocio.
     */
    fun getAlertCount(businessId: String) {
        viewModelScope.launch {
            when (val result = stockRepository.getUnreadAlerts(businessId)) {
                is Result.Success -> {
                    _alertCountState.value = result.data.size
                }
                else -> {
                    _alertCountState.value = 0
                }
            }
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