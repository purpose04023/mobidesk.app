package com.example.mobidesk.data.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime

/**
 * Singleton provider for SupabaseClient instance with Auth, Postgrest, and Realtime modules.
 */
object SupabaseClientProvider {
    @Volatile
    private var instance: SupabaseClient? = null

    val client: SupabaseClient
        get() {
            return instance ?: synchronized(this) {
                instance ?: createClient().also { instance = it }
            }
        }

    val auth get() = client.auth
    val postgrest get() = client.postgrest
    val realtime get() = client.realtime

    fun reconfigure(url: String, anonKey: String) {
        SupabaseConfig.updateConfig(url, anonKey)
        synchronized(this) {
            instance = createClient()
        }
    }

    private fun createClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = SupabaseConfig.supabaseUrl,
            supabaseKey = SupabaseConfig.supabaseAnonKey,
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
