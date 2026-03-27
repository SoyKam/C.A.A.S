package com.caas.app.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.constants.FirestoreCollections
import com.caas.app.core.result.Result
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.Product
import com.caas.app.data.model.Stock
import com.caas.app.data.model.StockMovement
import com.caas.app.data.repository.StockRepositoryImpl
import com.caas.app.data.source.FirestoreStockDataSource
import com.caas.app.domain.repository.StockRepository
import com.caas.app.domain.usecase.GetLowStockUseCase
import com.caas.app.domain.usecase.GetMovementsByBranchUseCase
import com.caas.app.domain.usecase.GetStockByBranchUseCase
import com.caas.app.domain.usecase.RegisterStockEntryUseCase
import com.caas.app.domain.usecase.RegisterStockExitUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StockViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dataSource = FirestoreStockDataSource(firestore)
    private val repository: StockRepository = StockRepositoryImpl(dataSource)

    private val registerStockEntryUseCase = RegisterStockEntryUseCase(repository)
    private val registerStockExitUseCase = RegisterStockExitUseCase(repository)
    private val getStockByBranchUseCase = GetStockByBranchUseCase(repository)
    private val getLowStockUseCase = GetLowStockUseCase(repository)
    private val getMovementsByBranchUseCase = GetMovementsByBranchUseCase(repository)

    private val _stockEntryState = MutableStateFlow<Result<Stock>?>(null)
    val stockEntryState: StateFlow<Result<Stock>?> = _stockEntryState.asStateFlow()

    private val _stockExitState = MutableStateFlow<Result<Stock>?>(null)
    val stockExitState: StateFlow<Result<Stock>?> = _stockExitState.asStateFlow()

    private val _stockListState = MutableStateFlow<Result<List<Stock>>?>(null)
    val stockListState: StateFlow<Result<List<Stock>>?> = _stockListState.asStateFlow()

    private val _lowStockState = MutableStateFlow<Result<List<Stock>>?>(null)
    val lowStockState: StateFlow<Result<List<Stock>>?> = _lowStockState.asStateFlow()

    private val _movementsState = MutableStateFlow<Result<List<StockMovement>>?>(null)
    val movementsState: StateFlow<Result<List<StockMovement>>?> = _movementsState.asStateFlow()

    private val _businessProductsState = MutableStateFlow<Result<List<Product>>?>(null)
    val businessProductsState: StateFlow<Result<List<Product>>?> = _businessProductsState.asStateFlow()

    fun registerEntry(
        businessId: String,
        branchId: String,
        productId: String,
        productName: String,
        quantity: Int,
        minStock: Int
    ) {
        val userId = auth.currentUser?.uid ?: ""
        viewModelScope.launch {
            _stockEntryState.value = Result.Loading
            _stockEntryState.value = registerStockEntryUseCase(
                businessId, branchId, productId, productName, quantity, minStock, userId
            )
        }
    }

    fun registerExit(
        businessId: String,
        branchId: String,
        productId: String,
        productName: String,
        quantity: Int,
        type: MovementType,
        reason: String
    ) {
        val userId = auth.currentUser?.uid ?: ""
        viewModelScope.launch {
            _stockExitState.value = Result.Loading
            _stockExitState.value = registerStockExitUseCase(
                businessId, branchId, productId, productName, quantity, type, reason, userId
            )
        }
    }

    fun getStockByBranch(businessId: String, branchId: String) {
        viewModelScope.launch {
            _stockListState.value = Result.Loading
            _stockListState.value = getStockByBranchUseCase(businessId, branchId)
        }
    }

    fun getLowStock(businessId: String, branchId: String) {
        viewModelScope.launch {
            _lowStockState.value = Result.Loading
            _lowStockState.value = getLowStockUseCase(businessId, branchId)
        }
    }

    fun getMovements(businessId: String, branchId: String) {
        viewModelScope.launch {
            _movementsState.value = Result.Loading
            _movementsState.value = getMovementsByBranchUseCase(businessId, branchId)
        }
    }

    fun getBusinessProducts(businessId: String) {
        viewModelScope.launch {
            _businessProductsState.value = Result.Loading
            _businessProductsState.value = runCatching {
                firestore.collection(FirestoreCollections.BUSINESSES)
                    .document(businessId)
                    .collection(FirestoreCollections.SubCollections.PRODUCTS_SUB)
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(Product::class.java)?.copy(id = it.id) }
                    .sortedBy { it.name.lowercase() }
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudieron cargar los productos", it) }
            )
        }
    }

    fun resetEntryState() { _stockEntryState.value = null }
    fun resetExitState() { _stockExitState.value = null }
    fun resetListState() { _stockListState.value = null }
    fun resetLowStockState() { _lowStockState.value = null }
    fun resetMovementsState() { _movementsState.value = null }
}
