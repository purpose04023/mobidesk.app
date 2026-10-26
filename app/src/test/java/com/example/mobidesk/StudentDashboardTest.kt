package com.example.mobidesk

import com.example.mobidesk.data.model.VmDetails
import com.example.mobidesk.data.model.VmStatus
import com.example.mobidesk.data.repository.VmRepository
import com.example.mobidesk.ui.student.StudentDashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

class FakeVmRepository : VmRepository {
    private val initialVms = listOf(
        VmDetails(id = "win-01", name = "win-01", vcpu = 2, ramGb = 2, storageGb = 40, lanIp = "192.168.122.101", status = VmStatus.RUNNING),
        VmDetails(id = "win-02", name = "win-02", vcpu = 2, ramGb = 2, storageGb = 40, lanIp = "192.168.122.102", status = VmStatus.STOPPED),
        VmDetails(id = "win-03", name = "win-03", vcpu = 2, ramGb = 2, storageGb = 40, lanIp = "192.168.122.103", status = VmStatus.STOPPED),
        VmDetails(id = "win-04", name = "win-04", vcpu = 2, ramGb = 2, storageGb = 40, lanIp = "192.168.122.104", status = VmStatus.STOPPED),
        VmDetails(id = "win-05", name = "win-05", vcpu = 2, ramGb = 2, storageGb = 40, lanIp = "192.168.122.105", status = VmStatus.STOPPED)
    )

    private val _vmsFlow = MutableStateFlow(initialVms)
    override val studentVmsFlow: StateFlow<List<VmDetails>> = _vmsFlow.asStateFlow()

    override suspend fun getStudentVms(userId: String): Result<List<VmDetails>> {
        return Result.success(_vmsFlow.value)
    }

    override suspend fun executeVmAction(vmId: String, action: String): Result<VmDetails> {
        val target = _vmsFlow.value.find { it.id == vmId || it.name == vmId }
            ?: return Result.failure(IllegalArgumentException("VM not found"))

        val newStatus = when (action.lowercase()) {
            "start" -> VmStatus.RUNNING
            "stop" -> VmStatus.STOPPED
            "restart" -> VmStatus.RUNNING
            else -> target.status
        }

        val updated = target.copy(status = newStatus)
        _vmsFlow.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
        return Result.success(updated)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class StudentDashboardTest {

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
    fun testVmRepositoryAssignedCloudPcs() = runTest {
        val repository = FakeVmRepository()
        val result = repository.getStudentVms("student-001")

        assertTrue(result.isSuccess)
        val vms = result.getOrNull()
        assertNotNull(vms)
        assertEquals(5, vms?.size)

        val vmNames = vms?.map { it.name }
        assertEquals(listOf("win-01", "win-02", "win-03", "win-04", "win-05"), vmNames)

        vms?.forEach { vm ->
            assertEquals(2, vm.vcpu)
            assertEquals(2, vm.ramGb)
            assertEquals(40, vm.storageGb)
        }
    }

    @Test
    fun testVmRepositoryLifecycleActions() = runTest {
        val repository = FakeVmRepository()

        // Test stopping win-01
        val stopResult = repository.executeVmAction("win-01", "stop")
        assertTrue(stopResult.isSuccess)
        assertEquals(VmStatus.STOPPED, stopResult.getOrNull()?.status)

        // Test starting win-01
        val startResult = repository.executeVmAction("win-01", "start")
        assertTrue(startResult.isSuccess)
        assertEquals(VmStatus.RUNNING, startResult.getOrNull()?.status)
        assertEquals("192.168.122.101", startResult.getOrNull()?.lanIp)
    }

    @Test
    fun testStudentDashboardViewModelStateAndAction() = runTest {
        val repository = FakeVmRepository()
        val viewModel = StudentDashboardViewModel(repository)

        viewModel.loadVms("student-001")
        testDispatcher.scheduler.advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertEquals(5, initialState.vms.size)
        assertEquals("win-01", initialState.activeVm?.name)

        // Execute action on win-02 (stop to start)
        viewModel.executeVmLifecycleAction("win-02", "start")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        val win02 = updatedState.vms.find { it.name == "win-02" }
        assertNotNull(win02)
        assertEquals(VmStatus.RUNNING, win02?.status)
    }
}
