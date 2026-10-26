package com.example.mobidesk.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Represents the role of a user in the MobiDesk system.
 * [STUDENT] can view assigned VMs, start/stop them, and connect via VNC/WebRTC.
 * [ADMIN] can manage all VMs, assign VMs to users, create/delete VMs, and view system stats.
 */
@Serializable(with = UserRoleSerializer::class)
enum class UserRole(val key: String, val label: String) {
    STUDENT("student", "Student"),
    ADMIN("admin", "Administrator");

    val isAdmin: Boolean get() = this == ADMIN
    val isStudent: Boolean get() = this == STUDENT

    companion object {
        fun fromString(role: String?): UserRole {
            return when (role?.lowercase()?.trim()) {
                "admin", "administrator" -> ADMIN
                "student", "user" -> STUDENT
                else -> STUDENT
            }
        }
    }
}

object UserRoleSerializer : KSerializer<UserRole> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UserRole", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UserRole) {
        encoder.encodeString(value.key)
    }

    override fun deserialize(decoder: Decoder): UserRole {
        return UserRole.fromString(decoder.decodeString())
    }
}
