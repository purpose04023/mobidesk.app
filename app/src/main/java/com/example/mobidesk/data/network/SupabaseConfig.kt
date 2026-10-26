package com.example.mobidesk.data.network

/**
 * Supabase configuration credentials and constants.
 * In production, these can be set dynamically or read from BuildConfig / environment.
 */
object SupabaseConfig {
    var supabaseUrl: String = "https://your-supabase-project.supabase.co"
    var supabaseAnonKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlvdXItcHJvamVjdCIsInJvbGUiOiJhbW9uIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE5MDAwMDAwMDB9.placeholderKey"

    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() &&
                !supabaseUrl.contains("your-supabase-project") &&
                supabaseAnonKey.isNotBlank() &&
                !supabaseAnonKey.contains("placeholderKey")

    fun updateConfig(url: String, anonKey: String) {
        if (url.isNotBlank()) supabaseUrl = url
        if (anonKey.isNotBlank()) supabaseAnonKey = anonKey
    }
}
