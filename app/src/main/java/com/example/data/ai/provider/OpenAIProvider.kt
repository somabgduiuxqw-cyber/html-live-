package com.example.data.ai.provider

import com.example.data.ai.model.AICapability
import com.example.data.ai.model.AIDebugInfo
import com.example.data.ai.model.AIErrorCode
import com.example.data.ai.model.AIException
import com.example.data.ai.model.AIRequest
import com.example.data.ai.model.AIResponse
import com.example.data.ai.model.AIStreamEvent
import com.example.data.ai.model.AIUsage
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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class OpenAIProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : AIProvider {

    override val providerType: AiProvider = AiProvider.OPENAI
    val nativeAdapter = OpenAINativeAdapter(client)

    override fun getAdapterName(request: AIRequest): String = "OpenAI Native Adapter"

    override fun getCapabilities(model: String): Set<AICapability> {
        val caps = mutableSetOf(
            AICapability.TEXT_INPUT,
            AICapability.TEXT_OUTPUT,
            AICapability.CODE_GENERATION,
            AICapability.STREAMING,
            AICapability.STRUCTURED_OUTPUT
        )
        if (model.startsWith("o1") || model.startsWith("o3")) {
            caps.add(AICapability.REASONING)
        }
        if (model.contains("4o")) {
            caps.add(AICapability.IMAGE_INPUT)
            caps.add(AICapability.TOOL_CALLING)
        }
        return caps
    }

    override suspend fun execute(request: AIRequest, apiKey: String): Result<AIResponse> {
        return nativeAdapter.execute(request, apiKey)
    }

    override suspend fun executeStream(request: AIRequest, apiKey: String): Flow<AIStreamEvent> {
        return nativeAdapter.executeStream(request, apiKey)
    }

    override suspend fun discoverModels(apiKey: String): Result<List<DiscoveredModel>> {
        return nativeAdapter.discoverModels(apiKey)
    }
}

class OpenAINativeAdapter(private val client: OkHttpClient) {
    private val baseUrl = "https://api.openai.com/v1"

    suspend fun discoverModels(apiKey: String): Result<List<DiscoveredModel>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                AIException(AIErrorCode.INVALID_API_KEY, "OpenAI API key is missing.", AiProvider.OPENAI)
            )
        }

        try {
            val request = Request.Builder()
                .url("$baseUrl/models")
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val code = response.code
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errCode = when (code) {
                    401 -> AIErrorCode.INVALID_API_KEY
                    403 -> AIErrorCode.PERMISSION_DENIED
                    429 -> AIErrorCode.RATE_LIMITED
                    else -> AIErrorCode.SERVER_ERROR
                }
                return@withContext Result.failure(
                    AIException(errCode, "OpenAI models discovery failed ($code): $body", AiProvider.OPENAI, code, body)
                )
            }

            val json = JSONObject(body)
            val data = json.optJSONArray("data") ?: JSONArray()
            val models = mutableListOf<DiscoveredModel>()

            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                val id = item.getString("id")

                // Filter for coding/chat models
                val isSupported = id.startsWith("gpt-4o") ||
                    id.startsWith("gpt-4-turbo") ||
                    id.startsWith("o1") ||
                    id.startsWith("o3") ||
                    id == "gpt-4" ||
                    id.startsWith("chatgpt-4o")

                if (isSupported && !id.contains("realtime") && !id.contains("audio")) {
                    val caps = mutableSetOf(
                        AICapability.TEXT_INPUT,
                        AICapability.TEXT_OUTPUT,
                        AICapability.CODE_GENERATION,
                        AICapability.STREAMING
                    )
                    if (id.contains("4o")) caps.add(AICapability.IMAGE_INPUT)
                    if (id.startsWith("o1") || id.startsWith("o3")) caps.add(AICapability.REASONING)

                    models.add(
                        DiscoveredModel(
                            id = id,
                            displayName = id,
                            description = "OpenAI active model",
                            capabilities = caps,
                            contextWindowTokens = 128000
                        )
                    )
                }
            }

            models.sortWith(compareByDescending<DiscoveredModel> {
                when {
                    it.id == "gpt-4o" -> 100
                    it.id == "gpt-4o-mini" -> 90
                    it.id.startsWith("o3") -> 85
                    it.id.startsWith("o1") -> 80
                    else -> 10
                }
            })

            Result.success(models)
        } catch (e: Exception) {
            Result.failure(AIException(AIErrorCode.NETWORK_ERROR, "Failed to connect to OpenAI: ${e.message}", AiProvider.OPENAI, cause = e))
        }
    }

    suspend fun execute(aiRequest: AIRequest, apiKey: String): Result<AIResponse> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val actualModel = aiRequest.model.ifBlank { "gpt-4o" }

        // Use modern Responses API where supported, or Chat Completions
        val url = "$baseUrl/chat/completions"

        try {
            val rootJson = JSONObject().apply {
                put("model", actualModel)
                val messages = JSONArray()
                if (aiRequest.systemInstruction.isNotBlank()) {
                    messages.put(JSONObject().put("role", "system").put("content", aiRequest.systemInstruction))
                }
                for (msg in aiRequest.messages) {
                    if (msg.images.isNotEmpty()) {
                        val contentArr = JSONArray()
                        contentArr.put(JSONObject().put("type", "text").put("text", msg.content))
                        for (img in msg.images) {
                            contentArr.put(
                                JSONObject().apply {
                                    put("type", "image_url")
                                    put("image_url", JSONObject().put("url", if (img.startsWith("data:")) img else "data:image/jpeg;base64,$img"))
                                }
                            )
                        }
                        messages.put(JSONObject().put("role", msg.role).put("content", contentArr))
                    } else {
                        messages.put(JSONObject().put("role", msg.role).put("content", msg.content))
                    }
                }
                put("messages", messages)

                if (!actualModel.startsWith("o1") && !actualModel.startsWith("o3")) {
                    aiRequest.temperature?.let { put("temperature", it) }
                    aiRequest.maxOutputTokens?.let { put("max_tokens", it) }
                } else if (aiRequest.reasoning != null) {
                    put("reasoning_effort", aiRequest.reasoning)
                }
            }

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val code = response.code
            val body = response.body?.string() ?: ""
            val duration = System.currentTimeMillis() - startTime

            if (!response.isSuccessful) {
                val errCode = when (code) {
                    401 -> AIErrorCode.INVALID_API_KEY
                    404 -> AIErrorCode.MODEL_NOT_FOUND
                    429 -> AIErrorCode.RATE_LIMITED
                    else -> AIErrorCode.SERVER_ERROR
                }
                return@withContext Result.failure(
                    AIException(errCode, "OpenAI API error ($code): $body", AiProvider.OPENAI, code, body)
                )
            }

            val json = JSONObject(body)
            val choices = json.getJSONArray("choices")
            val firstChoice = choices.getJSONObject(0)
            val text = firstChoice.getJSONObject("message").getString("content")
            val finishReason = firstChoice.optString("finish_reason", "stop")

            val usageObj = json.optJSONObject("usage")
            val usage = usageObj?.let {
                AIUsage(
                    promptTokens = it.optInt("prompt_tokens", 0),
                    completionTokens = it.optInt("completion_tokens", 0),
                    totalTokens = it.optInt("total_tokens", 0)
                )
            }

            val debugInfo = AIDebugInfo(
                provider = "OpenAI",
                adapter = "OpenAI Native",
                baseApi = "v1",
                model = actualModel,
                requestId = response.header("x-request-id"),
                httpStatus = code,
                streamingSupported = true,
                capabilities = setOf(AICapability.TEXT_INPUT, AICapability.TEXT_OUTPUT),
                requestTimeMs = duration
            )

            Result.success(
                AIResponse(
                    text = text,
                    model = actualModel,
                    finishReason = finishReason,
                    usage = usage,
                    requestId = debugInfo.requestId,
                    debugInfo = debugInfo
                )
            )
        } catch (e: Exception) {
            Result.failure(AIException(AIErrorCode.NETWORK_ERROR, "Network error calling OpenAI: ${e.message}", AiProvider.OPENAI, cause = e))
        }
    }

    fun executeStream(aiRequest: AIRequest, apiKey: String): Flow<AIStreamEvent> = flow {
        val actualModel = aiRequest.model.ifBlank { "gpt-4o" }
        val url = "$baseUrl/chat/completions"

        val rootJson = JSONObject().apply {
            put("model", actualModel)
            put("stream", true)
            val messages = JSONArray()
            if (aiRequest.systemInstruction.isNotBlank()) {
                messages.put(JSONObject().put("role", "system").put("content", aiRequest.systemInstruction))
            }
            for (msg in aiRequest.messages) {
                messages.put(JSONObject().put("role", msg.role).put("content", msg.content))
            }
            put("messages", messages)
        }

        val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val reader = BufferedReader(InputStreamReader(response.body!!.byteStream()))
        var line = reader.readLine()
        val full = StringBuilder()

        while (line != null) {
            if (line.startsWith("data: ")) {
                val data = line.removePrefix("data: ").trim()
                if (data == "[DONE]") break
                try {
                    val delta = JSONObject(data).getJSONArray("choices").getJSONObject(0).getJSONObject("delta").optString("content", "")
                    if (delta.isNotEmpty()) {
                        full.append(delta)
                        emit(AIStreamEvent.TextDelta(delta))
                    }
                } catch (_: Exception) {}
            }
            line = reader.readLine()
        }
        emit(AIStreamEvent.Done(AIResponse(text = full.toString(), model = actualModel)))
    }.flowOn(Dispatchers.IO)
}
