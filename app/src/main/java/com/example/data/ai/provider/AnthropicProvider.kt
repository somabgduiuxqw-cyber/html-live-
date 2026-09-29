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

class AnthropicProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : AIProvider {

    override val providerType: AiProvider = AiProvider.ANTHROPIC
    val messagesAdapter = AnthropicMessagesAdapter(client)

    override fun getAdapterName(request: AIRequest): String = "Anthropic Messages Adapter"

    override fun getCapabilities(model: String): Set<AICapability> {
        return setOf(
            AICapability.TEXT_INPUT,
            AICapability.TEXT_OUTPUT,
            AICapability.IMAGE_INPUT,
            AICapability.CODE_GENERATION,
            AICapability.STREAMING,
            AICapability.LONG_CONTEXT
        )
    }

    override suspend fun execute(request: AIRequest, apiKey: String): Result<AIResponse> {
        return messagesAdapter.execute(request, apiKey)
    }

    override suspend fun executeStream(request: AIRequest, apiKey: String): Flow<AIStreamEvent> {
        return messagesAdapter.executeStream(request, apiKey)
    }

    override suspend fun discoverModels(apiKey: String): Result<List<DiscoveredModel>> {
        return messagesAdapter.discoverModels(apiKey)
    }
}

class AnthropicMessagesAdapter(private val client: OkHttpClient) {
    private val baseUrl = "https://api.anthropic.com/v1"

    suspend fun discoverModels(apiKey: String): Result<List<DiscoveredModel>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                AIException(AIErrorCode.INVALID_API_KEY, "Anthropic API key is missing.", AiProvider.ANTHROPIC)
            )
        }

        try {
            val request = Request.Builder()
                .url("$baseUrl/models")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val code = response.code
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                // If model list is restricted or unavailable on older keys, provide verified active Claude models
                val fallbackModels = getActiveClaudeModels()
                return@withContext Result.success(fallbackModels)
            }

            val json = JSONObject(body)
            val data = json.optJSONArray("data") ?: JSONArray()
            val models = mutableListOf<DiscoveredModel>()

            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                val id = item.getString("id")
                val displayName = item.optString("display_name", id)

                // Check lifecycle states: skip retired
                val isRetired = id.contains("claude-1") || id.contains("claude-2.0")
                val isDeprecated = id.contains("claude-2.1") || id.contains("claude-instant")

                if (!isRetired) {
                    models.add(
                        DiscoveredModel(
                            id = id,
                            displayName = displayName,
                            description = "Anthropic Claude model",
                            isDeprecated = isDeprecated,
                            isRetired = false,
                            capabilities = setOf(
                                AICapability.TEXT_INPUT,
                                AICapability.TEXT_OUTPUT,
                                AICapability.IMAGE_INPUT,
                                AICapability.CODE_GENERATION,
                                AICapability.STREAMING
                            ),
                            contextWindowTokens = 200000
                        )
                    )
                }
            }

            if (models.isEmpty()) {
                models.addAll(getActiveClaudeModels())
            }

            models.sortWith(compareByDescending<DiscoveredModel> {
                when {
                    it.id.contains("3-5-sonnet") -> 100
                    it.id.contains("3-5-haiku") -> 90
                    it.id.contains("3-opus") -> 80
                    !it.isDeprecated -> 50
                    else -> 10
                }
            })

            Result.success(models)
        } catch (e: Exception) {
            Result.success(getActiveClaudeModels())
        }
    }

    private fun getActiveClaudeModels(): List<DiscoveredModel> {
        return listOf(
            DiscoveredModel(
                id = "claude-3-5-sonnet-20241022",
                displayName = "Claude 3.5 Sonnet",
                description = "Most capable model for coding and reasoning",
                capabilities = setOf(AICapability.TEXT_INPUT, AICapability.TEXT_OUTPUT, AICapability.IMAGE_INPUT, AICapability.CODE_GENERATION, AICapability.STREAMING)
            ),
            DiscoveredModel(
                id = "claude-3-5-haiku-20241022",
                displayName = "Claude 3.5 Haiku",
                description = "Fastest, lightweight model",
                capabilities = setOf(AICapability.TEXT_INPUT, AICapability.TEXT_OUTPUT, AICapability.IMAGE_INPUT, AICapability.CODE_GENERATION, AICapability.STREAMING)
            ),
            DiscoveredModel(
                id = "claude-3-opus-20240229",
                displayName = "Claude 3 Opus",
                description = "High complexity reasoning model",
                capabilities = setOf(AICapability.TEXT_INPUT, AICapability.TEXT_OUTPUT, AICapability.IMAGE_INPUT, AICapability.CODE_GENERATION, AICapability.STREAMING)
            )
        )
    }

    suspend fun execute(aiRequest: AIRequest, apiKey: String): Result<AIResponse> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val actualModel = aiRequest.model.ifBlank { "claude-3-5-sonnet-20241022" }
        val url = "$baseUrl/messages"

        try {
            val rootJson = JSONObject().apply {
                put("model", actualModel)
                put("max_tokens", aiRequest.maxOutputTokens ?: 4096)
                if (aiRequest.systemInstruction.isNotBlank()) {
                    put("system", aiRequest.systemInstruction)
                }

                val messagesArr = JSONArray()
                for (msg in aiRequest.messages) {
                    val role = if (msg.role == "assistant") "assistant" else "user"
                    val contentArr = JSONArray()
                    contentArr.put(JSONObject().put("type", "text").put("text", msg.content))
                    for (img in msg.images) {
                        contentArr.put(JSONObject().apply {
                            put("type", "image")
                            put("source", JSONObject().apply {
                                put("type", "base64")
                                put("media_type", "image/jpeg")
                                put("data", img.substringAfter("base64,"))
                            })
                        })
                    }
                    messagesArr.put(JSONObject().apply {
                        put("role", role)
                        put("content", contentArr)
                    })
                }
                put("messages", messagesArr)
            }

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
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
                    AIException(errCode, "Anthropic API error ($code): $body", AiProvider.ANTHROPIC, code, body)
                )
            }

            val json = JSONObject(body)
            val contentArr = json.getJSONArray("content")
            val textBuilder = StringBuilder()
            for (i in 0 until contentArr.length()) {
                val block = contentArr.getJSONObject(i)
                if (block.optString("type") == "text") {
                    textBuilder.append(block.getString("text"))
                }
            }

            val stopReason = json.optString("stop_reason", "end_turn")
            val usageObj = json.optJSONObject("usage")
            val usage = usageObj?.let {
                AIUsage(
                    promptTokens = it.optInt("input_tokens", 0),
                    completionTokens = it.optInt("output_tokens", 0),
                    totalTokens = it.optInt("input_tokens", 0) + it.optInt("output_tokens", 0)
                )
            }

            val debugInfo = AIDebugInfo(
                provider = "Anthropic Claude",
                adapter = "Anthropic Messages Native",
                baseApi = "v1/messages",
                model = actualModel,
                requestId = response.header("request-id"),
                httpStatus = code,
                streamingSupported = true,
                capabilities = setOf(AICapability.TEXT_INPUT, AICapability.TEXT_OUTPUT, AICapability.IMAGE_INPUT),
                requestTimeMs = duration
            )

            Result.success(
                AIResponse(
                    text = textBuilder.toString(),
                    model = actualModel,
                    finishReason = stopReason,
                    usage = usage,
                    requestId = debugInfo.requestId,
                    debugInfo = debugInfo
                )
            )
        } catch (e: Exception) {
            Result.failure(AIException(AIErrorCode.NETWORK_ERROR, "Network error calling Anthropic: ${e.message}", AiProvider.ANTHROPIC, cause = e))
        }
    }

    fun executeStream(aiRequest: AIRequest, apiKey: String): Flow<AIStreamEvent> = flow {
        val actualModel = aiRequest.model.ifBlank { "claude-3-5-sonnet-20241022" }
        val url = "$baseUrl/messages"

        val rootJson = JSONObject().apply {
            put("model", actualModel)
            put("stream", true)
            put("max_tokens", aiRequest.maxOutputTokens ?: 4096)
            if (aiRequest.systemInstruction.isNotBlank()) {
                put("system", aiRequest.systemInstruction)
            }
            val messagesArr = JSONArray()
            for (msg in aiRequest.messages) {
                val role = if (msg.role == "assistant") "assistant" else "user"
                messagesArr.put(JSONObject().apply {
                    put("role", role)
                    put("content", msg.content)
                })
            }
            put("messages", messagesArr)
        }

        val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val reader = BufferedReader(InputStreamReader(response.body!!.byteStream()))
        var line = reader.readLine()
        val full = StringBuilder()

        while (line != null) {
            if (line.startsWith("data: ")) {
                val data = line.removePrefix("data: ").trim()
                try {
                    val eventObj = JSONObject(data)
                    val type = eventObj.optString("type")
                    if (type == "content_block_delta") {
                        val delta = eventObj.optJSONObject("delta")?.optString("text", "") ?: ""
                        if (delta.isNotEmpty()) {
                            full.append(delta)
                            emit(AIStreamEvent.TextDelta(delta))
                        }
                    }
                } catch (_: Exception) {}
            }
            line = reader.readLine()
        }
        emit(AIStreamEvent.Done(AIResponse(text = full.toString(), model = actualModel)))
    }.flowOn(Dispatchers.IO)
}
