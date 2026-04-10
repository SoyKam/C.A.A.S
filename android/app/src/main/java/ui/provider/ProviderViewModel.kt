package com.caas.app.ui.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.constants.FirestoreCollections
import com.caas.app.core.result.Result
import com.caas.app.data.model.Provider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProviderViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _providerListState = MutableStateFlow<Result<List<Provider>>?>(null)
    val providerListState: StateFlow<Result<List<Provider>>?> = _providerListState.asStateFlow()

    private val _providerState = MutableStateFlow<Result<Provider?>?>(null)
    val providerState: StateFlow<Result<Provider?>?> = _providerState.asStateFlow()

    private val _createProviderState = MutableStateFlow<Result<Provider>?>(null)
    val createProviderState: StateFlow<Result<Provider>?> = _createProviderState.asStateFlow()

    private val _updateProviderState = MutableStateFlow<Result<Provider>?>(null)
    val updateProviderState: StateFlow<Result<Provider>?> = _updateProviderState.asStateFlow()

    private val _deleteProviderState = MutableStateFlow<Result<Boolean>?>(null)
    val deleteProviderState: StateFlow<Result<Boolean>?> = _deleteProviderState.asStateFlow()

    private val _providersByProductState = MutableStateFlow<Result<List<Provider>>?>(null)
    val providersByProductState: StateFlow<Result<List<Provider>>?> = _providersByProductState.asStateFlow()

    private val _associateProductState = MutableStateFlow<Result<Boolean>?>(null)
    val associateProductState: StateFlow<Result<Boolean>?> = _associateProductState.asStateFlow()

    private val _removeProductState = MutableStateFlow<Result<Boolean>?>(null)
    val removeProductState: StateFlow<Result<Boolean>?> = _removeProductState.asStateFlow()

    fun getProvidersByBusiness(businessId: String) {
        viewModelScope.launch {
            _providerListState.value = Result.Loading
            _providerListState.value = runCatching {
                providersCollection(businessId)
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(Provider::class.java)?.copy(id = it.id) }
                    .sortedBy { it.name.lowercase() }
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudieron cargar los proveedores", it) }
            )
        }
    }

    fun getProviderById(businessId: String, providerId: String) {
        viewModelScope.launch {
            _providerState.value = Result.Loading
            _providerState.value = runCatching {
                providersCollection(businessId)
                    .document(providerId)
                    .get()
                    .await()
                    .toObject(Provider::class.java)
                    ?.copy(id = providerId)
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudo obtener el proveedor", it) }
            )
        }
    }

    fun createProvider(
        businessId: String,
        name: String,
        phone: String,
        email: String,
        productIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _createProviderState.value = Result.Loading

            if (name.isBlank()) {
                _createProviderState.value = Result.Error("El nombre del proveedor es requerido")
                return@launch
            }
            if (phone.isBlank()) {
                _createProviderState.value = Result.Error("El teléfono es requerido")
                return@launch
            }
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _createProviderState.value = Result.Error("El correo electrónico no es válido")
                return@launch
            }

            _createProviderState.value = runCatching {
                val now = System.currentTimeMillis()
                val providerId = "${businessId}_${System.nanoTime()}"
                val provider = Provider(
                    id = providerId,
                    businessId = businessId,
                    name = name,
                    phone = phone,
                    email = email,
                    productIds = productIds,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )
                providersCollection(businessId).document(providerId).set(provider).await()
                provider
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudo crear el proveedor", it) }
            )
        }
    }

    fun updateProvider(
        businessId: String,
        providerId: String,
        name: String,
        phone: String,
        email: String,
        productIds: List<String>
    ) {
        viewModelScope.launch {
            _updateProviderState.value = Result.Loading

            if (name.isBlank()) {
                _updateProviderState.value = Result.Error("El nombre del proveedor es requerido")
                return@launch
            }
            if (phone.isBlank()) {
                _updateProviderState.value = Result.Error("El teléfono es requerido")
                return@launch
            }
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _updateProviderState.value = Result.Error("El correo electrónico no es válido")
                return@launch
            }

            _updateProviderState.value = runCatching {
                val ref = providersCollection(businessId).document(providerId)
                val existing = ref.get().await().toObject(Provider::class.java)?.copy(id = providerId)
                val provider = Provider(
                    id = providerId,
                    businessId = businessId,
                    name = name,
                    phone = phone,
                    email = email,
                    productIds = productIds,
                    isActive = existing?.isActive ?: true,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                ref.set(provider).await()
                provider
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudo actualizar el proveedor", it) }
            )
        }
    }

    fun deleteProvider(businessId: String, providerId: String) {
        viewModelScope.launch {
            _deleteProviderState.value = Result.Loading
            _deleteProviderState.value = runCatching {
                providersCollection(businessId)
                    .document(providerId)
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
                onFailure = { Result.Error(it.message ?: "No se pudo eliminar el proveedor", it) }
            )
        }
    }

    fun getProvidersByProduct(businessId: String, productId: String) {
        viewModelScope.launch {
            _providersByProductState.value = Result.Loading
            _providersByProductState.value = runCatching {
                providersCollection(businessId)
                    .whereEqualTo("isActive", true)
                    .whereArrayContains("productIds", productId)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(Provider::class.java)?.copy(id = it.id) }
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudieron cargar los proveedores", it) }
            )
        }
    }

    fun associateProduct(businessId: String, providerId: String, productId: String) {
        viewModelScope.launch {
            _associateProductState.value = Result.Loading
            _associateProductState.value = runCatching {
                val ref = providersCollection(businessId).document(providerId)
                val existing = ref.get().await().toObject(Provider::class.java)
                if (existing?.productIds?.contains(productId) == true) {
                    throw IllegalStateException("El producto ya está asociado a este proveedor")
                }
                ref.update(
                    mapOf(
                        "productIds" to FieldValue.arrayUnion(productId),
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
                true
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudo asociar el producto", it) }
            )
        }
    }

    fun removeProduct(businessId: String, providerId: String, productId: String) {
        viewModelScope.launch {
            _removeProductState.value = Result.Loading
            _removeProductState.value = runCatching {
                providersCollection(businessId)
                    .document(providerId)
                    .update(
                        mapOf(
                            "productIds" to FieldValue.arrayRemove(productId),
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
                true
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it.message ?: "No se pudo desasociar el producto", it) }
            )
        }
    }

    fun resetCreateState() { _createProviderState.value = null }
    fun resetUpdateState() { _updateProviderState.value = null }
    fun resetDeleteState() { _deleteProviderState.value = null }
    fun resetAssociateState() { _associateProductState.value = null }
    fun resetRemoveProductState() { _removeProductState.value = null }

    private fun providersCollection(businessId: String) =
        firestore.collection(FirestoreCollections.BUSINESSES)
            .document(businessId)
            .collection("providers")
}
