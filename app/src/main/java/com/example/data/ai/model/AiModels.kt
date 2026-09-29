package com.example.data.ai.model

import com.example.model.AiProvider

enum class AICapability {
    TEXT_INPUT,
    TEXT_OUTPUT,
    IMAGE_INPUT,
    FILE_INPUT,
    CODE_GENERATION,
    REASONING,
    TOOL_CALLING,
    STREAMING,
    STRUCTURED_OUTPUT,
    WEB_SEARCH,
    CITATIONS,
    LONG_CONTEXT
}

enum class AIErrorCode {
    INVALID_API_KEY,
    MODEL_NOT_FOUND,
    MODEL_NOT_AVAILABLE,
    RATE_LIMITED,
    QUOTA_EXCEEDED,
    PERMISSION_DENIED,
    BAD_REQUEST,
    NETWORK_ERROR,
    TIMEOUT,
    SERVER_ERROR,
    UNSUPPORTED_FEATURE,
    CONTENT_LIMIT,
    UNKNOWN_ERROR
}

class AIException(
    val code: AIErrorCode,
    message: String,
    val provider: AiProvider,
    val httpStatus: Int? = null,
    val rawDetails: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

data class AIFileAttachment(
    val name: String,
    val mimeType: String,
    val content: String,
    val sizeBytes: Long = content.length.toLong()
)

data class AIToolDefinition(
    val name: String,
    val description: String,
    val parametersJsonSchema: String
)

data class AIToolCall(
    val id: String,
    val name: String,
    val argumentsJson: String
)

data class AICitation(
    val title: String,
    val url: String,
    val snippet: String? = null
)

data class AIUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = promptTokens + completionTokens
)

data class AIMessage(
    val role: String, // "system", "user", "assistant", "tool"
    val content: String,
    val images: List<String> = emptyList(), // base64 or URLs
    val files: List<AIFileAttachment> = emptyList()
)

data class AIDebugInfo(
    val provider: String,
    val adapter: String,
    val baseApi: String,
    val model: String,
    val requestId: String? = null,
    val httpStatus: Int = 200,
    val streamingSupported: Boolean = true,
    val capabilities: Set<AICapability> = emptySet(),
    val requestTimeMs: Long = 0L,
    val errorSummary: String? = null
)

data class AIRequest(
    val provider: AiProvider,
    val model: String,
    val systemInstruction: String = "",
    val messages: List<AIMessage> = emptyList(),
    val files: List<AIFileAttachment> = emptyList(),
    val images: List<String> = emptyList(),
    val tools: List<AIToolDefinition> = emptyList(),
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null,
    val reasoning: String? = null, // "low", "medium", "high"
    val streaming: Boolean = false,
    val metadata: Map<String, String> = emptyMap(),
    val preferNative: Boolean = true
)

data class AIResponse(
    val text: String,
    val model: String,
    val finishReason: String? = null,
    val usage: AIUsage? = null,
    val citations: List<AICitation> = emptyList(),
    val toolCalls: List<AIToolCall> = emptyList(),
    val reasoningSummary: String? = null,
    val requestId: String? = null,
    val rawProviderMetadata: Map<String, Any> = emptyMap(),
    val debugInfo: AIDebugInfo? = null
)

sealed class AIStreamEvent {
    data class TextDelta(val delta: String) : AIStreamEvent()
    data class ToolCall(val toolCall: AIToolCall) : AIStreamEvent()
    data class Citation(val citation: AICitation) : AIStreamEvent()
    data class ReasoningUpdate(val reasoning: String) : AIStreamEvent()
    data class Error(val error: AIException) : AIStreamEvent()
    data class Done(val response: AIResponse) : AIStreamEvent()
}

data class DiscoveredModel(
    val id: String,
    val displayName: String,
    val description: String = "",
    val capabilities: Set<AICapability> = emptySet(),
    val isDeprecated: Boolean = false,
    val isRetired: Boolean = false,
    val contextWindowTokens: Int = 128000
)
