package com.caas.app.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.result.Result
import com.caas.app.data.model.PriceHistory
import com.caas.app.data.repository.PriceHistoryRepositoryImpl
import com.caas.app.data.source.FirestorePriceHistoryDataSource
import com.caas.app.domain.usecase.GetPriceHistoryUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PriceHistoryViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val dataSource = FirestorePriceHistoryDataSource(firestore)
    private val repository = PriceHistoryRepositoryImpl(dataSource)
    private val getPriceHistoryUseCase = GetPriceHistoryUseCase(repository)

    private val _priceHistoryState = MutableStateFlow<Result<List<PriceHistory>>?>(null)
    val priceHistoryState: StateFlow<Result<List<PriceHistory>>?> = _priceHistoryState.asStateFlow()

    fun getPriceHistory(businessId: String, productId: String) {
        viewModelScope.launch {
            _priceHistoryState.value = Result.Loading
            _priceHistoryState.value = getPriceHistoryUseCase(businessId, productId)
        }
    }

    fun resetPriceHistoryState() {
        _priceHistoryState.value = null
    }
}
