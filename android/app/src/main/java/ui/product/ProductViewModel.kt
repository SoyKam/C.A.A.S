package com.caas.app.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.caas.app.data.repository.ProductRepositoryImpl
import com.caas.app.data.source.FirestoreProductDataSource
import com.caas.app.domain.repository.ProductRepository
import com.caas.app.domain.usecase.CreateProductUseCase
import com.caas.app.domain.usecase.DeleteProductUseCase
import com.caas.app.domain.usecase.GetProductByIdUseCase
import com.caas.app.domain.usecase.GetProductsByCategoryUseCase
import com.caas.app.domain.usecase.GetProductsByBusinessUseCase
import com.caas.app.domain.usecase.SearchProductsUseCase
import com.caas.app.domain.usecase.UpdateProductUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val dataSource = FirestoreProductDataSource(firestore)
    private val repository: ProductRepository = ProductRepositoryImpl(dataSource)

    private val createProductUseCase = CreateProductUseCase(repository)
    private val updateProductUseCase = UpdateProductUseCase(repository)
    private val deleteProductUseCase = DeleteProductUseCase(repository)
    private val getProductsByBusinessUseCase = GetProductsByBusinessUseCase(repository)
    private val getProductByIdUseCase = GetProductByIdUseCase(repository)
    private val searchProductsUseCase = SearchProductsUseCase(repository)
    private val getProductsByCategoryUseCase = GetProductsByCategoryUseCase(repository)

    private val _createProductState = MutableStateFlow<Result<Product>?>(null)
    val createProductState: StateFlow<Result<Product>?> = _createProductState.asStateFlow()

    private val _updateProductState = MutableStateFlow<Result<Product>?>(null)
    val updateProductState: StateFlow<Result<Product>?> = _updateProductState.asStateFlow()

    private val _deleteProductState = MutableStateFlow<Result<Unit>?>(null)
    val deleteProductState: StateFlow<Result<Unit>?> = _deleteProductState.asStateFlow()

    private val _productListState = MutableStateFlow<Result<List<Product>>?>(null)
    val productListState: StateFlow<Result<List<Product>>?> = _productListState.asStateFlow()

    private val _productState = MutableStateFlow<Result<Product?>?>(null)
    val productState: StateFlow<Result<Product?>?> = _productState.asStateFlow()

    private val _searchState = MutableStateFlow<Result<List<Product>>?>(null)
    val searchState: StateFlow<Result<List<Product>>?> = _searchState.asStateFlow()

    private val _categoryFilterState = MutableStateFlow<Result<List<Product>>?>(null)
    val categoryFilterState: StateFlow<Result<List<Product>>?> = _categoryFilterState.asStateFlow()

    fun createProduct(
        businessId: String,
        name: String,
        sku: String,
        category: String,
        costPrice: Double,
        salePrice: Double,
        imageUrl: String
    ) {
        viewModelScope.launch {
            _createProductState.value = Result.Loading
            _createProductState.value = createProductUseCase(
                businessId, name, sku, category, costPrice, salePrice, imageUrl
            )
        }
    }

    fun updateProduct(
        businessId: String,
        productId: String,
        name: String,
        sku: String,
        category: String,
        costPrice: Double,
        salePrice: Double,
        imageUrl: String
    ) {
        viewModelScope.launch {
            _updateProductState.value = Result.Loading
            _updateProductState.value = updateProductUseCase(
                businessId, productId, name, sku, category, costPrice, salePrice, imageUrl
            )
        }
    }

    fun deleteProduct(businessId: String, productId: String) {
        viewModelScope.launch {
            _deleteProductState.value = Result.Loading
            _deleteProductState.value = deleteProductUseCase(businessId, productId)
        }
    }

    fun getProductsByBusiness(businessId: String) {
        viewModelScope.launch {
            _productListState.value = Result.Loading
            _productListState.value = getProductsByBusinessUseCase(businessId)
        }
    }

    fun getProductById(businessId: String, productId: String) {
        viewModelScope.launch {
            _productState.value = Result.Loading
            _productState.value = getProductByIdUseCase(businessId, productId)
        }
    }

    fun searchProducts(businessId: String, query: String) {
        viewModelScope.launch {
            _searchState.value = Result.Loading
            _searchState.value = searchProductsUseCase(businessId, query)
        }
    }

    fun getProductsByCategory(businessId: String, category: String) {
        viewModelScope.launch {
            _categoryFilterState.value = Result.Loading
            _categoryFilterState.value = getProductsByCategoryUseCase(businessId, category)
        }
    }

    fun resetCreateState() { _createProductState.value = null }
    fun resetUpdateState() { _updateProductState.value = null }
    fun resetDeleteState() { _deleteProductState.value = null }
    fun resetProductListState() { _productListState.value = null }
    fun resetProductState() { _productState.value = null }
    fun resetSearchState() { _searchState.value = null }
    fun resetCategoryFilterState() { _categoryFilterState.value = null }
}
