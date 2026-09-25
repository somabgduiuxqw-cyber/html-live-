package com.example.data.ai

import com.example.model.AiChangeProposal
import com.example.model.AiProvider
import com.example.model.AiSettings
import com.example.model.TeachingStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // Test connection with current provider & key
    suspend fun testConnection(settings: AiSettings): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveKey(settings)
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API Key is missing. Please configure your API key in Settings."))
        }

        try {
            when (settings.provider) {
                AiProvider.GEMINI -> {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
                    val request = Request.Builder().url(url).get().build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        Result.success("Connection successful! Google Gemini is ready.")
                    } else {
                        Result.failure(Exception("Gemini error (${response.code}): ${response.message}"))
                    }
                }
                AiProvider.OPENAI -> {
                    val url = "https://api.openai.com/v1/models"
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .get()
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        Result.success("Connection successful! OpenAI is ready.")
                    } else {
                        Result.failure(Exception("OpenAI error (${response.code}): ${response.message}"))
                    }
                }
                AiProvider.CUSTOM -> {
                    val endpoint = settings.customEndpoint.ifBlank { "https://api.openai.com/v1/chat/completions" }
                    Result.success("Custom endpoint configured: $endpoint")
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Dynamic model discovery
    suspend fun fetchAvailableModels(settings: AiSettings): List<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveKey(settings)
        if (apiKey.isBlank()) return@withContext getDefaultModels(settings.provider)

        try {
            when (settings.provider) {
                AiProvider.GEMINI -> {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
                    val request = Request.Builder().url(url).get().build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string() ?: return@withContext getDefaultModels(settings.provider)
                    val json = JSONObject(body)
                    val modelsArray = json.optJSONArray("models") ?: return@withContext getDefaultModels(settings.provider)

                    val result = mutableListOf<String>()
                    for (i in 0 until modelsArray.length()) {
                        val m = modelsArray.getJSONObject(i)
                        val name = m.getString("name").removePrefix("models/")
                        val supportedMethods = m.optJSONArray("supportedGenerationMethods")
                        var canGenerate = false
                        if (supportedMethods != null) {
                            for (j in 0 until supportedMethods.length()) {
                                if (supportedMethods.getString(j) == "generateContent") canGenerate = true
                            }
                        }
                        if (canGenerate && !name.contains("embedding") && !name.contains("aqa")) {
                            result.add(name)
                        }
                    }
                    if (result.isNotEmpty()) result else getDefaultModels(settings.provider)
                }
                AiProvider.OPENAI -> {
                    val url = "https://api.openai.com/v1/models"
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .get()
                        .build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string() ?: return@withContext getDefaultModels(settings.provider)
                    val json = JSONObject(body)
                    val data = json.optJSONArray("data") ?: return@withContext getDefaultModels(settings.provider)

                    val result = mutableListOf<String>()
                    for (i in 0 until data.length()) {
                        val id = data.getJSONObject(i).getString("id")
                        if (id.startsWith("gpt-") || id.startsWith("o1") || id.startsWith("o3")) {
                            result.add(id)
                        }
                    }
                    if (result.isNotEmpty()) result.sorted() else getDefaultModels(settings.provider)
                }
                AiProvider.CUSTOM -> {
                    listOf(settings.customModel, "gpt-4o", "gpt-4o-mini", "claude-3-5-sonnet")
                }
            }
        } catch (e: Exception) {
            getDefaultModels(settings.provider)
        }
    }

    private fun getDefaultModels(provider: AiProvider): List<String> {
        return when (provider) {
            AiProvider.GEMINI -> listOf("gemini-2.5-flash", "gemini-1.5-pro", "gemini-1.5-flash")
            AiProvider.OPENAI -> listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo")
            AiProvider.CUSTOM -> listOf("gpt-4o", "gpt-4o-mini")
        }
    }

    // Main AI completion method
    suspend fun generateResponse(
        prompt: String,
        systemPrompt: String = "",
        settings: AiSettings,
        projectContextFiles: Map<String, String> = emptyMap()
    ): Result<AiResponseResult> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveKey(settings)
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API Key not found. Please provide an API key in Settings."))
        }

        val enrichedSystemPrompt = buildString {
            append("You are the expert HTML Live coding engine for Android web and game development. ")
            append("User experience level: ${settings.experienceLevel.uppercase()}. ")
            append("Mode: ${settings.mode}. ")
            if (settings.mode == "just_build") {
                append("Provide working code immediately without excessive chit-chat. ")
            } else if (settings.mode == "teach_build") {
                append("Decompose the solution into sequential teaching steps: Step 1 HTML, Step 2 CSS, Step 3 JS with clear educational explanations. ")
            }
            if (systemPrompt.isNotBlank()) {
                append("\n$systemPrompt\n")
            }
            if (projectContextFiles.isNotEmpty()) {
                append("\nCurrent Project Files:\n")
                projectContextFiles.forEach { (name, content) ->
                    append("--- $name ---\n$content\n\n")
                }
            }
            append("\nWhen providing or updating code files, clearly format them inside labeled markdown codeblocks, like:\n")
            append("```html:index.html\n<!-- code -->\n```\n")
            append("```css:style.css\n/* code */\n```\n")
            append("```javascript:script.js\n// code\n```\n")
        }

        try {
            val responseText = when (settings.provider) {
                AiProvider.GEMINI -> callGemini(prompt, enrichedSystemPrompt, settings.selectedModel, apiKey)
                AiProvider.OPENAI -> callOpenAi(prompt, enrichedSystemPrompt, settings.selectedModel, apiKey)
                AiProvider.CUSTOM -> callCustom(prompt, enrichedSystemPrompt, settings.customEndpoint, settings.customModel, apiKey)
            }

            val proposal = parseFileChanges(responseText)
            val teachingSteps = if (settings.mode == "teach_build") parseTeachingSteps(responseText) else null

            Result.success(
                AiResponseResult(
                    content = responseText,
                    changeProposal = proposal,
                    teachingSteps = teachingSteps
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun callGemini(prompt: String, systemPrompt: String, model: String, apiKey: String): String {
        val actualModel = if (model.isBlank()) "gemini-2.5-flash" else model
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$actualModel:generateContent?key=$apiKey"

        val rootJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        if (systemPrompt.isNotBlank()) {
                            put(JSONObject().put("text", "System Instructions:\n$systemPrompt\n\nUser Request:\n$prompt"))
                        } else {
                            put(JSONObject().put("text", prompt))
                        }
                    })
                })
            }
            put("contents", contents)
        }

        val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response from Gemini")

        if (!response.isSuccessful) {
            throw Exception("Gemini API error (${response.code}): $body")
        }

        val json = JSONObject(body)
        val candidates = json.optJSONArray("candidates") ?: throw Exception("No candidates returned from Gemini")
        if (candidates.length() == 0) throw Exception("Empty candidates array from Gemini")

        val content = candidates.getJSONObject(0).getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val textBuilder = StringBuilder()
        for (i in 0 until parts.length()) {
            textBuilder.append(parts.getJSONObject(i).optString("text", ""))
        }
        return textBuilder.toString()
    }

    private fun callOpenAi(prompt: String, systemPrompt: String, model: String, apiKey: String): String {
        val actualModel = if (model.isBlank()) "gpt-4o" else model
        val url = "https://api.openai.com/v1/chat/completions"

        val rootJson = JSONObject().apply {
            put("model", actualModel)
            val messages = JSONArray().apply {
                if (systemPrompt.isNotBlank()) {
                    put(JSONObject().put("role", "system").put("content", systemPrompt))
                }
                put(JSONObject().put("role", "user").put("content", prompt))
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
        val body = response.body?.string() ?: throw Exception("Empty response from OpenAI")

        if (!response.isSuccessful) {
            throw Exception("OpenAI API error (${response.code}): $body")
        }

        val json = JSONObject(body)
        val choices = json.getJSONArray("choices")
        return choices.getJSONObject(0).getJSONObject("message").getString("content")
    }

    private fun callCustom(prompt: String, systemPrompt: String, endpoint: String, model: String, apiKey: String): String {
        val url = endpoint.ifBlank { "https://api.openai.com/v1/chat/completions" }
        val rootJson = JSONObject().apply {
            put("model", model.ifBlank { "gpt-4o" })
            val messages = JSONArray().apply {
                if (systemPrompt.isNotBlank()) {
                    put(JSONObject().put("role", "system").put("content", systemPrompt))
                }
                put(JSONObject().put("role", "user").put("content", prompt))
            }
            put("messages", messages)
        }

        val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
        val builder = Request.Builder().url(url).post(requestBody)
        if (apiKey.isNotBlank()) {
            builder.addHeader("Authorization", "Bearer $apiKey")
        }
        val response = client.newCall(builder.build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response from endpoint")

        if (!response.isSuccessful) {
            throw Exception("Provider error (${response.code}): $body")
        }

        val json = JSONObject(body)
        val choices = json.optJSONArray("choices")
        return if (choices != null && choices.length() > 0) {
            choices.getJSONObject(0).getJSONObject("message").getString("content")
        } else {
            body
        }
    }

    private fun getEffectiveKey(settings: AiSettings): String {
        return when (settings.provider) {
            AiProvider.GEMINI -> {
                if (settings.geminiApiKey.isNotBlank()) settings.geminiApiKey
                else runCatching {
                    val field = Class.forName("com.example.BuildConfig").getField("GEMINI_API_KEY")
                    field.get(null) as? String ?: ""
                }.getOrDefault("")
            }
            AiProvider.OPENAI -> settings.openAiApiKey
            AiProvider.CUSTOM -> settings.customApiKey
        }
    }

    // Parses codeblocks like ```html:index.html ... ``` into structured files
    private fun parseFileChanges(text: String): AiChangeProposal? {
        val files = mutableMapOf<String, String>()
        val regex = Regex("```(?:html|css|js|javascript|json)?(?::|\\s+)?([a-zA-Z0-9_.-]+)?\\s*\\n([\\s\\S]*?)```")

        for (match in regex.findAll(text)) {
            val explicitName = match.groups[1]?.value?.trim()
            val content = match.groups[2]?.value?.trim() ?: continue

            val targetFile = when {
                explicitName != null && explicitName.isNotBlank() && explicitName.contains(".") -> explicitName
                content.contains("<!DOCTYPE") || content.contains("<html") -> "index.html"
                content.contains("margin:") || content.contains("padding:") || content.contains("display:") -> "style.css"
                content.contains("function") || content.contains("const ") || content.contains("let ") -> "script.js"
                else -> null
            }

            if (targetFile != null) {
                files[targetFile] = content
            }
        }

        if (files.isEmpty()) return null

        return AiChangeProposal(
            title = "Code Updates Generated",
            description = "Updates ready for: " + files.keys.joinToString(", "),
            files = files,
            explanation = "AI generated functional code updates based on your request."
        )
    }

    private fun parseTeachingSteps(text: String): List<TeachingStep>? {
        val stepRegex = Regex("(?:Step\\s*(\\d+)[—:\\s]+([^\\n]+))([\\s\\S]*?)(?=(?:Step\\s*\\d+)|$)")
        val matches = stepRegex.findAll(text).toList()
        if (matches.isEmpty()) return null

        return matches.mapIndexed { idx, m ->
            val num = m.groups[1]?.value?.toIntOrNull() ?: (idx + 1)
            val title = m.groups[2]?.value?.trim() ?: "Step $num"
            val explanation = m.groups[3]?.value?.trim() ?: ""
            TeachingStep(stepNumber = num, title = title, explanation = explanation)
        }
    }
}

data class AiResponseResult(
    val content: String,
    val changeProposal: AiChangeProposal?,
    val teachingSteps: List<TeachingStep>?
)
