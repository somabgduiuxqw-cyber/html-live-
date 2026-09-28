package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// -------------------------------------------------------------
// 1. HTTP Request Tool (Real Network Calls via OkHttp)
// -------------------------------------------------------------
@Composable
fun HttpRequestModal(
    onDismiss: () -> Unit,
    onInjectDataIntoProject: (String) -> Unit
) {
    var url by remember { mutableStateOf("https://jsonplaceholder.typicode.com/posts/1") }
    var method by remember { mutableStateOf("GET") }
    var responseBody by remember { mutableStateOf("") }
    var responseStatus by remember { mutableStateOf("") }
    var responseTimeMs by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Http, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("HTTP Request Tester", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("GET", "POST", "PUT", "DELETE").forEach { m ->
                        FilterChip(
                            selected = method == m,
                            onClick = { method = m },
                            label = { Text(m, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Request URL", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            val client = OkHttpClient.Builder()
                                .connectTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(10, TimeUnit.SECONDS)
                                .build()

                            val startTime = System.currentTimeMillis()
                            try {
                                val reqBuilder = Request.Builder().url(url)
                                if (method == "POST") {
                                    reqBuilder.post("{}".toRequestBody())
                                } else if (method == "DELETE") {
                                    reqBuilder.delete()
                                } else {
                                    reqBuilder.get()
                                }

                                withContext(Dispatchers.IO) {
                                    client.newCall(reqBuilder.build()).execute().use { resp ->
                                        responseTimeMs = System.currentTimeMillis() - startTime
                                        responseStatus = "${resp.code} ${resp.message}"
                                        responseBody = resp.body?.string() ?: "(Empty Body)"
                                    }
                                }
                            } catch (e: Exception) {
                                responseStatus = "Error"
                                responseBody = "Failed: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading && url.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Sending...")
                    } else {
                        Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Send Request")
                    }
                }

                if (responseStatus.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status: $responseStatus", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("$responseTimeMs ms", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(160.dp).verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = responseBody,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            onInjectDataIntoProject(responseBody)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save as data.json in Project", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// -------------------------------------------------------------
// 2. In-App Browser (Live Web Inspector & Mobile Viewport)
// -------------------------------------------------------------
@Composable
fun InAppBrowserModal(
    initialUrl: String = "https://html.spec.whatwg.org/",
    onDismiss: () -> Unit
) {
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var inputUrl by remember { mutableStateOf(initialUrl) }
    var webViewInstance: WebView? by remember { mutableStateOf(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("In-App Browser", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Row {
                    IconButton(onClick = { webViewInstance?.goBack() }) {
                        Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { webViewInstance?.goForward() }) {
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { webViewInstance?.reload() }) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(46.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Button(
                        onClick = {
                            var target = inputUrl.trim()
                            if (!target.startsWith("http://") && !target.startsWith("https://")) {
                                target = "https://$target"
                            }
                            currentUrl = target
                            webViewInstance?.loadUrl(target)
                        },
                        modifier = Modifier.height(46.dp)
                    ) {
                        Text("Go", fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                            loadUrl(currentUrl)
                            webViewInstance = this
                        }
                    },
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// -------------------------------------------------------------
// 3. Photo to Code (UI Layout Generator)
// -------------------------------------------------------------
@Composable
fun PhotoToCodeModal(
    onDismiss: () -> Unit,
    onInjectCode: (html: String, css: String) -> Unit
) {
    val templates = listOf(
        "Hero Landing Banner" to Pair(
            "<section class=\"hero\"><h1>Transform Your Web Vision</h1><p>Craft responsive web applications on mobile.</p><button class=\"btn\">Get Started</button></section>",
            ".hero { padding: 40px 20px; text-align: center; background: linear-gradient(135deg, #1e293b, #0f172a); color: white; border-radius: 12px; }\n.btn { background: #38bdf8; color: #0f172a; padding: 10px 20px; border: none; border-radius: 8px; font-weight: bold; margin-top: 16px; cursor: pointer; }"
        ),
        "Modern Login Card" to Pair(
            "<div class=\"card\"><h2>Account Login</h2><input type=\"email\" placeholder=\"Email\"><input type=\"password\" placeholder=\"Password\"><button class=\"btn\">Sign In</button></div>",
            ".card { max-width: 320px; margin: 40px auto; padding: 24px; background: #1e293b; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.3); color: white; }\ninput { width: 100%; padding: 10px; margin: 8px 0; border: 1px solid #334155; border-radius: 6px; background: #0f172a; color: white; box-sizing: border-box; }\n.btn { width: 100%; padding: 10px; background: #10b981; border: none; border-radius: 6px; color: white; font-weight: bold; cursor: pointer; margin-top: 8px; }"
        ),
        "Product Showcase Card" to Pair(
            "<div class=\"product-card\"><div class=\"badge\">NEW</div><h3>Pro Headset</h3><p class=\"price\">$99.00</p><p>Noise-canceling spatial audio.</p><button class=\"btn\">Add to Cart</button></div>",
            ".product-card { padding: 20px; background: #1e293b; color: white; border-radius: 12px; border: 1px solid #334155; position: relative; }\n.badge { display: inline-block; background: #f59e0b; color: black; padding: 2px 8px; font-size: 11px; font-weight: bold; border-radius: 4px; }\n.price { font-size: 20px; font-weight: bold; color: #38bdf8; margin: 8px 0; }\n.btn { background: #38bdf8; color: #0f172a; border: none; padding: 8px 16px; border-radius: 6px; font-weight: bold; cursor: pointer; }"
        )
    )

    var selectedIdx by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PhotoCamera, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Photo & Wireframe to Code", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Select a layout prototype or design wireframe to instantly convert into clean, responsive HTML & CSS markup:",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )

                templates.forEachIndexed { idx, (name, _) ->
                    Surface(
                        onClick = { selectedIdx = idx },
                        color = if (selectedIdx == idx) Color(0xFF0369A1) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            if (selectedIdx == idx) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text("Generated Preview:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(templates[selectedIdx].second.first, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFF97316))
                        Spacer(Modifier.height(6.dp))
                        Text(templates[selectedIdx].second.second, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF38BDF8))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pair = templates[selectedIdx].second
                    onInjectCode(pair.first, pair.second)
                    onDismiss()
                }
            ) {
                Text("Add Code to Project")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// -------------------------------------------------------------
// 4. HTML5 Tags Reference Directory
// -------------------------------------------------------------
@Composable
fun HtmlTagsReferenceModal(
    onDismiss: () -> Unit,
    onTryTag: (tagName: String, sampleCode: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val tags = listOf(
        Triple("<header>", "Container for introductory content or navigational links", "<header>\n  <h1>Website Title</h1>\n  <nav><a href=\"#\">Home</a></nav>\n</header>"),
        Triple("<nav>", "Section of a page intended to provide navigation links", "<nav>\n  <ul>\n    <li><a href=\"#home\">Home</a></li>\n    <li><a href=\"#about\">About</a></li>\n  </ul>\n</nav>"),
        Triple("<main>", "Dominant content of the <body> of a document", "<main>\n  <h2>Featured Article</h2>\n  <p>Article content goes here.</p>\n</main>"),
        Triple("<article>", "Self-contained composition intended to be independently reusable", "<article>\n  <h3>Blog Post Title</h3>\n  <p>Published on September 2026.</p>\n</article>"),
        Triple("<section>", "Generic standalone section of a document", "<section>\n  <h2>Services</h2>\n  <p>We build web apps.</p>\n</section>"),
        Triple("<canvas>", "Graphics container used to draw graphics on the fly via JavaScript", "<canvas id=\"gameCanvas\" width=\"400\" height=\"300\"></canvas>"),
        Triple("<dialog>", "Dialog box or subwindow such as a modal alert or inspector", "<dialog open>\n  <p>Welcome to HTML Live!</p>\n  <button>OK</button>\n</dialog>"),
        Triple("<form>", "Document section containing interactive controls for submitting information", "<form action=\"/submit\">\n  <label>Name: <input type=\"text\" name=\"user\"></label>\n  <button type=\"submit\">Submit</button>\n</form>"),
        Triple("<audio>", "Used to embed sound content in documents", "<audio controls src=\"sound.mp3\">\n  Your browser does not support the audio element.\n</audio>"),
        Triple("<video>", "Used to embed video content in documents", "<video width=\"320\" height=\"240\" controls>\n  <source src=\"movie.mp4\" type=\"video/mp4\">\n</video>")
    )

    val filtered = tags.filter { it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, null, tint = Color(0xFFF97316), modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("HTML5 Tags Directory", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tags (<canvas>, <nav>...)", fontSize = 12.sp) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )

                Spacer(Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { (tag, desc, snippet) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tag, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                    TextButton(onClick = { onTryTag(tag, snippet) }) {
                                        Text("Try Code", fontSize = 11.sp, color = Color(0xFF10B981))
                                    }
                                }
                                Text(desc, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// -------------------------------------------------------------
// 5. HTML & Web Q&A Knowledge Base
// -------------------------------------------------------------
@Composable
fun HtmlQnaModal(onDismiss: () -> Unit) {
    val qnaList = listOf(
        Pair(
            "What is Semantic HTML and why should we use it?",
            "Semantic HTML introduces meaning to the web page rather than just presentation (e.g. using <header>, <article>, <section> instead of endless <div> tags). It improves accessibility for screen readers, SEO for search engines, and maintainability for developers."
        ),
        Pair(
            "What is the CSS Box Model?",
            "The Box Model comprises Content, Padding (space inside border), Border, and Margin (space outside border). Using 'box-sizing: border-box' ensures padding and border are included in the element's total width and height."
        ),
        Pair(
            "What is the difference between LocalStorage and SessionStorage?",
            "LocalStorage persists data indefinitely with no expiration date, while SessionStorage clears data as soon as the browser tab or session is closed."
        ),
        Pair(
            "How does the HTML5 Canvas game loop function?",
            "Canvas game loops use 'window.requestAnimationFrame(loop)' to sync animation updates with the device's refresh rate (typically 60Hz or 120Hz). Each frame clears the canvas, updates physics/positions, and renders sprites."
        ),
        Pair(
            "What causes a Git merge conflict?",
            "A merge conflict happens when two branches modify the exact same line of code in contradictory ways. Git pauses the merge and places conflict markers (<<<<<<< HEAD, =======, >>>>>>>) so the developer can manually resolve it."
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QuestionAnswer, null, tint = Color(0xFFA855F7), modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("HTML & Web Development Q&A", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(qnaList) { (q, a) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Q: $q", fontWeight = FontWeight.Bold, color = Color(0xFFFDE68A), fontSize = 13.sp)
                            Text("A: $a", color = Color(0xFFCBD5E1), fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
