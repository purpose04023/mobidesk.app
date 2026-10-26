package com.example.mobidesk.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation 3 destination screens representing routes in MobiDesk application.
 */
sealed interface Screen {
    @Serializable
    data object Login : Screen

    @Serializable
    data object StudentDashboard : Screen

    @Serializable
    data object AdminDashboard : Screen
}
