package com.example.mobidesk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model representing assignment of a Virtual Machine to a user.
 */
@Serializable
data class VmAssignment(
    val id: String,
    @SerialName("vm_id")
    val vmId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("assigned_at")
    val assignedAt: String? = null,
    @SerialName("expires_at")
    val expiresAt: String? = null,
    @SerialName("vm_details")
    val vmDetails: VmDetails? = null,
    @SerialName("user_profile")
    val userProfile: UserProfile? = null
)
