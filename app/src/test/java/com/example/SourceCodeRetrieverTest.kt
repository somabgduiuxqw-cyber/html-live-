package com.example

import com.example.data.source.SourceMode
import com.example.data.source.SourcePresets
import com.example.data.source.SourceRetrievalResult
import com.example.ui.editor.CodeSyntaxHighlighter
import com.example.ui.viewmodel.NavDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceCodeRetrieverTest {

    @Test
    fun testSourceRetrievalResult_MetricsAndModeSwitching() {
        val rawHtml = "<!DOCTYPE html>\n<html>\n<head><title>Test</title></head>\n<body><h1>Hello</h1></body>\n</html>"
        val renderedDom = "<!DOCTYPE html><html><head><title>Test</title></head><body><h1>Hello</h1><script>console.log('hydrated');</script></body></html>"

        val result = SourceRetrievalResult(
            url = "https://example.com",
            finalUrl = "https://example.com/index.html",
            httpStatusCode = 200,
            statusMessage = "OK",
            contentType = "text/html; charset=UTF-8",
            detectedLanguage = "html",
            charset = "UTF-8",
            sizeBytes = rawHtml.toByteArray().size.toLong(),
            responseTimeMs = 120L,
            rawHttpSource = rawHtml,
            renderedDomSource = renderedDom,
            headers = listOf("Content-Type" to "text/html; charset=UTF-8"),
            isHttps = true,
            isBinary = false
        )

        assertEquals("https://example.com", result.url)
        assertEquals("https://example.com/index.html", result.finalUrl)
        assertEquals(200, result.httpStatusCode)
        assertEquals("text/html; charset=UTF-8", result.contentType)
        assertTrue(result.isHttps)
        assertFalse(result.isBinary)

        // Raw mode returns raw HTTP source
        assertEquals(rawHtml, result.getSourceForMode(SourceMode.RAW_HTTP))

        // Rendered DOM mode returns rendered DOM source
        assertEquals(renderedDom, result.getSourceForMode(SourceMode.RENDERED_DOM))

        // Line counts
        assertEquals(5, result.lineCount)
        assertEquals(5, result.getLineCount(SourceMode.RAW_HTTP))
        assertEquals(1, result.getLineCount(SourceMode.RENDERED_DOM))
        assertEquals(rawHtml.length, result.characterCount)

        // Formatted size
        assertTrue(result.formattedSize.endsWith("B"))
    }

    @Test
    fun testSourcePresets_ContainStandardTargets() {
        val presets = SourcePresets.PRESETS
        assertTrue("Presets should not be empty", presets.isNotEmpty())

        val examplePreset = presets.find { it.url == "https://example.com" }
        assertNotNull("Should contain example.com preset", examplePreset)

        val jsonPreset = presets.find { it.category == "JSON" }
        assertNotNull("Should contain JSON preset", jsonPreset)
    }

    @Test
    fun testCodeSyntaxHighlighter_HighlightsLanguages() {
        val htmlCode = "<div class=\"container\"><span>Text</span></div>"
        val highlightedHtml = CodeSyntaxHighlighter.highlight(htmlCode, "html")
        assertTrue("Highlighted HTML length matches", highlightedHtml.text == htmlCode)

        val xmlCode = "<root><item id=\"1\">Value</item></root>"
        val highlightedXml = CodeSyntaxHighlighter.highlight(xmlCode, "xml")
        assertTrue("Highlighted XML length matches", highlightedXml.text == xmlCode)

        val cssCode = ".btn { background: #38bdf8; font-size: 14px; }"
        val highlightedCss = CodeSyntaxHighlighter.highlight(cssCode, "css")
        assertTrue("Highlighted CSS length matches", highlightedCss.text == cssCode)

        val jsCode = "const x = 42; function run() { console.log(x); }"
        val highlightedJs = CodeSyntaxHighlighter.highlight(jsCode, "js")
        assertTrue("Highlighted JS length matches", highlightedJs.text == jsCode)

        val jsonCode = "{\n  \"name\": \"HTML Live\",\n  \"version\": 1\n}"
        val highlightedJson = CodeSyntaxHighlighter.highlight(jsonCode, "json")
        assertTrue("Highlighted JSON length matches", highlightedJson.text == jsonCode)
    }

    @Test
    fun testNavDestination_SourceCodeExists() {
        val nav = NavDestination.valueOf("SOURCE_CODE")
        assertEquals("Source Code", nav.title)
        assertEquals("source", nav.iconName)
    }
}
