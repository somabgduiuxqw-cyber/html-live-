package com.example.data.ai.provider

import com.example.data.ai.model.AICapability
import com.example.data.ai.model.AICitation
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

class GeminiProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : AIProvider {

    override val providerType: AiProvider = AiProvider.GEMINI

    val nativeAdapter = GeminiNativeAdapter(client)
    val openAiAdapter = GeminiOpenAICompatibilityAdapter(client)

    override fun getAdapterName(request: AIRequest): String {
        return if (request.preferNative) "Gemini Native Adapter" else "Gemini OpenAI Compatibility Adapter"
    }

    override fun getCapabilities(model: String): Set<AICapability> {
        return setOf(
            AICapability.TEXT_INPUT,
            AICapability.TEXT_OUTPUT,
            AICapability.IMAGE_INPUT,
            AICapability.FILE_INPUT,
            AICapability.CODE_GENERATION,
            AICapability.REASONING,
            AICapability.TOOL_CALLING,
            AICapability.STREAMING,
            AICapability.STRUCTURED_OUTPUT,
            AICapability.LONG_CONTEXT
        )
    }

    override suspend fun execute(request: AIRequest, apiKey: String): Result<AIResponse> {
        return if (request.preferNative) {
            nativeAdapter.generateContent(request, apiKey)
        } else {
            openAiAdapter.generateContent(request, apiKey)
        }
    }

    override suspend fun executeStream(request: AIRequest, apiKey: String): Flow<AIStreamEvent> {
        return if (request.preferNative) {
            nativeAdapter.streamContent(request, apiKey)
        } else {
            openAiAdapter.streamContent(request, apiKey)
        }
    }

    override suspend fun discoverModels(apiKey: String): Result<List<DiscoveredModel>> {
        return nativeAdapter.discoverModels(apiKey)
    }
}

class GeminiNativeAdapter(private val client: OkHttpClient) {
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"

    suspend fun discoverModels(apiKey: String): Result<List<DiscoveredModel>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                AIException(AIErrorCode.INVALID_API_KEY, "Gemini API key is missing.", AiProvider.GEMINI)
            )
        }

        try {
            val url = "$baseUrl/models?key=$apiKey"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val code = response.code
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorCode = when (code) {
                    400 -> AIErrorCode.INVALID_API_KEY
                    403 -> AIErrorCode.PERMISSION_DENIED
                    429 -> AIErrorCode.RATE_LIMITED
                    else -> AIErrorCode.SERVER_ERROR
                }
                return@withContext Result.failure(
                    AIException(errorCode, "Gemini discovery failed ($code): $body", AiProvider.GEMINI, code, body)
                )
            }

            val json = JSONObject(body)
            val modelsArray = json.optJSONArray("models") ?: JSONArray()
            val models = mutableListOf<DiscoveredModel>()

            for (i in 0 until modelsArray.length()) {
                val m = modelsArray.getJSONObject(i)
                val rawName = m.getString("name") // e.g. "models/gemini-2.5-flash"
                val id = rawName.removePrefix("models/")
                val displayName = m.optString("displayName", id)
                val description = m.optString("description", "")
                val supportedMethods = m.optJSONArray("supportedGenerationMethods")

                var canGenerate = false
                if (supportedMethods != null) {
                    for (j in 0 until supportedMethods.length()) {
                        if (supportedMethods.getString(j) == "generateContent") {
                            canGenerate = true
                            break
                        }
                    }
                }

                // Filter out embeddings, TTS, speech-only, image/video-only, retired models
                val isExcluded = !canGenerate ||
                    id.contains("embedding", ignoreCase = true) ||
                    id.contains("aqa", ignoreCase = true) ||
                    id.contains("tts", ignoreCase = true) ||
                    id.contains("speech", ignoreCase = true) ||
                    id.contains("imagen", ignoreCase = true) ||
                    id.contains("veo", ignoreCase = true) ||
                    id.contains("legacy", ignoreCase = true) ||
                    id.contains("bison", ignoreCase = true)

                if (!isExcluded) {
                    val inputTokenLimit = m.optInt("inputTokenLimit", 128000)
                    models.add(
                        DiscoveredModel(
                            id = id,
                            displayName = displayName,
                            description = description,
                            contextWindowTokens = inputTokenLimit,
                            capabilities = setOf(
                                AICapability.TEXT_INPUT,
                                AICapability.TEXT_OUTPUT,
                                AICapability.IMAGE_INPUT,
                                AICapability.CODE_GENERATION,
                                AICapability.STREAMING,
                                AICapability.REASONING
                            )
                        )
                    )
                }
            }

            // Ensure newest models (like gemini-3.8-flash or gemini-2.5-flash) appear at the top
            models.sortWith(compareByDescending<DiscoveredModel> {
                when {
                    it.id.contains("3.8") -> 100
                    it.id.contains("2.5") -> 80
                    it.id.contains("1.5-pro") -> 60
                    it.id.contains("1.5-flash") -> 50
                    else -> 10
                }
            })

            Result.success(models)
        } catch (e: Exception) {
            Result.failure(
                AIException(AIErrorCode.NETWORK_ERROR, "Failed to connect to Gemini: ${e.message}", AiProvider.GEMINI, cause = e)
            )
        }
    }

    suspend fun generateContent(aiRequest: AIRequest, apiKey: String): Result<AIResponse> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val actualModel = aiRequest.model.ifBlank { "gemini-3.8-flash" }
        val url = "$baseUrl/models/$actualModel:generateContent?key=$apiKey"

        try {
            val rootJson = JSONObject().apply {
                // System Instruction
                if (aiRequest.systemInstruction.isNotBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", aiRequest.systemInstruction))
                        })
                    })
                }

                // Contents
                val contents = JSONArray()
                if (aiRequest.messages.isNotEmpty()) {
                    for (msg in aiRequest.messages) {
                        val role = if (msg.role == "assistant") "model" else "user"
                        contents.put(JSONObject().apply {
                            put("role", role)
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", msg.content))
                                for (img in msg.images) {
                                    put(JSONObject().apply {
                                        put("inlineData", JSONObject().apply {
                                            put("mimeType", "image/jpeg")
                                            put("data", img)
                                        })
                                    })
                                }
                            })
                        })
                    }
                } else {
                    contents.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "Please respond"))
                        })
                    })
                }
                put("contents", contents)

                // Generation Config (temperature, maxOutputTokens, reasoning)
                val genConfig = JSONObject()
                aiRequest.temperature?.let { genConfig.put("temperature", it) }
                aiRequest.maxOutputTokens?.let { genConfig.put("maxOutputTokens", it) }
                if (aiRequest.reasoning != null) {
                    val thinkingConfig = JSONObject().apply {
                        val budget = when (aiRequest.reasoning) {
                            "high" -> 8192
                            "medium" -> 4096
                            else -> 2048
                        }
                        put("thinkingBudget", budget)
                    }
                    genConfig.put("thinkingConfig", thinkingConfig)
                }
                if (genConfig.length() > 0) {
                    put("generationConfig", genConfig)
                }
            }

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(requestBody).build()
            val response = client.newCall(request).execute()
            val code = response.code
            val body = response.body?.string() ?: ""
            val duration = System.currentTimeMillis() - startTime

            if (!response.isSuccessful) {
                val errCode = when (code) {
                    400 -> AIErrorCode.BAD_REQUEST
                    401, 403 -> AIErrorCode.INVALID_API_KEY
                    404 -> AIErrorCode.MODEL_NOT_FOUND
                    429 -> AIErrorCode.RATE_LIMITED
                    else -> AIErrorCode.SERVER_ERROR
                }
                return@withContext Result.failure(
                    AIException(errCode, "Gemini API error ($code): $body", AiProvider.GEMINI, code, body)
                )
            }

            val json = JSONObject(body)
            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(
                    AIException(AIErrorCode.CONTENT_LIMIT, "No response candidates returned from Gemini", AiProvider.GEMINI, code, body)
                )
            }

            val cand = candidates.getJSONObject(0)
            val contentObj = cand.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts") ?: JSONArray()
            val textBuilder = StringBuilder()
            var reasoningSummary: String? = null

            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("text")) {
                    textBuilder.append(part.getString("text"))
                }
                if (part.has("thought")) {
                    reasoningSummary = part.optString("thought")
                }
            }

            val finishReason = cand.optString("finishReason", "STOP")
            val usageObj = json.optJSONObject("usageMetadata")
            val usage = usageObj?.let {
                AIUsage(
                    promptTokens = it.optInt("promptTokenCount", 0),
                    completionTokens = it.optInt("candidatesTokenCount", 0),
                    totalTokens = it.optInt("totalTokenCount", 0)
                )
            }

            val citations = mutableListOf<AICitation>()
            val citationMetadata = cand.optJSONObject("citationMetadata")
            val citationSources = citationMetadata?.optJSONArray("citationSources")
            if (citationSources != null) {
                for (k in 0 until citationSources.length()) {
                    val source = citationSources.getJSONObject(k)
                    citations.add(
                        AICitation(
                            title = source.optString("title", "Google Source"),
                            url = source.optString("uri", ""),
                            snippet = null
                        )
                    )
                }
            }

            val debugInfo = AIDebugInfo(
                provider = "Google Gemini",
                adapter = "Gemini Native",
                baseApi = "v1beta",
                model = actualModel,
                requestId = response.header("x-goog-request-id") ?: response.header("request-id"),
                httpStatus = code,
                streamingSupported = true,
                capabilities = setOf(AICapability.TEXT_INPUT, AICapability.TEXT_OUTPUT, AICapability.REASONING),
                requestTimeMs = duration
            )

            Result.success(
                AIResponse(
                    text = textBuilder.toString(),
                    model = actualModel,
                    finishReason = finishReason,
                    usage = usage,
                    citations = citations,
                    reasoningSummary = reasoningSummary,
                    requestId = debugInfo.requestId,
                    debugInfo = debugInfo
                )
            )
        } catch (e: Exception) {
            Result.failure(
                AIException(AIErrorCode.NETWORK_ERROR, "Network error calling Gemini: ${e.message}", AiProvider.GEMINI, cause = e)
            )
        }
    }

    fun streamContent(aiRequest: AIRequest, apiKey: String): Flow<AIStreamEvent> = flow {
        val actualModel = aiRequest.model.ifBlank { "gemini-3.8-flash" }
        val url = "$baseUrl/models/$actualModel:streamGenerateContent?key=$apiKey&alt=sse"

        val rootJson = JSONObject().apply {
            if (aiRequest.systemInstruction.isNotBlank()) {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply { put(JSONObject().put("text", aiRequest.systemInstruction)) })
                })
            }
            val contents = JSONArray()
            for (msg in aiRequest.messages) {
                val role = if (msg.role == "assistant") "model" else "user"
                contents.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().apply { put(JSONObject().put("text", msg.content)) })
                })
            }
            put("contents", contents)
        }

        val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            emit(AIStreamEvent.Error(AIException(AIErrorCode.SERVER_ERROR, "Stream failed: ${response.code}", AiProvider.GEMINI)))
            return@flow
        }

        val reader = BufferedReader(InputStreamReader(response.body!!.byteStream()))
        var line: String? = reader.readLine()
        val fullText = StringBuilder()

        while (line != null) {
            if (line.startsWith("data: ")) {
                val chunkJsonStr = line.removePrefix("data: ").trim()
                if (chunkJsonStr.isNotEmpty() && chunkJsonStr != "[DONE]") {
                    try {
                        val chunkObj = JSONObject(chunkJsonStr)
                        val candidates = chunkObj.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                            if (parts != null) {
                                for (p in 0 until parts.length()) {
                                    val partText = parts.getJSONObject(p).optString("text", "")
                                    if (partText.isNotEmpty()) {
                                        fullText.append(partText)
                                        emit(AIStreamEvent.TextDelta(partText))
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
            line = reader.readLine()
        }

        emit(AIStreamEvent.Done(AIResponse(text = fullText.toString(), model = actualModel)))
    }.flowOn(Dispatchers.IO)
}

class GeminiOpenAICompatibilityAdapter(private val client: OkHttpClient) {
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai"

    suspend fun generateContent(aiRequest: AIRequest, apiKey: String): Result<AIResponse> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val actualModel = aiRequest.model.ifBlank { "gemini-3.8-flash" }
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
                return@withContext Result.failure(
                    AIException(AIErrorCode.BAD_REQUEST, "Gemini OpenAI-compat error ($code): $body", AiProvider.GEMINI, code, body)
                )
            }

            val json = JSONObject(body)
            val choices = json.getJSONArray("choices")
            val text = choices.getJSONObject(0).getJSONObject("message").getString("content")
            val finishReason = choices.getJSONObject(0).optString("finish_reason", "stop")

            val debugInfo = AIDebugInfo(
                provider = "Google Gemini",
                adapter = "Gemini OpenAI Compatibility",
                baseApi = "v1beta/openai",
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
                    debugInfo = debugInfo
                )
            )
        } catch (e: Exception) {
            Result.failure(AIException(AIErrorCode.NETWORK_ERROR, "Network error: ${e.message}", AiProvider.GEMINI, cause = e))
        }
    }

    fun streamContent(aiRequest: AIRequest, apiKey: String): Flow<AIStreamEvent> = flow {
        val actualModel = aiRequest.model.ifBlank { "gemini-3.8-flash" }
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
