package com.caas.app.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.constants.FirestoreCollections
import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _productListState = MutableStateFlow<Result<List<Product>>?>(null)
    val productListState: StateFlow<Result<List<Product>>?> = _productListState.asStateFlow()

    private val _searchState = MutableStateFlow<Result<List<Product>>?>(null)
    val searchState: StateFlow<Result<List<Product>>?> = _searchState.asStateFlow()

    private val _categoryFilterState = MutableStateFlow<Result<List<Product>>?>(null)
    val categoryFilterState: StateFlow<Result<List<Product>>?> = _categoryFilterState.asStateFlow()

    private val _productState = MutableStateFlow<Result<Product?>?>(null)
    val productState: StateFlow<Result<Product?>?> = _productState.asStateFlow()

    private val _createProductState = MutableStateFlow<Result<Product>?>(null)
    val createProductState: StateFlow<Result<Product>?> = _createProductState.asStateFlow()

    private val _updateProductState = MutableStateFlow<Result<Product>?>(null)
    val updateProductState: StateFlow<Result<Product>?> = _updateProductState.asStateFlow()

    private val _deleteProductState = MutableStateFlow<Result<Boolean>?>(null)
    val deleteProductState: StateFlow<Result<Boolean>?> = _deleteProductState.asStateFlow()

    fun getProductsByBusiness(businessId: String) {
        viewModelScope.launch {
            _productListState.value = Result.Loading
            _productListState.value = runCatching {
                productsCollection(businessId)
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

    fun searchProducts(businessId: String, query: String) {
        viewModelScope.launch {
            val sanitized = query.trim()
            if (sanitized.isBlank()) {
                _searchState.value = null
                return@launch
            }

            _searchState.value = Result.Loading
            _searchState.value = runCatching {
                val base = fetchActiveProducts(businessId)
                base.filter {
                    it.name.contains(sanitized, ignoreCase = true) ||
                        it.sku.contains(sanitized, ignoreCase = true) ||
                        it.category.contains(sanitized, ignoreCase = true)
                }
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudo buscar productos", it) }
            )
        }
    }

    fun getProductsByCategory(businessId: String, category: String) {
        viewModelScope.launch {
            _categoryFilterState.value = Result.Loading
            _categoryFilterState.value = runCatching {
                fetchActiveProducts(businessId).filter { it.category.equals(category, ignoreCase = true) }
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudo filtrar la categoria", it) }
            )
        }
    }

    fun getProductById(businessId: String, productId: String) {
        viewModelScope.launch {
            _productState.value = Result.Loading
            _productState.value = runCatching {
                productsCollection(businessId)
                    .document(productId)
                    .get()
                    .await()
                    .toObject(Product::class.java)
                    ?.copy(id = productId)
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudo obtener el producto", it) }
            )
        }
    }

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
            _createProductState.value = runCatching {
                val productRef = productsCollection(businessId).document()
                val now = System.currentTimeMillis()
                val product = Product(
                    id = productRef.id,
                    businessId = businessId,
                    name = name,
                    sku = sku,
                    category = category,
                    costPrice = costPrice,
                    salePrice = salePrice,
                    imageUrl = imageUrl,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )
                productRef.set(product).await()
                product
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudo crear el producto", it) }
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
            _updateProductState.value = runCatching {
                val existing = productsCollection(businessId).document(productId).get().await()
                    .toObject(Product::class.java)
                    ?.copy(id = productId)

                val now = System.currentTimeMillis()
                val product = Product(
                    id = productId,
                    businessId = businessId,
                    name = name,
                    sku = sku,
                    category = category,
                    costPrice = costPrice,
                    salePrice = salePrice,
                    imageUrl = imageUrl,
                    isActive = existing?.isActive ?: true,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
                productsCollection(businessId).document(productId).set(product).await()
                product
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudo actualizar el producto", it) }
            )
        }
    }

    fun deleteProduct(businessId: String, productId: String) {
        viewModelScope.launch {
            _deleteProductState.value = Result.Loading
            _deleteProductState.value = runCatching {
                productsCollection(businessId)
                    .document(productId)
                    .update(
                        mapOf(
                            "isActive" to false,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
                true
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudo eliminar el producto", it) }
            )
        }
    }

    fun resetCreateState() {
        _createProductState.value = null
    }

    fun resetUpdateState() {
        _updateProductState.value = null
    }

    fun resetDeleteState() {
        _deleteProductState.value = null
    }

    private suspend fun fetchActiveProducts(businessId: String): List<Product> {
        return productsCollection(businessId)
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Product::class.java)?.copy(id = it.id) }
    }

    private fun productsCollection(businessId: String) = firestore.collection(FirestoreCollections.BUSINESSES)
        .document(businessId)
        .collection(FirestoreCollections.SubCollections.PRODUCTS_SUB)
}

