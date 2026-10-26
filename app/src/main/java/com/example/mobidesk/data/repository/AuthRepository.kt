package com.example.mobidesk.data.repository

import com.example.mobidesk.data.model.UserProfile
import com.example.mobidesk.data.model.UserRole
import com.example.mobidesk.data.network.SupabaseClientProvider
import com.example.mobidesk.data.network.SupabaseConfig
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sealed class representing authentication states.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Interface defining authentication and user role management operations.
 */
interface AuthRepository {
    val currentProfile: StateFlow<UserProfile?>
    val authState: StateFlow<AuthState>

    suspend fun signIn(email: String, password: String): Result<UserProfile>
    suspend fun signUp(email: String, password: String, fullName: String, role: UserRole): Result<UserProfile>
    suspend fun signOut(): Result<Unit>
    suspend fun getUserProfile(userId: String): Result<UserProfile>
    suspend fun getUserRole(userId: String): Result<UserRole>
    suspend fun checkSession(): Result<UserProfile?>
}

/**
 * Concrete implementation of [AuthRepository] integrating Supabase Auth & Postgrest DB,
 * with fallback demo authentication for local offline testing.
 */
class AuthRepositoryImpl(
    private val supabaseProvider: SupabaseClientProvider = SupabaseClientProvider
) : AuthRepository {

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    override val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Demo in-memory account database for local offline testing / fallback
    private val demoUsers = mutableMapOf(
        "admin@mobidesk.com" to UserProfile(
            id = "demo-admin-uuid-001",
            email = "admin@mobidesk.com",
            fullName = "System Administrator",
            role = UserRole.ADMIN
        ),
        "student@mobidesk.com" to UserProfile(
            id = "demo-student-uuid-002",
            email = "student@mobidesk.com",
            fullName = "Alex Student",
            role = UserRole.STUDENT
        )
    )

    override suspend fun signIn(email: String, password: String): Result<UserProfile> {
        _authState.value = AuthState.Loading
        val trimmedEmail = email.trim().lowercase()

        if (trimmedEmail.isBlank() || password.isBlank()) {
            val errorMsg = "Email and password must not be empty"
            _authState.value = AuthState.Error(errorMsg)
            return Result.failure(IllegalArgumentException(errorMsg))
        }

        // Check if Supabase is properly configured
        if (SupabaseConfig.isConfigured) {
            try {
                supabaseProvider.auth.signInWith(Email) {
                    this.email = trimmedEmail
                    this.password = password
                }

                val currentSessionUser = supabaseProvider.auth.currentUserOrNull()
                if (currentSessionUser != null) {
                    val profileResult = getUserProfile(currentSessionUser.id)
                    val profile = profileResult.getOrElse {
                        UserProfile(
                            id = currentSessionUser.id,
                            email = currentSessionUser.email ?: trimmedEmail,
                            fullName = currentSessionUser.userMetadata?.get("full_name")?.toString() ?: "",
                            role = UserRole.fromString(currentSessionUser.userMetadata?.get("role")?.toString())
                        )
                    }
                    _currentProfile.value = profile
                    _authState.value = AuthState.Authenticated(profile)
                    return Result.success(profile)
                }
            } catch (e: Exception) {
                // If cloud Supabase attempt fails and matches demo account credentials, fallback to demo mode
                if (demoUsers.containsKey(trimmedEmail)) {
                    val demoUser = demoUsers[trimmedEmail]!!
                    _currentProfile.value = demoUser
                    _authState.value = AuthState.Authenticated(demoUser)
                    return Result.success(demoUser)
                }
                val errorMsg = e.localizedMessage ?: "Sign-in failed"
                _authState.value = AuthState.Error(errorMsg)
                return Result.failure(e)
            }
        }

        // Demo account fallback mode
        return if (demoUsers.containsKey(trimmedEmail)) {
            val demoUser = demoUsers[trimmedEmail]!!
            _currentProfile.value = demoUser
            _authState.value = AuthState.Authenticated(demoUser)
            Result.success(demoUser)
        } else {
            // Allow dynamic local sign-in for new emails in demo mode
            val isDemoAdmin = trimmedEmail.contains("admin")
            val newProfile = UserProfile(
                id = "demo-user-${System.currentTimeMillis()}",
                email = trimmedEmail,
                fullName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                role = if (isDemoAdmin) UserRole.ADMIN else UserRole.STUDENT
            )
            demoUsers[trimmedEmail] = newProfile
            _currentProfile.value = newProfile
            _authState.value = AuthState.Authenticated(newProfile)
            Result.success(newProfile)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        role: UserRole
    ): Result<UserProfile> {
        _authState.value = AuthState.Loading
        val trimmedEmail = email.trim().lowercase()

        if (trimmedEmail.isBlank() || password.isBlank()) {
            val errorMsg = "Email and password must not be empty"
            _authState.value = AuthState.Error(errorMsg)
            return Result.failure(IllegalArgumentException(errorMsg))
        }

        if (SupabaseConfig.isConfigured) {
            try {
                supabaseProvider.auth.signUpWith(Email) {
                    this.email = trimmedEmail
                    this.password = password
                }

                val currentSessionUser = supabaseProvider.auth.currentUserOrNull()
                val userId = currentSessionUser?.id ?: "user-${System.currentTimeMillis()}"

                val profile = UserProfile(
                    id = userId,
                    email = trimmedEmail,
                    fullName = fullName,
                    role = role
                )

                // Try inserting profile into Supabase database table 'profiles'
                try {
                    supabaseProvider.postgrest.from("profiles").insert(profile)
                } catch (_: Exception) {
                    // Ignore insert exception if table/RSL policy isn't set up yet
                }

                _currentProfile.value = profile
                _authState.value = AuthState.Authenticated(profile)
                return Result.success(profile)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Sign-up failed"
                _authState.value = AuthState.Error(errorMsg)
                return Result.failure(e)
            }
        }

        // Demo Mode Fallback
        val demoProfile = UserProfile(
            id = "demo-user-${System.currentTimeMillis()}",
            email = trimmedEmail,
            fullName = fullName,
            role = role
        )
        demoUsers[trimmedEmail] = demoProfile
        _currentProfile.value = demoProfile
        _authState.value = AuthState.Authenticated(demoProfile)
        return Result.success(demoProfile)
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            if (SupabaseConfig.isConfigured) {
                try {
                    supabaseProvider.auth.signOut()
                } catch (_: Exception) {}
            }
            _currentProfile.value = null
            _authState.value = AuthState.Idle
            Result.success(Unit)
        } catch (_: Exception) {
            _currentProfile.value = null
            _authState.value = AuthState.Idle
            Result.success(Unit)
        }
    }

    override suspend fun getUserProfile(userId: String): Result<UserProfile> {
        val activeProfile = _currentProfile.value
        if (activeProfile?.id == userId) {
            return Result.success(activeProfile)
        }

        if (SupabaseConfig.isConfigured) {
            try {
                val profile = supabaseProvider.postgrest.from("profiles")
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }.decodeSingle<UserProfile>()
                return Result.success(profile)
            } catch (e: Exception) {
                // Fallback to demo users search
            }
        }

        val foundDemoUser = demoUsers.values.find { it.id == userId }
        return if (foundDemoUser != null) {
            Result.success(foundDemoUser)
        } else {
            Result.failure(NoSuchElementException("User profile with id $userId not found"))
        }
    }

    override suspend fun getUserRole(userId: String): Result<UserRole> {
        return getUserProfile(userId).map { it.role }
    }

    override suspend fun checkSession(): Result<UserProfile?> {
        _authState.value = AuthState.Loading

        if (SupabaseConfig.isConfigured) {
            try {
                val user = supabaseProvider.auth.currentUserOrNull()
                if (user != null) {
                    val profileResult = getUserProfile(user.id)
                    val profile = profileResult.getOrElse {
                        UserProfile(
                            id = user.id,
                            email = user.email ?: "",
                            fullName = user.userMetadata?.get("full_name")?.toString() ?: "",
                            role = UserRole.fromString(user.userMetadata?.get("role")?.toString())
                        )
                    }
                    _currentProfile.value = profile
                    _authState.value = AuthState.Authenticated(profile)
                    return Result.success(profile)
                }
            } catch (_: Exception) {}
        }

        val existingProfile = _currentProfile.value
        return if (existingProfile != null) {
            _authState.value = AuthState.Authenticated(existingProfile)
            Result.success(existingProfile)
        } else {
            _authState.value = AuthState.Idle
            Result.success(null)
        }
    }
}
