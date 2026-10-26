package com.example.mobidesk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model representing Virtual Machine details from the KVM bridge / database.
 * Matches specs: id, name, vcpu, ram_gb, storage_gb, lan_ip, status.
 */
@Serializable
data class VmDetails(
    val id: String,
    val name: String,
    val vcpu: Int = 2,
    @SerialName("ram_gb")
    val ramGb: Int = 4,
    @SerialName("storage_gb")
    val storageGb: Int = 50,
    @SerialName("lan_ip")
    val lanIp: String? = null,
    val status: VmStatus = VmStatus.STOPPED,
    @SerialName("os_type")
    val osType: String? = "Ubuntu 22.04 LTS",
    @SerialName("mac_address")
    val macAddress: String? = null,
    @SerialName("node_id")
    val nodeId: String? = null,
    @SerialName("vnc_port")
    val vncPort: Int? = null,
    @SerialName("webrtc_url")
    val webRtcUrl: String? = null,
    val description: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
