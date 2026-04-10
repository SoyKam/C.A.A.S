package com.caas.app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.result.Result
import com.caas.app.data.repository.ProductRepositoryImpl
import com.caas.app.data.repository.StockRepositoryImpl
import com.caas.app.data.source.FirestoreProductDataSource
import com.caas.app.data.source.FirestoreStockDataSource
import com.caas.app.domain.model.InventorySummaryItem
import com.caas.app.domain.usecase.GetInventorySummaryByBranchUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val stockRepository = StockRepositoryImpl(FirestoreStockDataSource(firestore))
    private val productRepository = ProductRepositoryImpl(FirestoreProductDataSource(firestore))

    private val getInventorySummaryByBranchUseCase = GetInventorySummaryByBranchUseCase(
        stockRepository, productRepository
    )

    private val _summaryState = MutableStateFlow<Result<List<InventorySummaryItem>>?>(null)
    val summaryState: StateFlow<Result<List<InventorySummaryItem>>?> = _summaryState.asStateFlow()

    fun getInventorySummary(businessId: String, branchId: String) {
        viewModelScope.launch {
            _summaryState.value = Result.Loading
            _summaryState.value = getInventorySummaryByBranchUseCase(businessId, branchId)
        }
    }

    fun resetSummaryState() {
        _summaryState.value = null
    }
}
