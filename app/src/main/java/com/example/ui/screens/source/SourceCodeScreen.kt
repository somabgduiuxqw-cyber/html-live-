package com.example.ui.screens.source

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.source.SourceCodeRetriever
import com.example.data.source.SourceMode
import com.example.data.source.SourcePresets
import com.example.data.source.SourceRetrievalResult
import com.example.ui.editor.CodeSyntaxHighlighter
import kotlinx.coroutines.launch

@Composable
fun SourceCodeScreen(
    initialUrl: String = "https://example.com",
    onOpenInEditor: (projectName: String, sourceCode: String, extension: String) -> Unit,
    onSaveToProject: (projectName: String, sourceCode: String, extension: String) -> Unit,
    onSendToAi: (url: String, sourceCode: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var urlInput by remember { mutableStateOf(initialUrl) }
    var currentResult by remember { mutableStateOf<SourceRetrievalResult?>(null) }
    var currentMode by remember { mutableStateOf(SourceMode.RAW_HTTP) }
    var isLoadingRaw by remember { mutableStateOf(false) }
    var isLoadingDom by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    // Search inside code
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchMatchIndex by remember { mutableIntStateOf(0) }
    var searchMatchOffsets by remember { mutableStateOf<List<Int>>(emptyList()) }

    // Display options
    var wordWrap by remember { mutableStateOf(true) }
    var fontSizeSp by remember { mutableIntStateOf(12) }

    // Modals
    var showHeadersModal by remember { mutableStateOf(false) }
    var showSaveModal by remember { mutableStateOf(false) }
    var saveProjectNameInput by remember { mutableStateOf("") }
    var showPreviewModal by remember { mutableStateOf(false) }

    // History of URLs
    val recentUrls = remember {
        mutableStateListOf(
            "https://example.com",
            "https://html.spec.whatwg.org/",
            "https://jsonplaceholder.typicode.com/posts/1",
            "https://news.ycombinator.com"
        )
    }

    // BackHandler: exit fullscreen first!
    BackHandler(enabled = true) {
        if (showHeadersModal) {
            showHeadersModal = false
        } else if (showSaveModal) {
            showSaveModal = false
        } else if (showPreviewModal) {
            showPreviewModal = false
        } else if (showSearchBar) {
            showSearchBar = false
        } else if (isFullscreen) {
            isFullscreen = false
        } else {
            onBack()
        }
    }

    // Function to retrieve webpage
    fun executeGetSource(targetUrl: String) {
        val trimmed = targetUrl.trim()
        if (trimmed.isBlank()) return

        // Update history
        if (!recentUrls.contains(trimmed)) {
            recentUrls.add(0, trimmed)
            if (recentUrls.size > 8) recentUrls.removeAt(recentUrls.lastIndex)
        }

        isLoadingRaw = true
        scope.launch {
            val result = SourceCodeRetriever.fetchRawHttp(trimmed)
            currentResult = result
            isLoadingRaw = false

            // Auto-extract DOM in background if not binary and HTML
            if (!result.isBinary && (result.detectedLanguage == "html" || result.detectedLanguage == "xml")) {
                isLoadingDom = true
                val dom = SourceCodeRetriever.fetchRenderedDom(context, result.finalUrl)
                if (dom != null) {
                    currentResult = currentResult?.copy(renderedDomSource = dom)
                }
                isLoadingDom = false
            }
        }
    }

    // Load initial source automatically on launch if empty
    LaunchedEffect(Unit) {
        if (currentResult == null) {
            executeGetSource(initialUrl)
        }
    }

    // Current displayed source text based on mode
    val activeSourceText = remember(currentResult, currentMode) {
        currentResult?.getSourceForMode(currentMode) ?: ""
    }

    val activeLanguage = remember(currentResult, currentMode) {
        if (currentMode == SourceMode.RENDERED_DOM) "html"
        else currentResult?.detectedLanguage ?: "html"
    }

    // Update search matches when query or text changes
    LaunchedEffect(searchQuery, activeSourceText) {
        if (searchQuery.isNotBlank() && activeSourceText.isNotEmpty()) {
            val matches = mutableListOf<Int>()
            var idx = 0
            while (idx < activeSourceText.length) {
                val found = activeSourceText.indexOf(searchQuery, idx, ignoreCase = true)
                if (found >= 0) {
                    matches.add(found)
                    idx = found + searchQuery.length
                } else break
            }
            searchMatchOffsets = matches
            searchMatchIndex = if (matches.isNotEmpty()) 0 else -1
        } else {
            searchMatchOffsets = emptyList()
            searchMatchIndex = -1
        }
    }

    // Syntax highlighted code
    val highlightedText = remember(activeSourceText, activeLanguage, searchQuery, searchMatchIndex) {
        if (activeSourceText.length > 250_000) {
            // For extraordinarily large webpages, display text directly to avoid memory freeze
            buildAnnotatedString { append(activeSourceText) }
        } else {
            val base = CodeSyntaxHighlighter.highlight(activeSourceText, activeLanguage)
            if (searchQuery.isNotBlank() && searchMatchOffsets.isNotEmpty()) {
                val builder = androidx.compose.ui.text.AnnotatedString.Builder(base)
                searchMatchOffsets.forEachIndexed { i, offset ->
                    val end = (offset + searchQuery.length).coerceAtMost(activeSourceText.length)
                    val isCurrent = i == searchMatchIndex
                    builder.addStyle(
                        SpanStyle(
                            background = if (isCurrent) Color(0xFFF59E0B) else Color(0x66FBBF24),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        ),
                        offset,
                        end
                    )
                }
                builder.toAnnotatedString()
            } else {
                base
            }
        }
    }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
    ) {
        // TOP APP BAR (Hidden when isFullscreen = true)
        if (!isFullscreen) {
            Surface(
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack, modifier = Modifier.size(36.dp).testTag("source_back_btn")) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(Modifier.width(4.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Code, null, tint = Color(0xFFF97316), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Webpage Source Code", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                }
                                Text("Raw HTTP & Rendered DOM Inspector", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isFullscreen = true },
                                modifier = Modifier.size(36.dp).testTag("source_fullscreen_btn")
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = "Enter Fullscreen", tint = Color(0xFF38BDF8))
                            }
                        }
                    }

                    // URL INPUT BAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            placeholder = { Text("https://example.com", fontSize = 12.sp, color = Color.Gray) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    if (urlInput.startsWith("https://", ignoreCase = true)) Icons.Default.Security else Icons.Default.Http,
                                    contentDescription = null,
                                    tint = if (urlInput.startsWith("https://", ignoreCase = true)) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (urlInput.isNotBlank()) {
                                        IconButton(onClick = { urlInput = "" }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear URL", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                                            if (!clip.isNullOrBlank()) {
                                                urlInput = clip.trim()
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste URL", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("source_url_input")
                        )

                        Spacer(Modifier.width(6.dp))

                        Button(
                            onClick = { executeGetSource(urlInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            enabled = !isLoadingRaw,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp).testTag("source_get_code_btn")
                        ) {
                            if (isLoadingRaw) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Get Source", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // PRESET & RECENT CHIPS
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Text("Presets:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        items(SourcePresets.PRESETS) { preset ->
                            FilterChip(
                                selected = urlInput == preset.url,
                                onClick = {
                                    urlInput = preset.url
                                    executeGetSource(preset.url)
                                },
                                label = { Text(preset.title, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0284C7),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF0F172A),
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                        if (recentUrls.isNotEmpty()) {
                            item {
                                Spacer(Modifier.width(4.dp))
                                Text("Recent:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            items(recentUrls) { recent ->
                                FilterChip(
                                    selected = urlInput == recent,
                                    onClick = {
                                        urlInput = recent
                                        executeGetSource(recent)
                                    },
                                    label = { Text(recent.removePrefix("https://").removePrefix("http://").take(22), fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0284C7),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF1E293B),
                                        labelColor = Color(0xFF94A3B8)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // FULLSCREEN COMPACT TOP BAR (shown only when isFullscreen = true)
        if (isFullscreen) {
            Surface(color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isFullscreen = false }, modifier = Modifier.size(32.dp).testTag("source_exit_fs_top")) {
                            Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", tint = Color(0xFF38BDF8))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = currentResult?.finalUrl ?: urlInput,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.widthIn(max = 240.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Quick Mode Toggle
                        FilterChip(
                            selected = currentMode == SourceMode.RAW_HTTP,
                            onClick = { currentMode = SourceMode.RAW_HTTP },
                            label = { Text("Raw", fontSize = 10.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                        FilterChip(
                            selected = currentMode == SourceMode.RENDERED_DOM,
                            onClick = {
                                currentMode = SourceMode.RENDERED_DOM
                                if (currentResult?.renderedDomSource == null && currentResult != null) {
                                    isLoadingDom = true
                                    scope.launch {
                                        val dom = SourceCodeRetriever.fetchRenderedDom(context, currentResult!!.finalUrl)
                                        currentResult = currentResult?.copy(renderedDomSource = dom)
                                        isLoadingDom = false
                                    }
                                }
                            },
                            label = { Text("DOM", fontSize = 10.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                        IconButton(onClick = { showSearchBar = !showSearchBar }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Source Code", activeSourceText))
                            Toast.makeText(context, "Copied ${activeSourceText.length} characters", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // LOADING INDICATOR
        if (isLoadingRaw || isLoadingDom) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = Color(0xFF38BDF8),
                trackColor = Color(0xFF1E293B)
            )
        }

        // MODE SELECTOR STRIP (Raw HTTP Source vs Rendered DOM)
        Surface(color = Color(0xFF161F30), modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = currentMode == SourceMode.RAW_HTTP,
                            onClick = { currentMode = SourceMode.RAW_HTTP },
                            label = { Text("Raw HTTP Source", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("mode_raw_http")
                        )

                        FilterChip(
                            selected = currentMode == SourceMode.RENDERED_DOM,
                            onClick = {
                                currentMode = SourceMode.RENDERED_DOM
                                if (currentResult?.renderedDomSource == null && currentResult != null && !currentResult!!.isBinary) {
                                    isLoadingDom = true
                                    scope.launch {
                                        val dom = SourceCodeRetriever.fetchRenderedDom(context, currentResult!!.finalUrl)
                                        currentResult = currentResult?.copy(renderedDomSource = dom)
                                        isLoadingDom = false
                                    }
                                }
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Rendered DOM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (isLoadingDom) {
                                        Spacer(Modifier.width(4.dp))
                                        CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp, color = Color(0xFF38BDF8))
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("mode_rendered_dom")
                        )
                    }

                    // Mode helper note pill
                    Text(
                        text = if (currentMode == SourceMode.RAW_HTTP) "HTTP GET response" else "Evaluated JS DOM",
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // STATUS & METADATA BAR
                currentResult?.let { res ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // HTTP Status badge
                        val statusColor = if (res.httpStatusCode in 200..299) Color(0xFF10B981) else Color(0xFFEF4444)
                        Surface(color = statusColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "Status: ${res.httpStatusCode} ${res.statusMessage}",
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Response Time
                        Text("${res.responseTimeMs} ms", fontSize = 10.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)

                        // Content Type
                        Text(res.contentType.take(28), fontSize = 10.sp, color = Color(0xFFCBD5E1), fontFamily = FontFamily.Monospace)

                        // Size
                        Text(res.formattedSize, fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)

                        // Line & Char Count
                        Text("${res.lineCount} lines, ${res.characterCount} chars", fontSize = 10.sp, color = Color(0xFF94A3B8))

                        // View Headers & Security
                        TextButton(
                            onClick = { showHeadersModal = true },
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(Icons.Default.Security, null, modifier = Modifier.size(12.dp), tint = Color(0xFF38BDF8))
                            Spacer(Modifier.width(2.dp))
                            Text("Headers & SSL", fontSize = 10.sp, color = Color(0xFF38BDF8))
                        }
                    }
                }
            }
        }

        // IN-CODE SEARCH BAR (Toggleable)
        AnimatedVisibility(visible = showSearchBar) {
            Surface(color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Find in source...", fontSize = 11.sp, color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.weight(1f).height(44.dp)
                    )

                    Spacer(Modifier.width(4.dp))

                    val matchCount = searchMatchOffsets.size
                    val matchText = if (searchQuery.isBlank()) "" else if (matchCount == 0) "0/0" else "${searchMatchIndex + 1}/$matchCount"
                    Text(matchText, fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))

                    IconButton(
                        onClick = {
                            if (searchMatchOffsets.isNotEmpty()) {
                                searchMatchIndex = if (searchMatchIndex <= 0) searchMatchOffsets.size - 1 else searchMatchIndex - 1
                            }
                        },
                        enabled = searchMatchOffsets.isNotEmpty(),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous Match", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            if (searchMatchOffsets.isNotEmpty()) {
                                searchMatchIndex = if (searchMatchIndex >= searchMatchOffsets.size - 1) 0 else searchMatchIndex + 1
                            }
                        },
                        enabled = searchMatchOffsets.isNotEmpty(),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next Match", tint = Color.White)
                    }

                    IconButton(onClick = { showSearchBar = false }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close Search", tint = Color.Gray)
                    }
                }
            }
        }

        // ERROR BANNER IF ANY
        currentResult?.errorMessage?.let { err ->
            Surface(
                color = Color(0xFF7F1D1D),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Error: $err",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { executeGetSource(urlInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Retry", fontSize = 10.sp)
                    }
                }
            }
        }

        // BINARY FILE BANNER (IF URL RETURNED IMAGE / MEDIA)
        if (currentResult?.isBinary == true) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Download, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(36.dp))
                    Text("Binary Response Detected", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text(
                        "The server returned binary data (${currentResult?.contentType}) of ${currentResult?.formattedSize} rather than human-readable text code.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse(currentResult?.finalUrl ?: urlInput)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("Open in External Viewer")
                    }
                }
            }
        }

        // MAIN CODE VIEWER PANE
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0A0F1D))
        ) {
            if (activeSourceText.isEmpty() && !isLoadingRaw) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Code, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Enter a URL and tap 'Get Source' to inspect code", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll)
                ) {
                    // Line numbers gutter
                    val lines = remember(activeSourceText) {
                        val count = activeSourceText.count { it == '\n' } + 1
                        (1..count).toList()
                    }

                    Column(
                        modifier = Modifier
                            .background(Color(0xFF0D1424))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        lines.forEach { lineNum ->
                            Text(
                                text = "$lineNum",
                                color = Color(0xFF475569),
                                fontSize = fontSizeSp.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = (fontSizeSp + 6).sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .then(
                                if (!wordWrap) Modifier.horizontalScroll(horizontalScroll) else Modifier
                            )
                    ) {
                        SelectionContainer {
                            Text(
                                text = highlightedText,
                                color = Color(0xFFE2E8F0),
                                fontSize = fontSizeSp.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = (fontSizeSp + 6).sp,
                                modifier = Modifier.testTag("source_code_display_text")
                            )
                        }
                    }
                }
            }

            // FLOATING BOTTOM TOOLBAR / ACTIONS BAR
            Surface(
                color = Color(0xEE1E293B),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Copy All
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Source Code", activeSourceText))
                            Toast.makeText(context, "Copied code to clipboard (${activeSourceText.length} chars)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp).testTag("action_copy_source")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy All", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    }

                    // Search Trigger
                    IconButton(
                        onClick = { showSearchBar = !showSearchBar },
                        modifier = Modifier.size(36.dp).testTag("action_search_source")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Find", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    // Word Wrap Toggle
                    IconButton(
                        onClick = { wordWrap = !wordWrap },
                        modifier = Modifier.size(36.dp).testTag("action_wrap_toggle")
                    ) {
                        Icon(
                            Icons.Default.WrapText,
                            contentDescription = "Word Wrap",
                            tint = if (wordWrap) Color(0xFF38BDF8) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Font Size Cycle
                    IconButton(
                        onClick = {
                            fontSizeSp = when (fontSizeSp) {
                                10 -> 12
                                12 -> 14
                                14 -> 16
                                else -> 10
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FormatSize, contentDescription = "Font Size", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }

                    Divider(
                        color = Color(0xFF334155),
                        modifier = Modifier
                            .height(20.dp)
                            .width(1.dp)
                    )

                    // Open in HTML Live Editor
                    Button(
                        onClick = {
                            val hostName = try {
                                java.net.URI(currentResult?.finalUrl ?: urlInput).host ?: "webpage"
                            } catch (_: Exception) {
                                "webpage"
                            }
                            val cleanProjName = "Source: $hostName"
                            val ext = if (activeLanguage == "json") "json" else if (activeLanguage == "css") "css" else if (activeLanguage == "js") "js" else "html"
                            onOpenInEditor(cleanProjName, activeSourceText, ext)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("action_open_in_editor")
                    ) {
                        Icon(Icons.Default.Code, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Open in Editor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Save as Project
                    Button(
                        onClick = {
                            val hostName = try {
                                java.net.URI(currentResult?.finalUrl ?: urlInput).host ?: "webpage"
                            } catch (_: Exception) {
                                "webpage"
                            }
                            saveProjectNameInput = "Source - $hostName"
                            showSaveModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("action_save_project")
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save Project", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Live Preview in WebView
                    IconButton(
                        onClick = { showPreviewModal = true },
                        modifier = Modifier.size(36.dp).testTag("action_preview_source")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run in WebView", tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                    }

                    // Ask AI about this source code
                    IconButton(
                        onClick = {
                            onSendToAi(currentResult?.finalUrl ?: urlInput, activeSourceText)
                        },
                        modifier = Modifier.size(36.dp).testTag("action_ai_explain")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Explain", tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp))
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, activeSourceText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Source Code"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }

                    // Fullscreen Toggle
                    IconButton(
                        onClick = { isFullscreen = !isFullscreen },
                        modifier = Modifier.size(36.dp).testTag("action_fullscreen_toggle")
                    ) {
                        Icon(
                            if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // MODAL: HEADERS, COOKIES & SECURITY INSPECTOR
    // -------------------------------------------------------------
    if (showHeadersModal) {
        AlertDialog(
            onDismissRequest = { showHeadersModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("HTTP Headers & Security", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Security Info
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Connection Security", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            val isSec = currentResult?.isHttps == true
                            Row {
                                Text("Protocol: ", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text(if (isSec) "HTTPS (Encrypted)" else "HTTP (Unencrypted)", color = if (isSec) Color(0xFF34D399) else Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            currentResult?.tlsVersion?.let { tls ->
                                Row {
                                    Text("TLS Version: ", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text(tls, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            currentResult?.cipherSuite?.let { cipher ->
                                Row {
                                    Text("Cipher: ", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text(cipher, color = Color(0xFFCBD5E1), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    // Cookies if any
                    currentResult?.cookies?.let { cookies ->
                        if (cookies.isNotEmpty()) {
                            Text("Set-Cookie (${cookies.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            cookies.forEach { cookie ->
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = cookie,
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Headers
                    Text("Response Headers (${currentResult?.headers?.size ?: 0})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    currentResult?.headers?.forEach { (name, value) ->
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text(name, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(value, color = Color(0xFFCBD5E1), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHeadersModal = false }) { Text("Close") }
            }
        )
    }

    // -------------------------------------------------------------
    // MODAL: SAVE TO PROJECT DIALOG
    // -------------------------------------------------------------
    if (showSaveModal) {
        AlertDialog(
            onDismissRequest = { showSaveModal = false },
            title = { Text("Save Source as Project", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a project name to save this retrieved source code:", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    OutlinedTextField(
                        value = saveProjectNameInput,
                        onValueChange = { saveProjectNameInput = it },
                        singleLine = true,
                        label = { Text("Project Name") },
                        modifier = Modifier.fillMaxWidth().testTag("save_proj_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = saveProjectNameInput.trim().ifBlank { "Imported Webpage" }
                        val ext = if (activeLanguage == "json") "json" else if (activeLanguage == "css") "css" else if (activeLanguage == "js") "js" else "html"
                        onSaveToProject(name, activeSourceText, ext)
                        showSaveModal = false
                        Toast.makeText(context, "Saved project '$name'", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("save_proj_confirm_btn")
                ) {
                    Text("Save Project")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveModal = false }) { Text("Cancel") }
            }
        )
    }

    // -------------------------------------------------------------
    // MODAL: LIVE PREVIEW OF RETRIEVED SOURCE
    // -------------------------------------------------------------
    if (showPreviewModal) {
        AlertDialog(
            onDismissRequest = { showPreviewModal = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Live Web Preview", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { showPreviewModal = false }) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(440.dp)) {
                    Text(
                        text = "Rendering retrieved ${currentMode.label}:",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(Modifier.height(6.dp))
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            android.webkit.WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = android.webkit.WebViewClient()
                                val baseUrl = currentResult?.finalUrl ?: "https://example.com"
                                loadDataWithBaseURL(baseUrl, activeSourceText, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showPreviewModal = false
                    val hostName = try { java.net.URI(currentResult?.finalUrl ?: urlInput).host ?: "webpage" } catch (_: Exception) { "webpage" }
                    val ext = if (activeLanguage == "json") "json" else if (activeLanguage == "css") "css" else if (activeLanguage == "js") "js" else "html"
                    onOpenInEditor("Source: $hostName", activeSourceText, ext)
                }) {
                    Text("Open in Full Editor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreviewModal = false }) { Text("Close") }
            }
        )
    }
}
