package com.example.data.source

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

object SourceCodeRetriever {

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 HTML-Live-Inspector/1.0"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    suspend fun fetchRawHttp(inputUrl: String): SourceRetrievalResult = withContext(Dispatchers.IO) {
        val targetUrl = normalizeUrl(inputUrl)
        val startTime = System.currentTimeMillis()

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,text/css,application/json,text/javascript,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val elapsedMs = System.currentTimeMillis() - startTime
            val finalUrl = response.request.url.toString()
            val statusCode = response.code
            val statusMessage = response.message.ifBlank { if (statusCode in 200..299) "OK" else "Status $statusCode" }

            val rawHeaders = mutableListOf<Pair<String, String>>()
            val headers = response.headers
            for (i in 0 until headers.size) {
                rawHeaders.add(headers.name(i) to headers.value(i))
            }
            val cookies = response.headers("Set-Cookie")

            val isHttps = finalUrl.startsWith("https://", ignoreCase = true)
            val tlsVersion = response.handshake?.tlsVersion?.javaName
            val cipherSuite = response.handshake?.cipherSuite?.javaName

            val responseBody = response.body
            val contentTypeHeader = response.header("Content-Type") ?: "text/html"
            val mediaType = responseBody?.contentType()
            val mimeType = (mediaType?.type + "/" + mediaType?.subtype).lowercase()

            val charsetName = mediaType?.charset(Charsets.UTF_8)?.name() ?: "UTF-8"
            val charset = try {
                Charset.forName(charsetName)
            } catch (_: Exception) {
                Charsets.UTF_8
            }

            val isBinary = isBinaryMimeType(mimeType)
            val sizeBytes = responseBody?.contentLength().takeIf { it != null && it >= 0 }
                ?: 0L

            if (isBinary) {
                val byteCount = responseBody?.bytes()?.size?.toLong() ?: sizeBytes
                return@withContext SourceRetrievalResult(
                    url = targetUrl,
                    finalUrl = finalUrl,
                    httpStatusCode = statusCode,
                    statusMessage = statusMessage,
                    contentType = contentTypeHeader,
                    detectedLanguage = "binary",
                    charset = charsetName,
                    sizeBytes = byteCount,
                    responseTimeMs = elapsedMs,
                    rawHttpSource = "/* Binary Response: $contentTypeHeader */\n/* Size: $byteCount bytes */\n\n[Binary content cannot be rendered as text source code.]",
                    headers = rawHeaders,
                    cookies = cookies,
                    isHttps = isHttps,
                    tlsVersion = tlsVersion,
                    cipherSuite = cipherSuite,
                    isBinary = true
                )
            }

            val bodyBytes = responseBody?.bytes() ?: ByteArray(0)
            val actualSizeBytes = if (sizeBytes > 0) sizeBytes else bodyBytes.size.toLong()
            var bodyText = String(bodyBytes, charset)

            val detectedLanguage = detectLanguage(mimeType, finalUrl, bodyText)

            // Pretty format JSON if applicable
            if (detectedLanguage == "json") {
                bodyText = formatJsonSafely(bodyText)
            }

            SourceRetrievalResult(
                url = targetUrl,
                finalUrl = finalUrl,
                httpStatusCode = statusCode,
                statusMessage = statusMessage,
                contentType = contentTypeHeader,
                detectedLanguage = detectedLanguage,
                charset = charsetName,
                sizeBytes = actualSizeBytes,
                responseTimeMs = elapsedMs,
                rawHttpSource = bodyText,
                headers = rawHeaders,
                cookies = cookies,
                isHttps = isHttps,
                tlsVersion = tlsVersion,
                cipherSuite = cipherSuite,
                isBinary = false
            )
        } catch (e: Exception) {
            val elapsedMs = System.currentTimeMillis() - startTime
            SourceRetrievalResult(
                url = targetUrl,
                finalUrl = targetUrl,
                httpStatusCode = 0,
                statusMessage = "Network Request Failed",
                contentType = "text/plain",
                detectedLanguage = "text",
                charset = "UTF-8",
                sizeBytes = 0L,
                responseTimeMs = elapsedMs,
                rawHttpSource = "",
                isHttps = targetUrl.startsWith("https://", ignoreCase = true),
                isBinary = false,
                errorMessage = e.localizedMessage ?: "Unable to fetch webpage. Please check internet connection or URL validity."
            )
        }
    }

    suspend fun fetchRenderedDom(context: Context, url: String): String? = withTimeoutOrNull(15000L) {
        val targetUrl = normalizeUrl(url)
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<String?> { cont ->
                var hasResumed = false
                val webView = WebView(context.applicationContext)
                val handler = Handler(Looper.getMainLooper())

                fun finishWithResult(result: String?) {
                    if (!hasResumed) {
                        hasResumed = true
                        try {
                            webView.stopLoading()
                            webView.destroy()
                        } catch (_: Exception) {}
                        if (cont.isActive) {
                            cont.resume(result)
                        }
                    }
                }

                // Safety timeout fallback
                val timeoutRunnable = Runnable {
                    try {
                        webView.evaluateJavascript("document.documentElement ? document.documentElement.outerHTML : ''") { raw ->
                            finishWithResult(unescapeJsString(raw))
                        }
                    } catch (_: Exception) {
                        finishWithResult(null)
                    }
                }
                handler.postDelayed(timeoutRunnable, 10000L)

                cont.invokeOnCancellation {
                    handler.removeCallbacks(timeoutRunnable)
                    try {
                        webView.stopLoading()
                        webView.destroy()
                    } catch (_: Exception) {}
                }

                try {
                    webView.settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        loadsImagesAutomatically = false
                        userAgentString = USER_AGENT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }

                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, finishedUrl: String?) {
                            super.onPageFinished(view, finishedUrl)
                            handler.postDelayed({
                                view?.evaluateJavascript("document.documentElement ? document.documentElement.outerHTML : ''") { rawResult ->
                                    handler.removeCallbacks(timeoutRunnable)
                                    val cleaned = unescapeJsString(rawResult)
                                    finishWithResult(cleaned)
                                }
                            }, 400L) // Small debounce for JS framework hydration
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            // Allow retrieval of whatever HTML was received even on error
                        }
                    }

                    webView.webChromeClient = WebChromeClient()
                    webView.loadUrl(targetUrl)
                } catch (e: Exception) {
                    handler.removeCallbacks(timeoutRunnable)
                    finishWithResult(null)
                }
            }
        }
    }

    private fun unescapeJsString(raw: String?): String? {
        if (raw.isNullOrBlank() || raw == "null" || raw == "\"\"") return null
        return try {
            val tokener = JSONTokener(raw)
            tokener.nextValue().toString()
        } catch (_: Exception) {
            var s = raw
            if (s.startsWith("\"") && s.endsWith("\"") && s.length >= 2) {
                s = s.substring(1, s.length - 1)
            }
            s.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\")
        }
    }

    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        return if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            "https://$trimmed"
        } else {
            trimmed
        }
    }

    private fun isBinaryMimeType(mime: String): Boolean {
        return mime.startsWith("image/") ||
                mime.startsWith("audio/") ||
                mime.startsWith("video/") ||
                mime == "application/pdf" ||
                mime == "application/zip" ||
                mime == "application/octet-stream" ||
                mime == "application/gzip" ||
                mime == "application/x-tar"
    }

    private fun detectLanguage(mime: String, url: String, content: String): String {
        return when {
            mime.contains("json") || url.endsWith(".json", ignoreCase = true) -> "json"
            mime.contains("css") || url.endsWith(".css", ignoreCase = true) -> "css"
            mime.contains("javascript") || mime.contains("ecmascript") || url.endsWith(".js", ignoreCase = true) -> "js"
            mime.contains("xml") || url.endsWith(".xml", ignoreCase = true) -> "xml"
            mime.contains("html") || content.startsWith("<!DOCTYPE", ignoreCase = true) || content.contains("<html", ignoreCase = true) -> "html"
            mime.startsWith("text/plain") -> "text"
            else -> "html"
        }
    }

    private fun formatJsonSafely(jsonStr: String): String {
        val trimmed = jsonStr.trim()
        return try {
            if (trimmed.startsWith("{")) {
                JSONObject(trimmed).toString(2)
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed).toString(2)
            } else {
                jsonStr
            }
        } catch (_: Exception) {
            jsonStr
        }
    }
}
