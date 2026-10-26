package com.example.mobidesk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model representing a User Profile in MobiDesk.
 * Stores information retrieved from Supabase Auth & 'profiles' database table.
 */
@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    @SerialName("full_name")
    val fullName: String = "",
    val role: UserRole = UserRole.STUDENT,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    val isAdmin: Boolean get() = role.isAdmin
    val isStudent: Boolean get() = role.isStudent

    val displayName: String
        get() = fullName.ifBlank { email.substringBefore("@") }
}
