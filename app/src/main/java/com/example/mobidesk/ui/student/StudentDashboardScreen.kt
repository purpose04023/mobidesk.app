package com.example.mobidesk.ui.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.Lan
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobidesk.data.model.VmDetails
import com.example.mobidesk.data.model.VmStatus
import com.example.mobidesk.ui.auth.AuthViewModel
import com.example.mobidesk.ui.components.FluentButton
import com.example.mobidesk.ui.components.FluentSecondaryButton
import com.example.mobidesk.ui.components.MicaCard
import com.example.mobidesk.ui.components.SpecChip
import com.example.mobidesk.ui.components.StatusBadge
import com.example.mobidesk.util.RdpLauncher

@Composable
fun StudentDashboardScreen(
    authViewModel: AuthViewModel,
    studentDashboardViewModel: StudentDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.uiState.collectAsState()
    val dashboardState by studentDashboardViewModel.uiState.collectAsState()
    val user = authState.user
    val context = LocalContext.current

    LaunchedEffect(user?.id) {
        studentDashboardViewModel.loadVms(user?.id ?: "student-user-001")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fluent Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Computer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "MobiDesk",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = user?.displayName ?: "Student Portal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Student",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    IconButton(
                        onClick = { studentDashboardViewModel.loadVms(user?.id ?: "student-user-001") }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh status",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { authViewModel.signOut() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Adaptive Grid/List based on screen size (1 column on phones, 2 columns on tablets/foldables)
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTabletOrFoldable = maxWidth >= 600.dp
            val gridColumnCount = if (isTabletOrFoldable) 2 else 1

            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumnCount),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome Banner (spans all columns)
                item(span = { GridItemSpan(gridColumnCount) }) {
                    MicaCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 20.dp,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Welcome back, ${user?.displayName ?: "Student"}!",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your Windows 365 Cloud PCs are ready for connection.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector = Icons.Rounded.DesktopWindows,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Error Message Banner (spans all columns)
                item(span = { GridItemSpan(gridColumnCount) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AnimatedVisibility(
                            visible = dashboardState.errorMessage != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            dashboardState.errorMessage?.let { msg ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp)),
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = msg,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Header: Assigned Cloud PCs (spans all columns)
                item(span = { GridItemSpan(gridColumnCount) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Assigned Cloud PCs (${dashboardState.vms.size})",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (dashboardState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // List of VM Cards (win-01 through win-05)
                items(dashboardState.vms, key = { it.id }) { vm ->
                    CloudPcItemCard(
                        vm = vm,
                        isActionLoading = dashboardState.actionInProgressVmId == vm.id,
                        onStart = { studentDashboardViewModel.executeVmLifecycleAction(vm.id, "start") },
                        onStop = { studentDashboardViewModel.executeVmLifecycleAction(vm.id, "stop") },
                        onRestart = { studentDashboardViewModel.executeVmLifecycleAction(vm.id, "restart") },
                        onLaunchRdp = {
                            RdpLauncher.launchRdpSession(
                                context = context,
                                ipAddress = vm.lanIp ?: "172.16.33.137",
                                port = 3389,
                                username = user?.email
                            )
                        }
                    )
                }

                item(span = { GridItemSpan(gridColumnCount) }) {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Individual Cloud PC Card item for win-01 through win-05 displaying specs, LAN IP, status badge, lifecycle controls & native RDP launcher.
 */
@Composable
fun CloudPcItemCard(
    vm: VmDetails,
    isActionLoading: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onLaunchRdp: () -> Unit,
    modifier: Modifier = Modifier
) {
    MicaCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 18.dp
    ) {
        // Header Row: Icon, Name, OS Type & Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = if (vm.status.isOnline) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.DesktopWindows,
                            contentDescription = null,
                            tint = if (vm.status.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = vm.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = vm.osType ?: "Windows 11 Enterprise",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            StatusBadge(status = vm.status)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Specs Row (2 vCPU | 2 GB RAM | 40 GB Storage)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpecChip(
                icon = Icons.Rounded.Speed,
                label = "vCPU",
                value = "${vm.vcpu}"
            )
            SpecChip(
                icon = Icons.Rounded.Memory,
                label = "RAM",
                value = "${vm.ramGb} GB"
            )
            SpecChip(
                icon = Icons.Rounded.Storage,
                label = "Storage",
                value = "${vm.storageGb} GB"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // LAN IP Address display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lan,
                contentDescription = "LAN IP",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "LAN IP: ",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
                text = if (vm.status.isOnline) (vm.lanIp ?: "172.16.33.137") else "Offline (Stopped)",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (vm.status.isOnline) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lifecycle Action Controls (Start, Stop, Restart)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Start Button
            FluentButton(
                text = "Start",
                onClick = onStart,
                enabled = vm.status.canStart && !isActionLoading,
                isLoading = isActionLoading && vm.status == VmStatus.STARTING,
                icon = Icons.Rounded.PlayArrow,
                modifier = Modifier.weight(1f)
            )

            // Stop Button
            FluentSecondaryButton(
                text = "Stop",
                onClick = onStop,
                enabled = vm.status.canStop && !isActionLoading,
                isLoading = isActionLoading && vm.status == VmStatus.STOPPING,
                icon = Icons.Rounded.Stop,
                modifier = Modifier.weight(1f)
            )

            // Restart Button
            FluentSecondaryButton(
                text = "Restart",
                onClick = onRestart,
                enabled = !isActionLoading,
                isLoading = isActionLoading && vm.status == VmStatus.RUNNING,
                icon = Icons.Rounded.RestartAlt,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Native RDP Launcher Button
        FluentButton(
            text = "Connect via RDP (Native)",
            onClick = onLaunchRdp,
            enabled = !isActionLoading,
            icon = Icons.Rounded.DesktopWindows,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
