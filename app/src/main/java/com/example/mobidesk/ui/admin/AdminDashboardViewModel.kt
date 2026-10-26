package com.example.mobidesk.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobidesk.data.model.VmDetails
import com.example.mobidesk.data.model.VmStatus
import com.example.mobidesk.data.repository.VmRepository
import com.example.mobidesk.data.repository.VmRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Data item representing VM along with assigned student details for Admin Dashboard.
 */
data class AdminVmItem(
    val vm: VmDetails,
    val assignedStudentEmail: String
)

/**
 * UI State for Admin Dashboard console.
 */
data class AdminDashboardUiState(
    val vms: List<AdminVmItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val actionInProgressVmId: String? = null,
    val errorMessage: String? = null,
    val totalVmsCount: Int = 0,
    val runningVmsCount: Int = 0,
    val stoppedVmsCount: Int = 0
)

/**
 * ViewModel managing Admin Dashboard VM pool monitoring, assigned student details, and host lifecycle actions.
 */
class AdminDashboardViewModel(
    private val vmRepository: VmRepository = VmRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    // Assigned student email mappings for win-01 through win-05
    private val studentAssignments = mapOf(
        "win-01" to "student1@mobidesk.edu",
        "win-02" to "student2@mobidesk.edu",
        "win-03" to "student3@mobidesk.edu",
        "win-04" to "student4@mobidesk.edu",
        "win-05" to "student5@mobidesk.edu"
    )

    init {
        observeVms()
        loadVms()
    }

    private fun observeVms() {
        viewModelScope.launch {
            vmRepository.studentVmsFlow.collect { vmList ->
                updateVmListState(vmList)
            }
        }
    }

    fun loadVms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = vmRepository.getStudentVms("admin")
            if (result.isSuccess) {
                val fetched = result.getOrNull() ?: emptyList()
                updateVmListState(fetched)
                _uiState.update { it.copy(isLoading = false) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to load VMs"
                    )
                }
            }
        }
    }

    fun refreshVms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            val result = vmRepository.getStudentVms("admin")
            if (result.isSuccess) {
                val fetched = result.getOrNull() ?: emptyList()
                updateVmListState(fetched)
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Refresh failed")
                }
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun executeVmLifecycleAction(vmId: String, action: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgressVmId = vmId, errorMessage = null) }
            val result = vmRepository.executeVmAction(vmId, action)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to execute $action on $vmId: ${result.exceptionOrNull()?.localizedMessage}"
                    )
                }
            }
            _uiState.update { it.copy(actionInProgressVmId = null) }
        }
    }

    private fun updateVmListState(vmList: List<VmDetails>) {
        val adminVmItems = vmList.map { vm ->
            AdminVmItem(
                vm = vm,
                assignedStudentEmail = studentAssignments[vm.id] ?: studentAssignments[vm.name] ?: "unassigned@mobidesk.edu"
            )
        }

        val runningCount = vmList.count { it.status == VmStatus.RUNNING }
        val stoppedCount = vmList.count { it.status == VmStatus.STOPPED }

        _uiState.update {
            it.copy(
                vms = adminVmItems,
                totalVmsCount = adminVmItems.size,
                runningVmsCount = runningCount,
                stoppedVmsCount = stoppedCount
            )
        }
    }
}
