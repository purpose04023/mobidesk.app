package com.example.mobidesk.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobidesk.data.model.VmDetails
import com.example.mobidesk.data.repository.VmRepository
import com.example.mobidesk.data.repository.VmRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Student Dashboard screen.
 */
data class StudentDashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val vms: List<VmDetails> = emptyList(),
    val activeVm: VmDetails? = null,
    val actionInProgressVmId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for Student Dashboard managing assigned Cloud PCs (win-01 to win-05) and VM lifecycle actions.
 */
class StudentDashboardViewModel(
    private val vmRepository: VmRepository = VmRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentDashboardUiState())
    val uiState: StateFlow<StudentDashboardUiState> = _uiState.asStateFlow()

    init {
        observeStudentVms()
    }

    private fun observeStudentVms() {
        viewModelScope.launch {
            vmRepository.studentVmsFlow.collect { vmList ->
                _uiState.update { current ->
                    val primaryVm = vmList.firstOrNull { it.name == "win-01" } ?: vmList.firstOrNull()
                    current.copy(
                        vms = vmList,
                        activeVm = primaryVm,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadVms(userId: String = "student-user-001") {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            val result = vmRepository.getStudentVms(userId)
            _uiState.update { current ->
                if (result.isSuccess) {
                    current.copy(isRefreshing = false)
                } else {
                    current.copy(
                        isRefreshing = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to load Cloud PCs"
                    )
                }
            }
        }
    }

    fun executeVmLifecycleAction(vmId: String, action: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgressVmId = vmId, errorMessage = null) }

            val result = vmRepository.executeVmAction(vmId, action)

            _uiState.update { current ->
                if (result.isSuccess) {
                    current.copy(
                        actionInProgressVmId = null,
                        successMessage = "Action '$action' executed for VM $vmId"
                    )
                } else {
                    current.copy(
                        actionInProgressVmId = null,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Action execution failed"
                    )
                }
            }
        }
    }

    fun selectActiveVm(vm: VmDetails) {
        _uiState.update { it.copy(activeVm = vm) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    class Factory(private val vmRepository: VmRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentDashboardViewModel(vmRepository) as T
        }
    }
}
