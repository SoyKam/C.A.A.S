package com.caas.app.ui.reports

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.data.repository.BranchRepositoryImpl
import com.caas.app.data.repository.ProductRepositoryImpl
import com.caas.app.data.repository.StockRepositoryImpl
import com.caas.app.data.source.FirestoreBranchDataSource
import com.caas.app.data.source.FirestoreProductDataSource
import com.caas.app.data.source.FirestoreStockDataSource
import com.caas.app.domain.repository.BranchRepository
import com.caas.app.domain.repository.ProductRepository
import com.caas.app.domain.repository.StockRepository
import com.caas.app.domain.usecase.ExportInventoryReportUseCase
import com.caas.app.domain.usecase.ExportMovementsUseCase
import com.caas.app.domain.usecase.GetBranchesByBusinessUseCase
import com.caas.app.domain.usecase.GetInventorySummaryByBranchUseCase
import com.caas.app.domain.usecase.GetMovementsByDateRangeUseCase
import com.caas.app.domain.usecase.ShareFileUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class ExportType { PDF, EXCEL }

class ReportsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val branchRepository: BranchRepository =
        BranchRepositoryImpl(FirestoreBranchDataSource(firestore))
    private val stockRepository: StockRepository =
        StockRepositoryImpl(FirestoreStockDataSource(firestore))
    private val productRepository: ProductRepository =
        ProductRepositoryImpl(FirestoreProductDataSource(firestore))

    private val getBranchesUseCase = GetBranchesByBusinessUseCase(branchRepository)
    private val getInventorySummaryUseCase = GetInventorySummaryByBranchUseCase(stockRepository, productRepository)
    private val getMovementsByDateRangeUseCase = GetMovementsByDateRangeUseCase(stockRepository)
    private val exportPdfUseCase = ExportInventoryReportUseCase()
    private val exportExcelUseCase = ExportMovementsUseCase()
    private val shareFileUseCase = ShareFileUseCase()

    private val _branchesState = MutableStateFlow<Result<List<Branch>>?>(null)
    val branchesState: StateFlow<Result<List<Branch>>?> = _branchesState.asStateFlow()

    private val _exportPdfState = MutableStateFlow<Result<File>?>(null)
    val exportPdfState: StateFlow<Result<File>?> = _exportPdfState.asStateFlow()

    private val _exportExcelState = MutableStateFlow<Result<File>?>(null)
    val exportExcelState: StateFlow<Result<File>?> = _exportExcelState.asStateFlow()

    fun loadBranches(businessId: String) {
        viewModelScope.launch {
            _branchesState.value = Result.Loading
            _branchesState.value = withContext(Dispatchers.IO) { getBranchesUseCase(businessId) }
        }
    }

    fun exportInventoryPdf(
        context: Context,
        businessId: String,
        businessName: String,
        branchId: String,
        branchName: String
    ) {
        viewModelScope.launch {
            _exportPdfState.value = Result.Loading
            _exportPdfState.value = withContext(Dispatchers.IO) {
                when (val summaryResult = getInventorySummaryUseCase(businessId, branchId)) {
                    is Result.Success -> exportPdfUseCase(context, businessName, branchName, summaryResult.data)
                    is Result.Error -> summaryResult
                    Result.Loading -> Result.Error("Estado inesperado")
                }
            }
        }
    }

    fun exportMovementsExcel(
        context: Context,
        businessId: String,
        businessName: String,
        branchId: String,
        branchName: String,
        startDate: Long,
        endDate: Long
    ) {
        viewModelScope.launch {
            _exportExcelState.value = Result.Loading
            _exportExcelState.value = withContext(Dispatchers.IO) {
                when (val movResult = getMovementsByDateRangeUseCase(businessId, branchId, startDate, endDate)) {
                    is Result.Success -> exportExcelUseCase(context, businessName, branchName, movResult.data, startDate, endDate)
                    is Result.Error -> movResult
                    Result.Loading -> Result.Error("Estado inesperado")
                }
            }
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String): Intent =
        shareFileUseCase.invoke(context, file, mimeType)

    fun resetPdfState() { _exportPdfState.value = null }
    fun resetExcelState() { _exportExcelState.value = null }
    fun resetBranchesState() { _branchesState.value = null }
}
