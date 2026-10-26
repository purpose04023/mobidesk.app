package com.example.mobidesk.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobidesk.data.model.UserProfile
import com.example.mobidesk.data.model.UserRole
import com.example.mobidesk.data.repository.AuthRepository
import com.example.mobidesk.data.repository.AuthRepositoryImpl
import com.example.mobidesk.data.repository.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Authentication screens (Login & Sign Up).
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: UserProfile? = null,
    val role: UserRole? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val emailInput: String = "",
    val passwordInput: String = "",
    val fullNameInput: String = "",
    val selectedRole: UserRole = UserRole.STUDENT,
    val isSignUpMode: Boolean = false
)

/**
 * ViewModel managing Authentication logic, user session state, and role retrieval.
 */
class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
        checkSession()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is AuthState.Authenticated -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                user = state.user,
                                role = state.user.role,
                                errorMessage = null
                            )
                        }
                    }
                    is AuthState.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = state.message
                            )
                        }
                    }
                    is AuthState.Idle -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                user = null,
                                role = null
                            )
                        }
                    }
                }
            }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(emailInput = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(passwordInput = password, errorMessage = null) }
    }

    fun onFullNameChanged(name: String) {
        _uiState.update { it.copy(fullNameInput = name, errorMessage = null) }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun toggleSignUpMode() {
        _uiState.update {
            it.copy(
                isSignUpMode = !it.isSignUpMode,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun signIn() {
        val currentState = _uiState.value
        val email = currentState.emailInput
        val password = currentState.passwordInput

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password") }
            return
        }

        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Sign-in failed")
                }
            }
        }
    }

    fun signUp() {
        val currentState = _uiState.value
        val email = currentState.emailInput
        val password = currentState.passwordInput
        val fullName = currentState.fullNameInput
        val role = currentState.selectedRole

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password") }
            return
        }

        viewModelScope.launch {
            val result = authRepository.signUp(email, password, fullName, role)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(successMessage = "Account created successfully!")
                }
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Sign-up failed")
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update {
                AuthUiState(
                    emailInput = "",
                    passwordInput = "",
                    fullNameInput = ""
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun checkSession() {
        viewModelScope.launch {
            authRepository.checkSession()
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
