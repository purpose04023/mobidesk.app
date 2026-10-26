package com.example.mobidesk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.mobidesk.data.model.UserRole
import com.example.mobidesk.ui.admin.AdminDashboardScreen
import com.example.mobidesk.ui.admin.AdminDashboardViewModel
import com.example.mobidesk.ui.auth.AuthViewModel
import com.example.mobidesk.ui.auth.LoginScreen
import com.example.mobidesk.ui.student.StudentDashboardScreen
import com.example.mobidesk.ui.student.StudentDashboardViewModel

/**
 * Navigation 3 host component routing between LoginScreen, StudentDashboardScreen, and AdminDashboardScreen.
 */
@Composable
fun MobiDeskNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    studentDashboardViewModel: StudentDashboardViewModel = viewModel(),
    adminDashboardViewModel: AdminDashboardViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val backStack = remember { mutableStateListOf<Any>(Screen.Login) }

    // Automatic role-based navigation based on auth state
    LaunchedEffect(authState.isAuthenticated, authState.role) {
        if (authState.isAuthenticated) {
            val targetScreen: Screen = when (authState.role) {
                UserRole.ADMIN -> Screen.AdminDashboard
                UserRole.STUDENT -> Screen.StudentDashboard
                else -> Screen.StudentDashboard
            }

            if (backStack.lastOrNull() != targetScreen) {
                backStack.clear()
                backStack.add(targetScreen)
            }
        } else {
            if (backStack.lastOrNull() != Screen.Login) {
                backStack.clear()
                backStack.add(Screen.Login)
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        }
    ) { key: Any ->
        NavEntry<Any>(key) {
            when (key) {
                is Screen.Login -> {
                    LoginScreen(authViewModel = authViewModel)
                }
                is Screen.StudentDashboard -> {
                    StudentDashboardScreen(
                        authViewModel = authViewModel,
                        studentDashboardViewModel = studentDashboardViewModel
                    )
                }
                is Screen.AdminDashboard -> {
                    AdminDashboardScreen(
                        authViewModel = authViewModel,
                        adminDashboardViewModel = adminDashboardViewModel
                    )
                }
                else -> {
                    LoginScreen(authViewModel = authViewModel)
                }
            }
        }
    }
}
