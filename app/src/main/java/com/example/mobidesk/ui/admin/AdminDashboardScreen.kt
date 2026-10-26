package com.example.mobidesk.ui.admin

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
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Email
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun AdminDashboardScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    adminDashboardViewModel: AdminDashboardViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val adminState by adminDashboardViewModel.uiState.collectAsState()
    val user = authState.user
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fluent Admin Header Top Bar
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
                                imageVector = Icons.Rounded.AdminPanelSettings,
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
                            text = user?.displayName ?: "Admin Console",
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
                            text = "Administrator",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    IconButton(
                        onClick = { adminDashboardViewModel.refreshVms() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh hypervisor pool",
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

        // Error Banner
        AnimatedVisibility(
            visible = adminState.errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            adminState.errorMessage?.let { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
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

        // Adaptive Grid/List Layout based on screen width (Phones: 1 column list, Tablets/Foldables: multi-column grid)
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
                // System Summary Banner Card (spans all columns)
                item(span = { GridItemSpan(gridColumnCount) }) {
                    AdminSystemSummaryCard(
                        totalVms = adminState.totalVmsCount,
                        runningVms = adminState.runningVmsCount,
                        stoppedVms = adminState.stoppedVmsCount
                    )
                }

                // Section Header Title (spans all columns)
                item(span = { GridItemSpan(gridColumnCount) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hypervisor VM Pool (${adminState.vms.size})",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (adminState.isRefreshing || adminState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // VM Cards grid items (win-01 through win-05)
                items(adminState.vms, key = { it.vm.id }) { adminVm ->
                    AdminVmCard(
                        vmItem = adminVm,
                        isActionLoading = adminState.actionInProgressVmId == adminVm.vm.id,
                        onStart = { adminDashboardViewModel.executeVmLifecycleAction(adminVm.vm.id, "start") },
                        onStop = { adminDashboardViewModel.executeVmLifecycleAction(adminVm.vm.id, "stop") },
                        onRestart = { adminDashboardViewModel.executeVmLifecycleAction(adminVm.vm.id, "restart") },
                        onLaunchRdp = {
                            RdpLauncher.launchRdpSession(
                                context = context,
                                ipAddress = adminVm.vm.lanIp ?: "172.16.33.137",
                                port = 3389,
                                username = adminVm.assignedStudentEmail
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
 * System Overview Stats Card for Admin Dashboard.
 */
@Composable
fun AdminSystemSummaryCard(
    totalVms: Int,
    runningVms: Int,
    stoppedVms: Int,
    modifier: Modifier = Modifier
) {
    MicaCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 20.dp,
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Dns,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "KVM Hypervisor Node 01",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Real-time monitoring and host lifecycle control for student Cloud PCs win-01 to win-05.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Host Online",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatPill(
                label = "Total Pool",
                value = "$totalVms VMs",
                modifier = Modifier.weight(1f)
            )
            StatPill(
                label = "Running",
                value = "$runningVms active",
                modifier = Modifier.weight(1f)
            )
            StatPill(
                label = "Stopped",
                value = "$stoppedVms offline",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Individual Admin VM Card displaying win-01..win-05 specs, assigned student email, LAN IP, lifecycle controls & RDP launcher.
 */
@Composable
fun AdminVmCard(
    vmItem: AdminVmItem,
    isActionLoading: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onLaunchRdp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vm = vmItem.vm

    MicaCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 18.dp
    ) {
        // Top Header: Icon, VM Name, OS & Status Badge
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

        Spacer(modifier = Modifier.height(12.dp))

        // Assigned Student Email row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Email,
                contentDescription = "Assigned Student",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Assigned Student: ",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
                text = vmItem.assignedStudentEmail,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hardware Specs Row (2 vCPU | 2 GB RAM | 40 GB Storage)
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

        // LAN IP Address Display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lan,
                contentDescription = "LAN IP Address",
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

        Spacer(modifier = Modifier.height(14.dp))

        // Host Lifecycle Action Controls (Start, Stop, Restart)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FluentButton(
                text = "Start",
                onClick = onStart,
                enabled = vm.status.canStart && !isActionLoading,
                isLoading = isActionLoading && vm.status == VmStatus.STARTING,
                icon = Icons.Rounded.PlayArrow,
                modifier = Modifier.weight(1f)
            )

            FluentSecondaryButton(
                text = "Stop",
                onClick = onStop,
                enabled = vm.status.canStop && !isActionLoading,
                isLoading = isActionLoading && vm.status == VmStatus.STOPPING,
                icon = Icons.Rounded.Stop,
                modifier = Modifier.weight(1f)
            )

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
