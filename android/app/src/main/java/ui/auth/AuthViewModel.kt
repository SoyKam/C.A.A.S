package com.caas.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caas.app.core.utils.Resource
import com.caas.app.core.utils.ValidationUtils
import com.caas.app.data.repository.AuthRepositoryImpl
import com.caas.app.data.source.FirebaseAuthSource
import com.caas.app.domain.model.User
import com.caas.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel que maneja la lógica de autenticación.
 * Valida inputs y coordina entre UI y repositorio.
 */
class AuthViewModel : ViewModel() {

    // Instanciación manual del repositorio (sin DI)
    private val repository: AuthRepository = AuthRepositoryImpl(FirebaseAuthSource())

    // Estado de login
    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState.asStateFlow()

    // Estado de registro
    private val _registerState = MutableStateFlow<Resource<User>?>(null)
    val registerState: StateFlow<Resource<User>?> = _registerState.asStateFlow()

    /**
     * Inicia sesión con email y contraseña.
     * Valida inputs antes de llamar al repositorio.
     */
    fun login(email: String, password: String) {
        // Validación de email
        if (!ValidationUtils.isValidEmail(email)) {
            _loginState.value = Resource.Error("El formato del email no es válido")
            return
        }

        // Validación de contraseña
        if (!ValidationUtils.isValidPassword(password)) {
            _loginState.value = Resource.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        // Ejecutar login en coroutine
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            val result = repository.login(email, password)
            _loginState.value = result
        }
    }

    /**
     * Registra un nuevo usuario.
     * Valida inputs antes de llamar al repositorio.
     */
    fun register(email: String, password: String, name: String) {
        // Validación de nombre
        if (!ValidationUtils.isValidName(name)) {
            _registerState.value = Resource.Error("El nombre no puede estar vacío")
            return
        }

        // Validación de email
        if (!ValidationUtils.isValidEmail(email)) {
            _registerState.value = Resource.Error("El formato del email no es válido")
            return
        }

        // Validación de contraseña
        if (!ValidationUtils.isValidPassword(password)) {
            _registerState.value = Resource.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        // Ejecutar registro en coroutine
        viewModelScope.launch {
            _registerState.value = Resource.Loading
            val result = repository.register(email, password, name)
            _registerState.value = result
        }
    }

    /**
     * Resetea el estado de login para evitar navegaciones repetidas.
     */
    fun resetLoginState() {
        _loginState.value = null
    }

    /**
     * Resetea el estado de registro para evitar navegaciones repetidas.
     */
    fun resetRegisterState() {
        _registerState.value = null
    }
}