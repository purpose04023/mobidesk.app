package com.example.mobidesk.data.network

import com.example.mobidesk.data.model.KvmApiResponse
import com.example.mobidesk.data.model.SystemStats
import com.example.mobidesk.data.model.VmActionRequest
import com.example.mobidesk.data.model.VmAssignment
import com.example.mobidesk.data.model.VmCreateRequest
import com.example.mobidesk.data.model.VmDetails
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Ktor REST API Client configured to communicate with the Node.js Express KVM hypervisor bridge.
 */
class KvmBridgeClient(
    baseUrl: String = DEFAULT_BASE_URL
) {
    var baseUrl: String = baseUrl
        private set

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
        prettyPrint = true
    }

    val httpClient: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(jsonConfig)
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    fun setCustomBaseUrl(url: String) {
        if (url.isNotBlank()) {
            baseUrl = url.trimEnd('/')
        }
    }

    /**
     * Helper method to handle HTTP status codes and safely parse responses.
     */
    private suspend inline fun <reified T> safeApiCall(
        call: () -> HttpResponse
    ): Result<T> {
        return try {
            val response = call()
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created, HttpStatusCode.Accepted -> {
                    val body = response.body<T>()
                    Result.success(body)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Unauthorized access to KVM bridge API (401)"))
                }
                HttpStatusCode.Forbidden -> {
                    Result.failure(Exception("Forbidden operation on KVM bridge (403)"))
                }
                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("Requested resource not found on KVM bridge (404)"))
                }
                HttpStatusCode.InternalServerError -> {
                    val errorText = response.bodyAsText()
                    Result.failure(Exception("KVM bridge server error (500): $errorText"))
                }
                else -> {
                    val errorText = response.bodyAsText()
                    Result.failure(Exception("HTTP Error ${response.status.value}: $errorText"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieve list of all Virtual Machines managed by KVM bridge.
     */
    suspend fun getVms(): Result<List<VmDetails>> {
        return safeApiCall<List<VmDetails>> {
            httpClient.get("$baseUrl/vms")
        }
    }

    /**
     * Get single VM details by ID.
     */
    suspend fun getVmById(id: String): Result<VmDetails> {
        return safeApiCall<VmDetails> {
            httpClient.get("$baseUrl/vms/$id")
        }
    }

    /**
     * Execute power lifecycle action on a VM (start, stop, restart, pause, resume).
     */
    suspend fun executeVmAction(id: String, action: String, force: Boolean = false): Result<VmDetails> {
        return safeApiCall<VmDetails> {
            httpClient.post("$baseUrl/vms/$id/action") {
                setBody(VmActionRequest(action = action, force = force))
            }
        }
    }

    /**
     * Create a new VM instance.
     */
    suspend fun createVm(request: VmCreateRequest): Result<VmDetails> {
        return safeApiCall<VmDetails> {
            httpClient.post("$baseUrl/vms") {
                setBody(request)
            }
        }
    }

    /**
     * Delete a VM instance.
     */
    suspend fun deleteVm(id: String): Result<Boolean> {
        return safeApiCall<KvmApiResponse<String>> {
            httpClient.delete("$baseUrl/vms/$id")
        }.map { it.success }
    }

    /**
     * Retrieve system performance metrics (CPU, RAM, storage, VM counts).
     */
    suspend fun getSystemStats(): Result<SystemStats> {
        return safeApiCall<SystemStats> {
            httpClient.get("$baseUrl/system/stats")
        }
    }

    /**
     * Retrieve all VM user assignments.
     */
    suspend fun getAssignments(): Result<List<VmAssignment>> {
        return safeApiCall<List<VmAssignment>> {
            httpClient.get("$baseUrl/assignments")
        }
    }

    /**
     * Assign a VM to a user.
     */
    suspend fun assignVm(vmId: String, userId: String): Result<VmAssignment> {
        return safeApiCall<VmAssignment> {
            httpClient.post("$baseUrl/assignments") {
                setBody(mapOf("vm_id" to vmId, "user_id" to userId))
            }
        }
    }

    /**
     * Unassign a VM assignment.
     */
    suspend fun unassignVm(assignmentId: String): Result<Boolean> {
        return safeApiCall<KvmApiResponse<String>> {
            httpClient.delete("$baseUrl/assignments/$assignmentId")
        }.map { it.success }
    }

    companion object {
        const val DEFAULT_BASE_URL = "http://10.0.2.2:3000/api"

        @Volatile
        private var instance: KvmBridgeClient? = null

        fun getInstance(baseUrl: String = DEFAULT_BASE_URL): KvmBridgeClient {
            return instance ?: synchronized(this) {
                instance ?: KvmBridgeClient(baseUrl).also { instance = it }
            }
        }
    }
}
