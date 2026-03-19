package com.caas.app.ui.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.data.repository.BusinessRepositoryImpl
import com.caas.app.data.source.FirestoreBusinessDataSource
import com.caas.app.domain.repository.BusinessRepository
import com.caas.app.domain.usecase.CreateBusinessUseCase
import com.caas.app.domain.usecase.UpdateBusinessUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel que maneja la lógica de creación y edición de negocio.
 * Coordina entre UI y repositorio de Business.
 */
class BusinessViewModel : ViewModel() {

    // Instanciación manual del repositorio (sin DI)
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val dataSource = FirestoreBusinessDataSource(firestore)
    private val repository: BusinessRepository = BusinessRepositoryImpl(dataSource)

    private val createBusinessUseCase = CreateBusinessUseCase(repository, firebaseAuth)
    private val updateBusinessUseCase = UpdateBusinessUseCase(repository)

    // Estado de creación de negocio - Cambio de Resource a Result
    private val _createBusinessState = MutableStateFlow<Result<Business>?>(null)
    val createBusinessState: StateFlow<Result<Business>?> = _createBusinessState.asStateFlow()

    // Estado de actualización de negocio - Cambio de Resource a Result
    private val _updateBusinessState = MutableStateFlow<Result<Business>?>(null)
    val updateBusinessState: StateFlow<Result<Business>?> = _updateBusinessState.asStateFlow()

    /**
     * Crea un nuevo negocio.
     * Valida inputs antes de llamar al UseCase.
     */
    fun createBusiness(name: String, sector: String, taxId: String) {
        // Validación rápida
        if (name.isBlank() || sector.isBlank() || taxId.isBlank()) {
            _createBusinessState.value = Result.Error("Todos los campos son requeridos")
            return
        }

        viewModelScope.launch {
            _createBusinessState.value = Result.Loading as? Result<Business>
            val result = createBusinessUseCase(name.trim(), sector.trim(), taxId.trim())
            _createBusinessState.value = when (result) {
                is Result.Success -> Result.Success(result.data)
                is Result.Error -> Result.Error(result.message)
                is Result.Loading -> Result.Loading as? Result<Business>
            }
        }
    }

    /**
     * Actualiza la información de un negocio existente.
     * Valida inputs antes de llamar al UseCase.
     */
    fun updateBusiness(businessId: String, name: String, sector: String, taxId: String) {
        // Validación rápida
        if (businessId.isBlank() || name.isBlank() || sector.isBlank() || taxId.isBlank()) {
            _updateBusinessState.value = Result.Error("Todos los campos son requeridos")
            return
        }

        viewModelScope.launch {
            _updateBusinessState.value = Result.Loading as? Result<Business>
            val result = updateBusinessUseCase(businessId, name.trim(), sector.trim(), taxId.trim())
            _updateBusinessState.value = when (result) {
                is Result.Success -> Result.Success(result.data)
                is Result.Error -> Result.Error(result.message)
                is Result.Loading -> Result.Loading as? Result<Business>
            }
        }
    }

    /**
     * Resetea el estado de creación para evitar navegaciones repetidas.
     */
    fun resetCreateState() {
        _createBusinessState.value = null
    }

    /**
     * Resetea el estado de actualización para evitar navegaciones repetidas.
     */
    fun resetUpdateState() {
        _updateBusinessState.value = null
    }
}