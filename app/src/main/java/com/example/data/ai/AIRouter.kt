package com.example.data.ai

import com.example.data.ai.model.AICapability
import com.example.data.ai.model.AIDebugInfo
import com.example.data.ai.model.AIErrorCode
import com.example.data.ai.model.AIException
import com.example.data.ai.model.AIRequest
import com.example.data.ai.model.AIResponse
import com.example.data.ai.model.AIStreamEvent
import com.example.data.ai.model.DiscoveredModel
import com.example.data.ai.provider.AIProvider
import com.example.data.ai.provider.AnthropicProvider
import com.example.data.ai.provider.CustomProvider
import com.example.data.ai.provider.GeminiProvider
import com.example.data.ai.provider.OpenAIProvider
import com.example.data.ai.provider.XAIProvider
import com.example.model.AiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AIRouter {
    val geminiProvider = GeminiProvider()
    val openAiProvider = OpenAIProvider()
    val xaiProvider = XAIProvider()
    val anthropicProvider = AnthropicProvider()
    val customProvider = CustomProvider()

    private val _lastDebugInfo = MutableStateFlow<AIDebugInfo?>(null)
    val lastDebugInfo: StateFlow<AIDebugInfo?> = _lastDebugInfo.asStateFlow()

    fun getProvider(type: AiProvider): AIProvider {
        return when (type) {
            AiProvider.GEMINI -> geminiProvider
            AiProvider.OPENAI -> openAiProvider
            AiProvider.XAI -> xaiProvider
            AiProvider.ANTHROPIC -> anthropicProvider
            AiProvider.CUSTOM -> customProvider
        }
    }

    suspend fun execute(request: AIRequest, apiKey: String): Result<AIResponse> {
        val provider = getProvider(request.provider)
        val sanitizedRequest = translateParameters(request, provider)

        val result = provider.execute(sanitizedRequest, apiKey)
        if (result.isSuccess) {
            val response = result.getOrNull()
            if (response?.debugInfo != null) {
                _lastDebugInfo.value = response.debugInfo
            }
        } else {
            val ex = result.exceptionOrNull()
            _lastDebugInfo.value = AIDebugInfo(
                provider = request.provider.displayName,
                adapter = provider.getAdapterName(request),
                baseApi = request.provider.name.lowercase(),
                model = request.model,
                httpStatus = (ex as? AIException)?.httpStatus ?: 500,
                streamingSupported = true,
                capabilities = provider.getCapabilities(request.model),
                errorSummary = ex?.message
            )
        }
        return result
    }

    suspend fun executeStream(request: AIRequest, apiKey: String): Flow<AIStreamEvent> {
        val provider = getProvider(request.provider)
        val sanitizedRequest = translateParameters(request, provider)
        return provider.executeStream(sanitizedRequest, apiKey)
    }

    suspend fun discoverModels(providerType: AiProvider, apiKey: String): Result<List<DiscoveredModel>> {
        val provider = getProvider(providerType)
        return provider.discoverModels(apiKey)
    }

    private fun translateParameters(request: AIRequest, provider: AIProvider): AIRequest {
        val caps = provider.getCapabilities(request.model)

        // Drop reasoning if not supported
        val reasoning = if (caps.contains(AICapability.REASONING)) request.reasoning else null

        // Drop images if image input not supported
        val images = if (caps.contains(AICapability.IMAGE_INPUT)) request.images else emptyList()

        return request.copy(
            reasoning = reasoning,
            images = images
        )
    }
}
