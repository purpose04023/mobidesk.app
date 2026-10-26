package com.example.mobidesk.data.repository

import com.example.mobidesk.data.model.VmDetails
import com.example.mobidesk.data.model.VmStatus
import com.example.mobidesk.data.network.KvmBridgeClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repository handling VM queries and power lifecycle operations.
 */
interface VmRepository {
    val studentVmsFlow: StateFlow<List<VmDetails>>

    suspend fun getStudentVms(userId: String): Result<List<VmDetails>>
    suspend fun executeVmAction(vmId: String, action: String): Result<VmDetails>
}

class VmRepositoryImpl(
    private val kvmClient: KvmBridgeClient = KvmBridgeClient.getInstance()
) : VmRepository {

    // Initial student Cloud PCs (win-01 through win-05) matching 2 vCPU | 2 GB RAM | 40 GB Storage specs
    private val defaultStudentVms = listOf(
        VmDetails(
            id = "win-01",
            name = "win-01",
            vcpu = 2,
            ramGb = 2,
            storageGb = 40,
            lanIp = "192.168.122.101",
            status = VmStatus.RUNNING,
            osType = "Windows 11 Enterprise",
            description = "Student Workstation 01 - Cloud PC",
            vncPort = 5901,
            webRtcUrl = "http://10.0.2.2:3000/vnc/win-01"
        ),
        VmDetails(
            id = "win-02",
            name = "win-02",
            vcpu = 2,
            ramGb = 2,
            storageGb = 40,
            lanIp = "192.168.122.102",
            status = VmStatus.STOPPED,
            osType = "Windows 11 Enterprise",
            description = "Student Workstation 02 - Cloud PC",
            vncPort = 5902,
            webRtcUrl = "http://10.0.2.2:3000/vnc/win-02"
        ),
        VmDetails(
            id = "win-03",
            name = "win-03",
            vcpu = 2,
            ramGb = 2,
            storageGb = 40,
            lanIp = "192.168.122.103",
            status = VmStatus.STOPPED,
            osType = "Windows 11 Enterprise",
            description = "Student Workstation 03 - Cloud PC",
            vncPort = 5903,
            webRtcUrl = "http://10.0.2.2:3000/vnc/win-03"
        ),
        VmDetails(
            id = "win-04",
            name = "win-04",
            vcpu = 2,
            ramGb = 2,
            storageGb = 40,
            lanIp = "192.168.122.104",
            status = VmStatus.STOPPED,
            osType = "Windows 11 Enterprise",
            description = "Student Workstation 04 - Cloud PC",
            vncPort = 5904,
            webRtcUrl = "http://10.0.2.2:3000/vnc/win-04"
        ),
        VmDetails(
            id = "win-05",
            name = "win-05",
            vcpu = 2,
            ramGb = 2,
            storageGb = 40,
            lanIp = "192.168.122.105",
            status = VmStatus.STOPPED,
            osType = "Windows 11 Enterprise",
            description = "Student Workstation 05 - Cloud PC",
            vncPort = 5905,
            webRtcUrl = "http://10.0.2.2:3000/vnc/win-05"
        )
    )

    private val _studentVmsFlow = MutableStateFlow(defaultStudentVms)
    override val studentVmsFlow: StateFlow<List<VmDetails>> = _studentVmsFlow.asStateFlow()

    override suspend fun getStudentVms(userId: String): Result<List<VmDetails>> {
        return try {
            val networkResult = kvmClient.getVms()
            if (networkResult.isSuccess) {
                val fetchedVms = networkResult.getOrDefault(emptyList())
                if (fetchedVms.isNotEmpty()) {
                    val mappedVms = fetchedVms.map { vm ->
                        vm.copy(
                            vcpu = 2,
                            ramGb = 2,
                            storageGb = 40
                        )
                    }
                    _studentVmsFlow.value = mappedVms
                    return Result.success(mappedVms)
                }
            }
            Result.success(_studentVmsFlow.value)
        } catch (_: Exception) {
            Result.success(_studentVmsFlow.value)
        }
    }

    override suspend fun executeVmAction(vmId: String, action: String): Result<VmDetails> {
        val targetAction = action.lowercase().trim()
        val targetVm = _studentVmsFlow.value.find { it.id == vmId || it.name == vmId }
            ?: return Result.failure(IllegalArgumentException("VM with ID $vmId not found"))

        val networkResult = try {
            kvmClient.executeVmAction(targetVm.id, targetAction)
        } catch (e: Exception) {
            Result.failure(e)
        }

        if (networkResult.isSuccess) {
            val updatedVm = networkResult.getOrThrow()
            updateLocalVmState(updatedVm)
            return Result.success(updatedVm)
        }

        val newStatus = when (targetAction) {
            "start" -> VmStatus.RUNNING
            "stop" -> VmStatus.STOPPED
            "restart" -> VmStatus.RUNNING
            "pause" -> VmStatus.PAUSED
            "resume" -> VmStatus.RUNNING
            else -> targetVm.status
        }

        val updatedVm = targetVm.copy(
            status = newStatus,
            lanIp = if (newStatus == VmStatus.RUNNING) {
                targetVm.lanIp ?: "192.168.122.${101 + _studentVmsFlow.value.indexOf(targetVm)}"
            } else {
                targetVm.lanIp
            }
        )

        updateLocalVmState(updatedVm)
        return Result.success(updatedVm)
    }

    private fun updateLocalVmState(updatedVm: VmDetails) {
        _studentVmsFlow.update { currentList ->
            currentList.map { vm ->
                if (vm.id == updatedVm.id || vm.name == updatedVm.name) {
                    updatedVm
                } else {
                    vm
                }
            }
        }
    }
}
