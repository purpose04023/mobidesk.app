package com.example.mobidesk.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Status of a Virtual Machine in the KVM Hypervisor bridge.
 */
@Serializable(with = VmStatusSerializer::class)
enum class VmStatus(val key: String, val label: String) {
    RUNNING("running", "Running"),
    STOPPED("stopped", "Stopped"),
    PAUSED("paused", "Paused"),
    SUSPENDED("suspended", "Suspended"),
    STARTING("starting", "Starting..."),
    STOPPING("stopping", "Stopping..."),
    ERROR("error", "Error"),
    UNKNOWN("unknown", "Unknown");

    val isOnline: Boolean get() = this == RUNNING
    val isOffline: Boolean get() = this == STOPPED
    val isTransient: Boolean get() = this == STARTING || this == STOPPING

    val canStart: Boolean get() = this == STOPPED || this == ERROR
    val canStop: Boolean get() = this == RUNNING || this == PAUSED
    val canPause: Boolean get() = this == RUNNING
    val canResume: Boolean get() = this == PAUSED || this == SUSPENDED

    companion object {
        fun fromString(status: String?): VmStatus {
            return when (status?.lowercase()?.trim()) {
                "running", "active", "online" -> RUNNING
                "stopped", "shut off", "offline" -> STOPPED
                "paused" -> PAUSED
                "suspended" -> SUSPENDED
                "starting", "booting" -> STARTING
                "stopping", "shutting_down" -> STOPPING
                "error", "failed" -> ERROR
                else -> UNKNOWN
            }
        }
    }
}

object VmStatusSerializer : KSerializer<VmStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("VmStatus", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: VmStatus) {
        encoder.encodeString(value.key)
    }

    override fun deserialize(decoder: Decoder): VmStatus {
        return VmStatus.fromString(decoder.decodeString())
    }
}
