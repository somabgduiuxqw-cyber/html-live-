package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.ConsoleMessage as WebKitConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.ConsoleLevel
import com.example.model.ConsoleMessage
import com.example.model.ProjectFile
import com.example.model.ViewportPreset

class ConsoleBridge(private val onLog: (ConsoleMessage) -> Unit) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun log(level: String, message: String, source: String, line: Int, stack: String?) {
        mainHandler.post {
            val lvl = when (level.uppercase()) {
                "WARN", "WARNING" -> ConsoleLevel.WARN
                "ERROR" -> ConsoleLevel.ERROR
                "INFO" -> ConsoleLevel.INFO
                else -> ConsoleLevel.LOG
            }
            onLog(
                ConsoleMessage(
                    level = lvl,
                    message = message,
                    sourceFile = source.substringAfterLast('/'),
                    lineNumber = if (line <= 0) 1 else line,
                    stackTrace = stack
                )
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LivePreviewView(
    files: List<ProjectFile>,
    consoleMessages: List<ConsoleMessage>,
    onConsoleMessage: (ConsoleMessage) -> Unit,
    onOpenConsole: () -> Unit,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {}
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var selectedPreset by remember { mutableStateOf(ViewportPreset.RESPONSIVE) }
    var isLoading by remember { mutableStateOf(false) }
    var loadProgress by remember { mutableIntStateOf(100) }

    val errorCount = remember(consoleMessages) {
        consoleMessages.count { it.level == ConsoleLevel.ERROR }
    }
    val warnCount = remember(consoleMessages) {
        consoleMessages.count { it.level == ConsoleLevel.WARN }
    }

    // Prepare complete bundled HTML document
    val bundledHtml = remember(files) {
        bundleProjectFiles(files)
    }

    // Refresh webview when files change
    LaunchedEffect(bundledHtml) {
        webViewRef?.let { wv ->
            wv.loadDataWithBaseURL("https://htmllive.local/", bundledHtml, "text/html", "UTF-8", null)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        // Browser Controls Bar
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (webViewRef?.canGoBack() == true) webViewRef?.goBack() },
                        modifier = Modifier.size(36.dp).testTag("preview_btn_back")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.LightGray)
                    }
                    IconButton(
                        onClick = { if (webViewRef?.canGoForward() == true) webViewRef?.goForward() },
                        modifier = Modifier.size(36.dp).testTag("preview_btn_forward")
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Forward", tint = Color.LightGray)
                    }
                    IconButton(
                        onClick = {
                            webViewRef?.loadDataWithBaseURL("https://htmllive.local/", bundledHtml, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.size(36.dp).testTag("preview_btn_reload")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color(0xFF38BDF8))
                    }
                    IconButton(
                        onClick = {
                            webViewRef?.loadDataWithBaseURL("https://htmllive.local/", bundledHtml, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.size(36.dp).testTag("preview_btn_home")
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.LightGray)
                    }

                    // Simulated URL / Status pill
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "localhost:8080/index.html",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                    }

                    // Console Trigger Badge
                    IconButton(
                        onClick = onOpenConsole,
                        modifier = Modifier.size(36.dp).testTag("preview_btn_console")
                    ) {
                        BadgedBox(
                            badge = {
                                if (errorCount > 0) {
                                    Badge(containerColor = Color(0xFFEF4444)) {
                                        Text("$errorCount", color = Color.White, fontSize = 9.sp)
                                    }
                                } else if (warnCount > 0) {
                                    Badge(containerColor = Color(0xFFF59E0B)) {
                                        Text("$warnCount", color = Color.Black, fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = "Console", tint = if (errorCount > 0) Color(0xFFEF4444) else Color(0xFF38BDF8))
                        }
                    }

                    // Fullscreen toggle
                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier.size(36.dp).testTag("preview_btn_fullscreen")
                    ) {
                        Icon(
                            if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White
                        )
                    }
                }

                // Loading bar
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = Color(0xFF38BDF8),
                        trackColor = Color.Transparent
                    )
                }

                // Responsive Viewport Selector Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ViewportPreset.values().forEach { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = { selectedPreset = preset },
                            label = { Text(preset.title, fontSize = 11.sp) },
                            leadingIcon = {
                                when (preset) {
                                    ViewportPreset.RESPONSIVE -> Icon(Icons.Default.StayCurrentPortrait, null, Modifier.size(14.dp))
                                    ViewportPreset.SMALL_PHONE -> Icon(Icons.Default.PhoneAndroid, null, Modifier.size(14.dp))
                                    ViewportPreset.LARGE_PHONE -> Icon(Icons.Default.PhoneAndroid, null, Modifier.size(14.dp))
                                    ViewportPreset.TABLET -> Icon(Icons.Default.Tablet, null, Modifier.size(14.dp))
                                    ViewportPreset.DESKTOP -> Icon(Icons.Default.DesktopWindows, null, Modifier.size(14.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF38BDF8),
                                selectedLabelColor = Color(0xFF0F172A),
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }
            }
        }

        // Viewport Frame (Center with simulated device frame if preset is selected)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF090D16)),
            contentAlignment = Alignment.Center
        ) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight

            val targetModifier = if (selectedPreset == ViewportPreset.RESPONSIVE) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .width(selectedPreset.widthDp.dp.coerceAtMost(availableWidth))
                    .height(selectedPreset.heightDp.dp.coerceAtMost(availableHeight))
                    .border(2.dp, Color(0xFF334155), MaterialTheme.shapes.small)
                    .clip(MaterialTheme.shapes.small)
            }

            Box(modifier = targetModifier) {
                AndroidView(
                    factory = { ctx ->
                        createConfiguredWebView(ctx, onConsoleMessage).also { wv ->
                            webViewRef = wv
                            wv.loadDataWithBaseURL("https://htmllive.local/", bundledHtml, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.fillMaxSize().testTag("live_webview_container")
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createConfiguredWebView(
    context: Context,
    onConsoleMessage: (ConsoleMessage) -> Unit
): WebView {
    val webView = WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_NO_CACHE
            mediaPlaybackRequiresUserGesture = false
        }

        // Bridge for direct JS-to-Kotlin console routing
        addJavascriptInterface(ConsoleBridge(onConsoleMessage), "HtmlLiveBridge")

        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(msg: WebKitConsoleMessage?): Boolean {
                if (msg != null) {
                    val lvl = when (msg.messageLevel()) {
                        WebKitConsoleMessage.MessageLevel.ERROR -> ConsoleLevel.ERROR
                        WebKitConsoleMessage.MessageLevel.WARNING -> ConsoleLevel.WARN
                        WebKitConsoleMessage.MessageLevel.LOG -> ConsoleLevel.LOG
                        else -> ConsoleLevel.INFO
                    }
                    onConsoleMessage(
                        ConsoleMessage(
                            level = lvl,
                            message = msg.message() ?: "",
                            sourceFile = msg.sourceId()?.substringAfterLast('/') ?: "script.js",
                            lineNumber = msg.lineNumber()
                        )
                    )
                }
                return true
            }
        }

        webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                onConsoleMessage(
                    ConsoleMessage(
                        level = ConsoleLevel.ERROR,
                        message = "Resource error: $description ($errorCode)",
                        sourceFile = failingUrl ?: "network",
                        lineNumber = 0
                    )
                )
            }
        }
    }
    return webView
}

// Bundles HTML, CSS, and JS into a unified self-contained document with the console interception script
private fun bundleProjectFiles(files: List<ProjectFile>): String {
    val htmlFile = files.find { it.name.equals("index.html", ignoreCase = true) }?.content
        ?: "<!DOCTYPE html><html><body><h1>No index.html</h1></body></html>"
    val cssFile = files.find { it.name.equals("style.css", ignoreCase = true) }?.content ?: ""
    val jsFile = files.find { it.name.equals("script.js", ignoreCase = true) }?.content ?: ""

    val consoleScript = """
<script>
(function() {
  function send(lvl, msg, src, line, stack) {
    if (window.HtmlLiveBridge) {
      window.HtmlLiveBridge.log(lvl, String(msg), src || 'script.js', line || 1, stack || '');
    }
  }
  const oldLog = console.log;
  console.log = function() {
    send('LOG', Array.from(arguments).join(' '));
    oldLog.apply(console, arguments);
  };
  const oldWarn = console.warn;
  console.warn = function() {
    send('WARN', Array.from(arguments).join(' '));
    oldWarn.apply(console, arguments);
  };
  const oldErr = console.error;
  console.error = function() {
    send('ERROR', Array.from(arguments).join(' '));
    oldErr.apply(console, arguments);
  };
  const oldInfo = console.info;
  console.info = function() {
    send('INFO', Array.from(arguments).join(' '));
    oldInfo.apply(console, arguments);
  };
  window.onerror = function(msg, url, line, col, error) {
    send('ERROR', msg, url, line, error ? error.stack : '');
  };
  window.addEventListener('unhandledrejection', function(event) {
    send('ERROR', 'Unhandled Rejection: ' + (event.reason ? (event.reason.message || event.reason) : 'Unknown'), 'script.js', 1);
  });
})();
</script>
"""

    var doc = htmlFile

    // Inject console script right after <head> or at start
    doc = if (doc.contains("<head>", ignoreCase = true)) {
        doc.replaceFirst("<head>", "<head>\n$consoleScript", ignoreCase = true)
    } else {
        "$consoleScript\n$doc"
    }

    // Inline CSS
    val styleTag = "\n<style>\n$cssFile\n</style>\n"
    doc = if (doc.contains("href=\"style.css\"", ignoreCase = true) || doc.contains("href='style.css'", ignoreCase = true)) {
        doc.replace(Regex("<link[^>]+href=[\"']style\\.css[\"'][^>]*>", RegexOption.IGNORE_CASE), styleTag)
    } else if (doc.contains("</head>", ignoreCase = true)) {
        doc.replaceFirst("</head>", "$styleTag</head>", ignoreCase = true)
    } else {
        doc + styleTag
    }

    // Inline JS
    val scriptTag = "\n<script>\n$jsFile\n</script>\n"
    doc = if (doc.contains("src=\"script.js\"", ignoreCase = true) || doc.contains("src='script.js'", ignoreCase = true)) {
        doc.replace(Regex("<script[^>]+src=[\"']script\\.js[\"'][^>]*></script>", RegexOption.IGNORE_CASE), scriptTag)
    } else if (doc.contains("</body>", ignoreCase = true)) {
        doc.replaceFirst("</body>", "$scriptTag</body>", ignoreCase = true)
    } else {
        doc + scriptTag
    }

    return doc
}
