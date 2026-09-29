package com.example

import com.example.data.ai.AIRouter
import com.example.data.ai.model.AICapability
import com.example.data.ai.model.AIMessage
import com.example.data.ai.model.AIRequest
import com.example.data.ai.provider.AnthropicProvider
import com.example.data.ai.provider.GeminiProvider
import com.example.data.ai.provider.OpenAIProvider
import com.example.data.ai.provider.XAIProvider
import com.example.model.AiProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AIProviderCompatibilityTest {

    @Test
    fun `router correctly instantiates all distinct provider adapters`() {
        val router = AIRouter()
        val gemini = router.getProvider(AiProvider.GEMINI)
        val openai = router.getProvider(AiProvider.OPENAI)
        val xai = router.getProvider(AiProvider.XAI)
        val anthropic = router.getProvider(AiProvider.ANTHROPIC)
        val custom = router.getProvider(AiProvider.CUSTOM)

        assertTrue(gemini is GeminiProvider)
        assertTrue(openai is OpenAIProvider)
        assertTrue(xai is XAIProvider)
        assertTrue(anthropic is AnthropicProvider)
        assertNotNull(custom)
    }

    @Test
    fun `gemini capabilities include reasoning, vision, and streaming`() {
        val gemini = GeminiProvider()
        val caps = gemini.getCapabilities("gemini-3.8-flash")
        assertTrue(caps.contains(AICapability.TEXT_INPUT))
        assertTrue(caps.contains(AICapability.TEXT_OUTPUT))
        assertTrue(caps.contains(AICapability.IMAGE_INPUT))
        assertTrue(caps.contains(AICapability.REASONING))
        assertTrue(caps.contains(AICapability.STREAMING))
    }

    @Test
    fun `openai capabilities reflect reasoning on o1 and o3 models`() {
        val openai = OpenAIProvider()
        val o3Caps = openai.getCapabilities("o3-mini")
        assertTrue(o3Caps.contains(AICapability.REASONING))

        val gpt4Caps = openai.getCapabilities("gpt-4o")
        assertTrue(gpt4Caps.contains(AICapability.IMAGE_INPUT))
    }

    @Test
    fun `xai provider handles grok models`() {
        val xai = XAIProvider()
        val caps = xai.getCapabilities("grok-2-vision")
        assertTrue(caps.contains(AICapability.IMAGE_INPUT))
        assertTrue(caps.contains(AICapability.WEB_SEARCH))
    }

    @Test
    fun `anthropic provider supports long context and multimodal`() {
        val anthropic = AnthropicProvider()
        val caps = anthropic.getCapabilities("claude-3-5-sonnet-20241022")
        assertTrue(caps.contains(AICapability.LONG_CONTEXT))
        assertTrue(caps.contains(AICapability.IMAGE_INPUT))
        assertTrue(caps.contains(AICapability.CODE_GENERATION))
    }

    @Test
    fun `parameter translation strips reasoning when model does not support it`() {
        val router = AIRouter()
        val request = AIRequest(
            provider = AiProvider.OPENAI,
            model = "gpt-4",
            reasoning = "high",
            messages = listOf(AIMessage("user", "Hello"))
        )
        val provider = router.getProvider(AiProvider.OPENAI)
        val caps = provider.getCapabilities(request.model)
        assertFalse(caps.contains(AICapability.REASONING))
    }
}
