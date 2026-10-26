package com.example.mobidesk

import com.example.mobidesk.data.model.VmStatus
import com.example.mobidesk.ui.admin.AdminDashboardViewModel
import com.example.mobidesk.util.RdpLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminDashboardAndRdpTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRdpLauncherUriGeneration() {
        // Test default IP and port URI string
        val defaultUriStr = RdpLauncher.buildRdpUriString(null, 3389)
        assertEquals("rdp://full%20address=s:172.16.33.137:3389", defaultUriStr)

        // Test custom IP, port and username
        val customUriStr = RdpLauncher.buildRdpUriString("192.168.122.101", 3389, "student1@mobidesk.edu")
        assertEquals("rdp://full%20address=s:192.168.122.101:3389&username=s:student1@mobidesk.edu", customUriStr)

        // Test empty/blank IP fallback
        val emptyIpUriStr = RdpLauncher.buildRdpUriString("", 3389)
        assertEquals("rdp://full%20address=s:172.16.33.137:3389", emptyIpUriStr)
    }

    @Test
    fun testAdminDashboardViewModelInitialVmsAndAssignments() = runTest {
        val repository = FakeVmRepository()
        val viewModel = AdminDashboardViewModel(repository)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.totalVmsCount)
        assertEquals(5, state.vms.size)
        assertEquals(1, state.runningVmsCount)
        assertEquals(4, state.stoppedVmsCount)

        // Verify win-01 through win-05 assignment emails
        val win01 = state.vms.find { it.vm.name == "win-01" }
        assertNotNull(win01)
        assertEquals("student1@mobidesk.edu", win01?.assignedStudentEmail)
        assertEquals(VmStatus.RUNNING, win01?.vm?.status)

        val win02 = state.vms.find { it.vm.name == "win-02" }
        assertNotNull(win02)
        assertEquals("student2@mobidesk.edu", win02?.assignedStudentEmail)
        assertEquals(VmStatus.STOPPED, win02?.vm?.status)
    }

    @Test
    fun testAdminDashboardViewModelLifecycleActions() = runTest {
        val repository = FakeVmRepository()
        val viewModel = AdminDashboardViewModel(repository)

        testDispatcher.scheduler.advanceUntilIdle()

        // Start win-02 from Admin Dashboard
        viewModel.executeVmLifecycleAction("win-02", "start")
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.value
        val win02 = state.vms.find { it.vm.name == "win-02" }
        assertNotNull(win02)
        assertEquals(VmStatus.RUNNING, win02?.vm?.status)
        assertEquals(2, state.runningVmsCount)

        // Stop win-01 from Admin Dashboard
        viewModel.executeVmLifecycleAction("win-01", "stop")
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.value
        val win01 = state.vms.find { it.vm.name == "win-01" }
        assertNotNull(win01)
        assertEquals(VmStatus.STOPPED, win01?.vm?.status)
    }
}
