package com.caas.app.ui.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.result.Result
import com.caas.app.data.model.PurchaseOrder
import com.caas.app.data.model.PurchaseOrderItem
import com.caas.app.data.model.PurchaseOrderStatus
import com.caas.app.data.repository.PurchaseOrderRepositoryImpl
import com.caas.app.data.repository.ProviderRepositoryImpl
import com.caas.app.data.repository.StockRepositoryImpl
import com.caas.app.data.source.FirestoreProviderDataSource
import com.caas.app.data.source.FirestorePurchaseOrderDataSource
import com.caas.app.data.source.FirestoreStockDataSource
import com.caas.app.domain.usecase.AutoGeneratePurchaseOrderUseCase
import com.caas.app.domain.usecase.CreatePurchaseOrderUseCase
import com.caas.app.domain.usecase.GetPurchaseOrdersUseCase
import com.caas.app.domain.usecase.ReceivePurchaseOrderUseCase
import com.caas.app.domain.usecase.UpdatePurchaseOrderStatusUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PurchaseOrderViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // Dependencias instanciadas manualmente
    private val purchaseOrderRepository = PurchaseOrderRepositoryImpl(
        FirestorePurchaseOrderDataSource(firestore)
    )
    private val stockRepository = StockRepositoryImpl(
        FirestoreStockDataSource(firestore)
    )
    private val providerRepository = ProviderRepositoryImpl(
        FirestoreProviderDataSource(firestore)
    )

    private val createOrderUseCase = CreatePurchaseOrderUseCase(purchaseOrderRepository)
    private val autoGenerateUseCase = AutoGeneratePurchaseOrderUseCase(
        stockRepository, providerRepository, purchaseOrderRepository
    )
    private val updateStatusUseCase = UpdatePurchaseOrderStatusUseCase(purchaseOrderRepository)
    private val receiveOrderUseCase = ReceivePurchaseOrderUseCase(purchaseOrderRepository, stockRepository)
    private val getOrdersUseCase = GetPurchaseOrdersUseCase(purchaseOrderRepository)

    private val _createOrderState = MutableStateFlow<Result<PurchaseOrder>?>(null)
    val createOrderState: StateFlow<Result<PurchaseOrder>?> = _createOrderState.asStateFlow()

    private val _autoGenerateState = MutableStateFlow<Result<List<PurchaseOrder>>?>(null)
    val autoGenerateState: StateFlow<Result<List<PurchaseOrder>>?> = _autoGenerateState.asStateFlow()

    private val _updateStatusState = MutableStateFlow<Result<PurchaseOrder>?>(null)
    val updateStatusState: StateFlow<Result<PurchaseOrder>?> = _updateStatusState.asStateFlow()

    private val _orderListState = MutableStateFlow<Result<List<PurchaseOrder>>?>(null)
    val orderListState: StateFlow<Result<List<PurchaseOrder>>?> = _orderListState.asStateFlow()

    private val _orderDetailState = MutableStateFlow<Result<PurchaseOrder>?>(null)
    val orderDetailState: StateFlow<Result<PurchaseOrder>?> = _orderDetailState.asStateFlow()

    fun createOrder(
        businessId: String,
        branchId: String,
        providerId: String,
        providerName: String,
        items: List<PurchaseOrderItem>,
        notes: String = "",
        createdBy: String = ""
    ) {
        viewModelScope.launch {
            _createOrderState.value = Result.Loading
            _createOrderState.value = createOrderUseCase(
                businessId, branchId, providerId, providerName, items, notes, createdBy
            )
        }
    }

    fun autoGenerateOrders(businessId: String, branchId: String) {
        viewModelScope.launch {
            _autoGenerateState.value = Result.Loading
            _autoGenerateState.value = autoGenerateUseCase(businessId, branchId)
        }
    }

    fun updateOrderStatus(businessId: String, orderId: String, status: PurchaseOrderStatus) {
        viewModelScope.launch {
            _updateStatusState.value = Result.Loading
            val result = updateStatusUseCase(businessId, orderId, status)
            _updateStatusState.value = when (result) {
                is Result.Success -> {
                    val orderResult = purchaseOrderRepository.getPurchaseOrderById(businessId, orderId)
                    if (orderResult is Result.Success && orderResult.data != null) {
                        Result.Success(orderResult.data)
                    } else {
                        Result.Error("No se pudo recargar la orden")
                    }
                }
                is Result.Error -> Result.Error(result.message, result.throwable)
                Result.Loading -> Result.Loading
            }
        }
    }

    fun receiveOrder(businessId: String, orderId: String, receivedBy: String = "") {
        viewModelScope.launch {
            _updateStatusState.value = Result.Loading
            _updateStatusState.value = receiveOrderUseCase(businessId, orderId, receivedBy)
        }
    }

    fun loadOrders(
        businessId: String,
        status: PurchaseOrderStatus? = null,
        providerId: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ) {
        viewModelScope.launch {
            _orderListState.value = Result.Loading
            _orderListState.value = getOrdersUseCase(businessId, status, providerId, startDate, endDate)
        }
    }

    fun loadOrderDetail(businessId: String, orderId: String) {
        viewModelScope.launch {
            _orderDetailState.value = Result.Loading
            val result = purchaseOrderRepository.getPurchaseOrderById(businessId, orderId)
            _orderDetailState.value = when (result) {
                is Result.Success -> if (result.data != null) {
                    Result.Success(result.data)
                } else {
                    Result.Error("Orden no encontrada")
                }
                is Result.Error -> Result.Error(result.message, result.throwable)
                Result.Loading -> Result.Loading
            }
        }
    }

    fun resetCreateState() { _createOrderState.value = null }
    fun resetUpdateState() { _updateStatusState.value = null }
    fun resetAutoGenerateState() { _autoGenerateState.value = null }
}
