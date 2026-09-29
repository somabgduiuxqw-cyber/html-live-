package com.example.data.ai.provider

import com.example.data.ai.model.AICapability
import com.example.data.ai.model.AIDebugInfo
import com.example.data.ai.model.AIErrorCode
import com.example.data.ai.model.AIException
import com.example.data.ai.model.AIRequest
import com.example.data.ai.model.AIResponse
import com.example.data.ai.model.AIStreamEvent
import com.example.data.ai.model.DiscoveredModel
import com.example.model.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CustomProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : AIProvider {

    override val providerType: AiProvider = AiProvider.CUSTOM

    override fun getAdapterName(request: AIRequest): String = "Custom OpenAI-Compatible Adapter"

    override fun getCapabilities(model: String): Set<AICapability> = setOf(
        AICapability.TEXT_INPUT,
        AICapability.TEXT_OUTPUT,
        AICapability.CODE_GENERATION,
        AICapability.STREAMING
    )

    override suspend fun execute(request: AIRequest, apiKey: String): Result<AIResponse> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val endpoint = request.metadata["customEndpoint"]?.ifBlank { "https://api.openai.com/v1/chat/completions" }
            ?: "https://api.openai.com/v1/chat/completions"
        val actualModel = request.model.ifBlank { "gpt-4o" }

        try {
            val rootJson = JSONObject().apply {
                put("model", actualModel)
                val messages = JSONArray()
                if (request.systemInstruction.isNotBlank()) {
                    messages.put(JSONObject().put("role", "system").put("content", request.systemInstruction))
                }
                for (msg in request.messages) {
                    messages.put(JSONObject().put("role", msg.role).put("content", msg.content))
                }
                put("messages", messages)
            }

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val builder = Request.Builder().url(endpoint).post(requestBody)
            if (apiKey.isNotBlank()) {
                builder.addHeader("Authorization", "Bearer $apiKey")
            }

            val response = client.newCall(builder.build()).execute()
            val code = response.code
            val body = response.body?.string() ?: ""
            val duration = System.currentTimeMillis() - startTime

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    AIException(AIErrorCode.SERVER_ERROR, "Custom provider error ($code): $body", AiProvider.CUSTOM, code, body)
                )
            }

            val json = JSONObject(body)
            val choices = json.optJSONArray("choices")
            val text = if (choices != null && choices.length() > 0) {
                choices.getJSONObject(0).getJSONObject("message").getString("content")
            } else {
                body
            }

            val debugInfo = AIDebugInfo(
                provider = "Custom Endpoint",
                adapter = "Custom OpenAI-Compatible",
                baseApi = endpoint,
                model = actualModel,
                requestId = response.header("x-request-id"),
                httpStatus = code,
                streamingSupported = false,
                capabilities = setOf(AICapability.TEXT_INPUT, AICapability.TEXT_OUTPUT),
                requestTimeMs = duration
            )

            Result.success(AIResponse(text = text, model = actualModel, debugInfo = debugInfo))
        } catch (e: Exception) {
            Result.failure(AIException(AIErrorCode.NETWORK_ERROR, "Error connecting to custom endpoint: ${e.message}", AiProvider.CUSTOM, cause = e))
        }
    }

    override suspend fun executeStream(request: AIRequest, apiKey: String): Flow<AIStreamEvent> = flow {
        val result = execute(request, apiKey)
        if (result.isSuccess) {
            val resp = result.getOrThrow()
            emit(AIStreamEvent.TextDelta(resp.text))
            emit(AIStreamEvent.Done(resp))
        } else {
            val ex = result.exceptionOrNull()
            emit(AIStreamEvent.Error(AIException(AIErrorCode.SERVER_ERROR, ex?.message ?: "Unknown error", AiProvider.CUSTOM)))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun discoverModels(apiKey: String): Result<List<DiscoveredModel>> = withContext(Dispatchers.IO) {
        Result.success(
            listOf(
                DiscoveredModel("gpt-4o", "gpt-4o", "Default model"),
                DiscoveredModel("gpt-4o-mini", "gpt-4o-mini", "Lightweight model"),
                DiscoveredModel("claude-3-5-sonnet", "claude-3-5-sonnet", "Claude via proxy"),
                DiscoveredModel("llama-3.3-70b", "llama-3.3-70b", "Local / Open model")
            )
        )
    }
}
