package com.example.data.ai.provider

import com.example.data.ai.model.AICapability
import com.example.data.ai.model.AIRequest
import com.example.data.ai.model.AIResponse
import com.example.data.ai.model.AIStreamEvent
import com.example.data.ai.model.DiscoveredModel
import com.example.model.AiProvider
import kotlinx.coroutines.flow.Flow

interface AIProvider {
    val providerType: AiProvider

    suspend fun execute(request: AIRequest, apiKey: String): Result<AIResponse>

    suspend fun executeStream(request: AIRequest, apiKey: String): Flow<AIStreamEvent>

    suspend fun discoverModels(apiKey: String): Result<List<DiscoveredModel>>

    fun getCapabilities(model: String): Set<AICapability>

    fun getAdapterName(request: AIRequest): String
}
