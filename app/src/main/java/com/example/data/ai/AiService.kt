package com.example.data.ai

import com.example.data.ai.model.AIMessage
import com.example.data.ai.model.AIRequest
import com.example.data.ai.model.AIResponse
import com.example.model.AiChangeProposal
import com.example.model.AiProvider
import com.example.model.AiSettings
import com.example.model.TeachingStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class AiService {
    val router = AIRouter()

    val lastDebugInfo: StateFlow<com.example.data.ai.model.AIDebugInfo?> = router.lastDebugInfo

    // Test connection with current provider & key
    suspend fun testConnection(settings: AiSettings): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveKey(settings)
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API Key is missing for ${settings.provider.displayName}. Please configure your API key in Settings."))
        }

        try {
            val discoveryResult = router.discoverModels(settings.provider, apiKey)
            if (discoveryResult.isSuccess) {
                val models = discoveryResult.getOrThrow()
                Result.success("Connection successful! ${settings.provider.displayName} is ready. (${models.size} models accessible)")
            } else {
                val err = discoveryResult.exceptionOrNull()
                Result.failure(Exception("${settings.provider.displayName} connection error: ${err?.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Dynamic model discovery for account-specific models
    suspend fun fetchAvailableModels(settings: AiSettings): List<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveKey(settings)
        if (apiKey.isBlank()) return@withContext getDefaultModels(settings.provider)

        try {
            val discoveryResult = router.discoverModels(settings.provider, apiKey)
            if (discoveryResult.isSuccess) {
                val models = discoveryResult.getOrThrow().map { it.id }
                if (models.isNotEmpty()) models else getDefaultModels(settings.provider)
            } else {
                getDefaultModels(settings.provider)
            }
        } catch (e: Exception) {
            getDefaultModels(settings.provider)
        }
    }

    fun getDefaultModels(provider: AiProvider): List<String> {
        return when (provider) {
            AiProvider.GEMINI -> listOf("gemini-3.8-flash", "gemini-2.5-flash", "gemini-1.5-pro", "gemini-1.5-flash")
            AiProvider.OPENAI -> listOf("gpt-4o", "gpt-4o-mini", "o3-mini", "o1", "gpt-4-turbo")
            AiProvider.XAI -> listOf("grok-2", "grok-beta")
            AiProvider.ANTHROPIC -> listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022", "claude-3-opus-20240229")
            AiProvider.CUSTOM -> listOf("gpt-4o", "gpt-4o-mini", "claude-3-5-sonnet")
        }
    }

    // Main AI completion method using internal AIRequest / AIResponse format
    suspend fun generateResponse(
        prompt: String,
        systemPrompt: String = "",
        settings: AiSettings,
        projectContextFiles: Map<String, String> = emptyMap(),
        images: List<String> = emptyList()
    ): Result<AiResponseResult> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveKey(settings)
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API Key not found for ${settings.provider.displayName}. Please provide an API key in Settings."))
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

        val internalRequest = AIRequest(
            provider = settings.provider,
            model = settings.selectedModel,
            systemInstruction = enrichedSystemPrompt,
            messages = listOf(
                AIMessage(
                    role = "user",
                    content = prompt,
                    images = images
                )
            ),
            reasoning = if (settings.enableThinking) settings.reasoningEffort else null,
            preferNative = settings.preferNativeAdapter,
            metadata = mapOf("customEndpoint" to settings.customEndpoint)
        )

        try {
            val responseResult = router.execute(internalRequest, apiKey)
            if (responseResult.isFailure) {
                return@withContext Result.failure(responseResult.exceptionOrNull() ?: Exception("Unknown AI error"))
            }

            val aiResponse = responseResult.getOrThrow()
            val proposal = parseFileChanges(aiResponse.text)
            val teachingSteps = if (settings.mode == "teach_build") parseTeachingSteps(aiResponse.text) else null

            Result.success(
                AiResponseResult(
                    content = aiResponse.text,
                    changeProposal = proposal,
                    teachingSteps = teachingSteps,
                    debugInfo = aiResponse.debugInfo
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getEffectiveKey(settings: AiSettings): String {
        return when (settings.provider) {
            AiProvider.GEMINI -> {
                if (settings.geminiApiKey.isNotBlank()) settings.geminiApiKey
                else runCatching {
                    val field = Class.forName("com.example.BuildConfig").getField("GEMINI_API_KEY")
                    field.get(null) as? String ?: ""
                }.getOrDefault("")
            }
            AiProvider.OPENAI -> settings.openAiApiKey
            AiProvider.XAI -> settings.xaiApiKey
            AiProvider.ANTHROPIC -> settings.anthropicApiKey
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
    val teachingSteps: List<TeachingStep>?,
    val debugInfo: com.example.data.ai.model.AIDebugInfo? = null
)
