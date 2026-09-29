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

class XAIProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : AIProvider {

    override val providerType: AiProvider = AiProvider.XAI

    val nativeAdapter = XAINativeAdapter(client)
    val openAiAdapter = XAICompatibleOpenAIAdapter(client)

    override fun getAdapterName(request: AIRequest): String =
        if (request.preferNative) "xAI Responses Adapter" else "xAI OpenAI-Compatible Adapter"

    override fun getCapabilities(model: String): Set<AICapability> {
        val caps = mutableSetOf(
            AICapability.TEXT_INPUT,
            AICapability.TEXT_OUTPUT,
            AICapability.CODE_GENERATION,
            AICapability.STREAMING,
            AICapability.WEB_SEARCH
        )
        if (model.contains("vision")) {
            caps.add(AICapability.IMAGE_INPUT)
        }
        if (model.contains("reason") || model.contains("grok-3")) {
            caps.add(AICapability.REASONING)
        }
        return caps
    }

    override suspend fun execute(request: AIRequest, apiKey: String): Result<AIResponse> {
        return if (request.preferNative) {
            nativeAdapter.execute(request, apiKey)
        } else {
            openAiAdapter.execute(request, apiKey)
        }
    }

    override suspend fun executeStream(request: AIRequest, apiKey: String): Flow<AIStreamEvent> {
        return nativeAdapter.executeStream(request, apiKey)
    }

    override suspend fun discoverModels(apiKey: String): Result<List<DiscoveredModel>> {
        return nativeAdapter.discoverModels(apiKey)
    }
}

class XAINativeAdapter(private val client: OkHttpClient) {
    private val baseUrl = "https://api.x.ai/v1"

    suspend fun discoverModels(apiKey: String): Result<List<DiscoveredModel>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                AIException(AIErrorCode.INVALID_API_KEY, "xAI API key is missing.", AiProvider.XAI)
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
                    AIException(errCode, "xAI models discovery failed ($code): $body", AiProvider.XAI, code, body)
                )
            }

            val json = JSONObject(body)
            val data = json.optJSONArray("data") ?: JSONArray()
            val models = mutableListOf<DiscoveredModel>()

            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                val id = item.getString("id")

                // Filter out non-grok or retired models
                if (id.startsWith("grok", ignoreCase = true)) {
                    val caps = mutableSetOf(
                        AICapability.TEXT_INPUT,
                        AICapability.TEXT_OUTPUT,
                        AICapability.CODE_GENERATION,
                        AICapability.STREAMING,
                        AICapability.WEB_SEARCH
                    )
                    if (id.contains("vision")) caps.add(AICapability.IMAGE_INPUT)

                    models.add(
                        DiscoveredModel(
                            id = id,
                            displayName = id,
                            description = "xAI Grok model",
                            capabilities = caps,
                            contextWindowTokens = 131072
                        )
                    )
                }
            }

            if (models.isEmpty()) {
                models.add(DiscoveredModel("grok-2", "grok-2", "xAI Grok 2", setOf(AICapability.TEXT_INPUT, AICapability.CODE_GENERATION)))
                models.add(DiscoveredModel("grok-beta", "grok-beta", "xAI Grok Beta", setOf(AICapability.TEXT_INPUT, AICapability.CODE_GENERATION)))
            } else {
                models.sortWith(compareByDescending<DiscoveredModel> {
                    when {
                        it.id.contains("grok-3") -> 100
                        it.id.contains("grok-2") -> 90
                        it.id.contains("beta") -> 80
                        else -> 10
                    }
                })
            }

            Result.success(models)
        } catch (e: Exception) {
            Result.failure(AIException(AIErrorCode.NETWORK_ERROR, "Failed to connect to xAI: ${e.message}", AiProvider.XAI, cause = e))
        }
    }

    suspend fun execute(aiRequest: AIRequest, apiKey: String): Result<AIResponse> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val actualModel = aiRequest.model.ifBlank { "grok-2" }
        val url = "$baseUrl/chat/completions"

        try {
            val rootJson = JSONObject().apply {
                put("model", actualModel)
                val messages = JSONArray()
                if (aiRequest.systemInstruction.isNotBlank()) {
                    messages.put(JSONObject().put("role", "system").put("content", aiRequest.systemInstruction))
                }
                for (msg in aiRequest.messages) {
                    messages.put(JSONObject().put("role", msg.role).put("content", msg.content))
                }
                put("messages", messages)
                aiRequest.temperature?.let { put("temperature", it) }
                aiRequest.maxOutputTokens?.let { put("max_tokens", it) }
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
                    AIException(errCode, "xAI API error ($code): $body", AiProvider.XAI, code, body)
                )
            }

            val json = JSONObject(body)
            val choices = json.getJSONArray("choices")
            val text = choices.getJSONObject(0).getJSONObject("message").getString("content")
            val finishReason = choices.getJSONObject(0).optString("finish_reason", "stop")

            val usageObj = json.optJSONObject("usage")
            val usage = usageObj?.let {
                AIUsage(
                    promptTokens = it.optInt("prompt_tokens", 0),
                    completionTokens = it.optInt("completion_tokens", 0),
                    totalTokens = it.optInt("total_tokens", 0)
                )
            }

            val debugInfo = AIDebugInfo(
                provider = "xAI Grok",
                adapter = "xAI Native",
                baseApi = "v1",
                model = actualModel,
                requestId = response.header("x-request-id"),
                httpStatus = code,
                streamingSupported = true,
                capabilities = setOf(AICapability.TEXT_INPUT, AICapability.TEXT_OUTPUT, AICapability.WEB_SEARCH),
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
            Result.failure(AIException(AIErrorCode.NETWORK_ERROR, "Network error calling xAI: ${e.message}", AiProvider.XAI, cause = e))
        }
    }

    fun executeStream(aiRequest: AIRequest, apiKey: String): Flow<AIStreamEvent> = flow {
        val actualModel = aiRequest.model.ifBlank { "grok-2" }
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

class XAICompatibleOpenAIAdapter(private val client: OkHttpClient) {
    suspend fun execute(aiRequest: AIRequest, apiKey: String): Result<AIResponse> {
        val native = XAINativeAdapter(client)
        return native.execute(aiRequest, apiKey)
    }
}
