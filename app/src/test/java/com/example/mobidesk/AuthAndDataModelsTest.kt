import com.example.mobidesk.data.model.UserRole
import com.example.mobidesk.data.model.VmAssignment
import com.example.mobidesk.data.model.VmDetails
import com.example.mobidesk.data.model.VmStatus
import com.example.mobidesk.data.network.KvmBridgeClient
import com.example.mobidesk.data.repository.AuthRepositoryImpl
import com.example.mobidesk.ui.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthAndDataModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testUserRoleParsing() {
        assertEquals(UserRole.ADMIN, UserRole.fromString("admin"))
        assertEquals(UserRole.ADMIN, UserRole.fromString("ADMINISTRATOR"))
        assertEquals(UserRole.STUDENT, UserRole.fromString("student"))
        assertEquals(UserRole.STUDENT, UserRole.fromString("unknown"))

        assertTrue(UserRole.ADMIN.isAdmin)
        assertFalse(UserRole.ADMIN.isStudent)
        assertTrue(UserRole.STUDENT.isStudent)
    }

    @Test
    fun testVmStatusParsing() {
        assertEquals(VmStatus.RUNNING, VmStatus.fromString("running"))
        assertEquals(VmStatus.STOPPED, VmStatus.fromString("stopped"))
        assertEquals(VmStatus.PAUSED, VmStatus.fromString("paused"))
        assertEquals(VmStatus.ERROR, VmStatus.fromString("error"))

        assertTrue(VmStatus.RUNNING.isOnline)
        assertTrue(VmStatus.STOPPED.canStart)
        assertTrue(VmStatus.RUNNING.canStop)
    }

    @Test
    fun testVmDetailsSerialization() {
        val vm = VmDetails(
            id = "vm-101",
            name = "Linux Lab VM",
            vcpu = 4,
            ramGb = 8,
            storageGb = 100,
            lanIp = "192.168.1.50",
            status = VmStatus.RUNNING
        )

        val encoded = json.encodeToString(vm)
        assertTrue(encoded.contains("\"ram_gb\":8"))
        assertTrue(encoded.contains("\"storage_gb\":100"))
        assertTrue(encoded.contains("\"lan_ip\":\"192.168.1.50\""))

        val decoded = json.decodeFromString<VmDetails>(encoded)
        assertEquals("vm-101", decoded.id)
        assertEquals("Linux Lab VM", decoded.name)
        assertEquals(4, decoded.vcpu)
        assertEquals(8, decoded.ramGb)
        assertEquals(100, decoded.storageGb)
        assertEquals("192.168.1.50", decoded.lanIp)
        assertEquals(VmStatus.RUNNING, decoded.status)
    }

    @Test
    fun testVmAssignmentSerialization() {
        val assignment = VmAssignment(
            id = "assign-001",
            vmId = "vm-101",
            userId = "user-202"
        )

        val encoded = json.encodeToString(assignment)
        assertTrue(encoded.contains("\"vm_id\":\"vm-101\""))
        assertTrue(encoded.contains("\"user_id\":\"user-202\""))

        val decoded = json.decodeFromString<VmAssignment>(encoded)
        assertEquals("assign-001", decoded.id)
        assertEquals("vm-101", decoded.vmId)
        assertEquals("user-202", decoded.userId)
    }

    @Test
    fun testAuthRepositoryDemoAdminSignIn() = runTest {
        val repo = AuthRepositoryImpl()
        val result = repo.signIn("admin@mobidesk.com", "admin123")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(UserRole.ADMIN, user?.role)
        assertTrue(user?.isAdmin == true)
        assertEquals(user, repo.currentProfile.value)
    }

    @Test
    fun testAuthRepositoryDemoStudentSignIn() = runTest {
        val repo = AuthRepositoryImpl()
        val result = repo.signIn("student@mobidesk.com", "student123")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(UserRole.STUDENT, user?.role)
        assertTrue(user?.isStudent == true)
    }

    @Test
    fun testAuthViewModelSignIn() = runTest {
        val repo = AuthRepositoryImpl()
        val viewModel = AuthViewModel(repo)

        viewModel.onEmailChanged("admin@mobidesk.com")
        viewModel.onPasswordChanged("admin123")
        viewModel.signIn()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
        assertEquals(UserRole.ADMIN, state.role)
        assertEquals("admin@mobidesk.com", state.user?.email)

        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterSignOut = viewModel.uiState.value
        assertFalse(stateAfterSignOut.isAuthenticated)
        assertNull(stateAfterSignOut.user)
    }

    @Test
    fun testKvmBridgeClientBaseUrl() {
        val client = KvmBridgeClient("http://192.168.1.10:3000/api")
        assertEquals("http://192.168.1.10:3000/api", client.baseUrl)

        client.setCustomBaseUrl("http://10.0.0.50:8080/api/")
        assertEquals("http://10.0.0.50:8080/api", client.baseUrl)
    }
}
