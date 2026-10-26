package com.example.mobidesk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for performing VM action (start, stop, restart, pause, resume).
 */
@Serializable
data class VmActionRequest(
    val action: String,
    val force: Boolean = false
)

/**
 * Request body for creating a new Virtual Machine on the KVM Hypervisor.
 */
@Serializable
data class VmCreateRequest(
    val name: String,
    val vcpu: Int = 2,
    @SerialName("ram_gb")
    val ramGb: Int = 4,
    @SerialName("storage_gb")
    val storageGb: Int = 50,
    @SerialName("os_type")
    val osType: String = "Ubuntu 22.04 LTS",
    @SerialName("assigned_user_id")
    val assignedUserId: String? = null
)

/**
 * Generic API Response wrapper from Node.js Express KVM bridge.
 */
@Serializable
data class KvmApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)

/**
 * Node.js Hypervisor bridge system performance metrics.
 */
@Serializable
data class SystemStats(
    @SerialName("cpu_usage_percent")
    val cpuUsagePercent: Double = 0.0,
    @SerialName("ram_used_gb")
    val ramUsedGb: Double = 0.0,
    @SerialName("ram_total_gb")
    val ramTotalGb: Double = 16.0,
    @SerialName("storage_used_gb")
    val storageUsedGb: Double = 0.0,
    @SerialName("storage_total_gb")
    val storageTotalGb: Double = 500.0,
    @SerialName("active_vms")
    val activeVmsCount: Int = 0,
    @SerialName("total_vms")
    val totalVmsCount: Int = 0
)
