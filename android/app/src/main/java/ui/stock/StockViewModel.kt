package com.caas.app.ui.stock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.constants.FirestoreCollections
import com.caas.app.core.result.Result
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.Product
import com.caas.app.data.model.Stock
import com.caas.app.data.model.StockAlert
import com.caas.app.data.model.StockMovement
import com.caas.app.data.repository.BranchRepositoryImpl
import com.caas.app.data.repository.StockRepositoryImpl
import com.caas.app.data.source.FirestoreBranchDataSource
import com.caas.app.data.source.FirestoreStockDataSource
import com.caas.app.domain.repository.BranchRepository
import com.caas.app.domain.repository.StockRepository
import com.caas.app.domain.usecase.CheckAndSaveStockAlertsUseCase
import com.caas.app.domain.usecase.GetAllLowStockByBusinessUseCase
import com.caas.app.domain.usecase.GetLowStockUseCase
import com.caas.app.domain.usecase.GetMovementsByBranchUseCase
import com.caas.app.domain.usecase.GetMovementsByDateRangeUseCase
import com.caas.app.domain.usecase.GetMovementsByProductUseCase
import com.caas.app.domain.usecase.GetMovementsByTypeUseCase
import com.caas.app.domain.usecase.GetStockByBranchUseCase
import com.caas.app.domain.usecase.GetUnreadAlertsUseCase
import com.caas.app.domain.usecase.MarkAlertAsReadUseCase
import com.caas.app.domain.usecase.RegisterStockEntryUseCase
import com.caas.app.domain.usecase.RegisterStockExitUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StockViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dataSource = FirestoreStockDataSource(firestore)
    private val repository: StockRepository = StockRepositoryImpl(dataSource)

    private val branchDataSource = FirestoreBranchDataSource(firestore)
    private val branchRepository: BranchRepository = BranchRepositoryImpl(branchDataSource)

    private val registerStockEntryUseCase = RegisterStockEntryUseCase(repository)
    private val registerStockExitUseCase = RegisterStockExitUseCase(repository)
    private val getStockByBranchUseCase = GetStockByBranchUseCase(repository)
    private val getLowStockUseCase = GetLowStockUseCase(repository)
    private val getMovementsByBranchUseCase = GetMovementsByBranchUseCase(repository)
    private val getMovementsByTypeUseCase = GetMovementsByTypeUseCase(repository)
    private val getMovementsByDateRangeUseCase = GetMovementsByDateRangeUseCase(repository)
    private val getMovementsByProductUseCase = GetMovementsByProductUseCase(repository)
    private val getAllLowStockByBusinessUseCase = GetAllLowStockByBusinessUseCase(repository)
    private val getUnreadAlertsUseCase = GetUnreadAlertsUseCase(repository)
    private val markAlertAsReadUseCase = MarkAlertAsReadUseCase(repository)
    private val checkAndSaveStockAlertsUseCase = CheckAndSaveStockAlertsUseCase(
        repository, branchRepository, application.applicationContext
    )

    // ── Estados existentes ──────────────────────────────────────────────────

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

    // ── Movimientos recientes para pantalla Reports ─────────────────────────

    private val _recentMovementsState = MutableStateFlow<Result<List<StockMovement>>?>(null)
    val recentMovementsState: StateFlow<Result<List<StockMovement>>?> = _recentMovementsState.asStateFlow()

    fun loadRecentMovements(businessIds: List<String>) {
        if (businessIds.isEmpty()) {
            _recentMovementsState.value = Result.Success(emptyList())
            return
        }
        viewModelScope.launch {
            _recentMovementsState.value = Result.Loading
            _recentMovementsState.value = try {
                val all = mutableListOf<StockMovement>()
                for (businessId in businessIds) {
                    val branchResult = branchRepository.getBranchesByBusinessId(businessId)
                    if (branchResult is Result.Success) {
                        for (branch in branchResult.data) {
                            try {
                                val movements = dataSource.getMovementsByBranch(businessId, branch.id)
                                    .map { m ->
                                        val branchTag = "Suc: ${branch.name}"
                                        m.copy(reason = if (m.reason.isBlank()) branchTag else "$branchTag · ${m.reason}")
                                    }
                                all.addAll(movements)
                            } catch (_: Exception) {}
                        }
                    }
                }
                Result.Success(all.sortedByDescending { it.createdAt }.take(10))
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error al cargar movimientos recientes", e)
            }
        }
    }

    fun resetRecentMovementsState() { _recentMovementsState.value = null }

    // ── Movimiento seleccionado para detalle ────────────────────────────────

    private val _selectedMovement = MutableStateFlow<com.caas.app.data.model.StockMovement?>(null)
    val selectedMovement: StateFlow<com.caas.app.data.model.StockMovement?> = _selectedMovement.asStateFlow()

    fun selectMovement(movement: com.caas.app.data.model.StockMovement) {
        _selectedMovement.value = movement
    }

    // ── Estados movimientos filtrados ───────────────────────────────────────

    private val _movementsByTypeState = MutableStateFlow<Result<List<StockMovement>>?>(null)
    val movementsByTypeState: StateFlow<Result<List<StockMovement>>?> = _movementsByTypeState.asStateFlow()

    private val _movementsByDateState = MutableStateFlow<Result<List<StockMovement>>?>(null)
    val movementsByDateState: StateFlow<Result<List<StockMovement>>?> = _movementsByDateState.asStateFlow()

    private val _movementsByProductState = MutableStateFlow<Result<List<StockMovement>>?>(null)
    val movementsByProductState: StateFlow<Result<List<StockMovement>>?> = _movementsByProductState.asStateFlow()

    // ── Estados nuevos RF-22 / RF-23 ────────────────────────────────────────

    private val _allLowStockState = MutableStateFlow<Result<List<Stock>>?>(null)
    val allLowStockState: StateFlow<Result<List<Stock>>?> = _allLowStockState.asStateFlow()

    private val _unreadAlertsState = MutableStateFlow<Result<List<StockAlert>>?>(null)
    val unreadAlertsState: StateFlow<Result<List<StockAlert>>?> = _unreadAlertsState.asStateFlow()

    private val _markReadState = MutableStateFlow<Result<Unit>?>(null)
    val markReadState: StateFlow<Result<Unit>?> = _markReadState.asStateFlow()

    private val _checkAlertsState = MutableStateFlow<Result<Unit>?>(null)
    val checkAlertsState: StateFlow<Result<Unit>?> = _checkAlertsState.asStateFlow()

    // ── Métodos existentes ──────────────────────────────────────────────────

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

    // ── Métodos movimientos filtrados ───────────────────────────────────────

    fun getMovementsByType(businessId: String, branchId: String, type: MovementType) {
        viewModelScope.launch {
            _movementsByTypeState.value = Result.Loading
            _movementsByTypeState.value = getMovementsByTypeUseCase(businessId, branchId, type)
        }
    }

    fun getMovementsByDateRange(businessId: String, branchId: String, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _movementsByDateState.value = Result.Loading
            _movementsByDateState.value = getMovementsByDateRangeUseCase(businessId, branchId, startDate, endDate)
        }
    }

    fun getMovementsByProduct(businessId: String, branchId: String, productId: String) {
        viewModelScope.launch {
            _movementsByProductState.value = Result.Loading
            _movementsByProductState.value = getMovementsByProductUseCase(businessId, branchId, productId)
        }
    }

    // ── Métodos nuevos RF-22 / RF-23 ────────────────────────────────────────

    fun getAllLowStockByBusiness(businessId: String) {
        viewModelScope.launch {
            _allLowStockState.value = Result.Loading
            _allLowStockState.value = getAllLowStockByBusinessUseCase(businessId)
        }
    }

    fun getUnreadAlerts(businessId: String) {
        viewModelScope.launch {
            _unreadAlertsState.value = Result.Loading
            _unreadAlertsState.value = getUnreadAlertsUseCase(businessId)
        }
    }

    fun markAlertAsRead(businessId: String, alertId: String) {
        viewModelScope.launch {
            _markReadState.value = Result.Loading
            _markReadState.value = markAlertAsReadUseCase(businessId, alertId)
        }
    }

    fun markAllAlertsAsRead(businessId: String, alertIds: List<String>) {
        viewModelScope.launch {
            _markReadState.value = Result.Loading
            for (alertId in alertIds) {
                markAlertAsReadUseCase(businessId, alertId)
            }
            _markReadState.value = Result.Success(Unit)
        }
    }

    fun checkAndSaveAlerts(businessId: String) {
        viewModelScope.launch {
            _checkAlertsState.value = Result.Loading
            _checkAlertsState.value = checkAndSaveStockAlertsUseCase(businessId)
        }
    }

    // ── Resets ──────────────────────────────────────────────────────────────

    fun resetEntryState() { _stockEntryState.value = null }
    fun resetExitState() { _stockExitState.value = null }
    fun resetListState() { _stockListState.value = null }
    fun resetLowStockState() { _lowStockState.value = null }
    fun resetMovementsState() { _movementsState.value = null }
    fun resetAllLowStockState() { _allLowStockState.value = null }
    fun resetUnreadAlertsState() { _unreadAlertsState.value = null }
    fun resetMarkReadState() { _markReadState.value = null }
    fun resetCheckAlertsState() { _checkAlertsState.value = null }
    fun resetMovementsByTypeState() { _movementsByTypeState.value = null }
    fun resetMovementsByDateState() { _movementsByDateState.value = null }
    fun resetMovementsByProductState() { _movementsByProductState.value = null }
}
