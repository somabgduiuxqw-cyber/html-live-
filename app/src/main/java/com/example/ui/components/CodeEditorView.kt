package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorSettings
import com.example.ui.editor.CodeSyntaxHighlighter

@Composable
fun CodeEditorView(
    code: String,
    fileName: String,
    settings: EditorSettings,
    onCodeChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(fileName) {
        mutableStateOf(TextFieldValue(code, selection = TextRange(0)))
    }

    // Update if external code changed (e.g. AI applied changes or template loaded)
    LaunchedEffect(code) {
        if (textFieldValue.text != code) {
            textFieldValue = textFieldValue.copy(text = code)
        }
    }

    // Undo / Redo history
    val undoStack = remember(fileName) { mutableStateListOf<String>() }
    val redoStack = remember(fileName) { mutableStateListOf<String>() }

    // Search and Replace State
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }

    // Go to line dialog
    var showGoToLineDialog by remember { mutableStateOf(false) }
    var targetLineInput by remember { mutableStateOf("") }

    var wordWrap by remember { mutableStateOf(settings.wordWrap) }

    val extension = fileName.substringAfterLast('.', "html")

    // Autocomplete / Quick snippets depending on file type
    val snippets = remember(extension) {
        when (extension.lowercase()) {
            "html" -> listOf("<div>", "</div>", "<h1>", "<p>", "<button>", "class=\"\"", "id=\"\"", "<span>", "<canvas>", "<a>", "<script>", "<style>")
            "css" -> listOf("display: flex;", "margin: 0;", "padding: 0;", "background: ", "color: ", "border-radius: ", "justify-content: center;", "align-items: center;", "font-size: ", "width: 100%;", "height: ", "box-sizing: border-box;")
            "js", "javascript" -> listOf("const ", "let ", "function ", "console.log();", "document.getElementById('')", "addEventListener('', () => {})", "if () {}", "return ", "=>", "{}", "()", ";", "localStorage.setItem('', '')")
            else -> listOf("\"", "{", "}", "[", "]", ":", ",", ";", "=", "<", ">")
        }
    }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val lines = remember(textFieldValue.text) {
        textFieldValue.text.split("\n")
    }

    val visualTransformation = remember(extension) {
        VisualTransformation { original ->
            val highlighted = CodeSyntaxHighlighter.highlight(original.text, extension)
            TransformedText(highlighted, OffsetMapping.Identity)
        }
    }

    fun applyChange(newText: String, newCursor: Int = -1) {
        undoStack.add(textFieldValue.text)
        if (undoStack.size > 50) undoStack.removeAt(0)
        redoStack.clear()

        val cursor = if (newCursor >= 0) TextRange(newCursor) else textFieldValue.selection
        textFieldValue = TextFieldValue(newText, selection = cursor)
        onCodeChanged(newText)
    }

    fun handleUndo() {
        if (undoStack.isNotEmpty()) {
            val last = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(textFieldValue.text)
            textFieldValue = TextFieldValue(last, selection = TextRange(last.length.coerceAtMost(textFieldValue.selection.start)))
            onCodeChanged(last)
        }
    }

    fun handleRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(textFieldValue.text)
            textFieldValue = TextFieldValue(next, selection = TextRange(next.length.coerceAtMost(textFieldValue.selection.start)))
            onCodeChanged(next)
        }
    }

    fun insertSnippet(snippet: String) {
        val start = textFieldValue.selection.start.coerceAtLeast(0)
        val end = textFieldValue.selection.end.coerceAtLeast(0)
        val before = textFieldValue.text.substring(0, start)
        val after = textFieldValue.text.substring(end)
        val newText = before + snippet + after
        val newPos = start + snippet.length
        applyChange(newText, newPos)
    }

    fun handleCommentUncomment() {
        val currentText = textFieldValue.text
        val pos = textFieldValue.selection.start.coerceIn(0, currentText.length)
        val lineStart = currentText.lastIndexOf('\n', pos - 1) + 1
        val lineEnd = currentText.indexOf('\n', pos).let { if (it == -1) currentText.length else it }
        val line = currentText.substring(lineStart, lineEnd)

        val newLine = when (extension) {
            "html" -> if (line.trim().startsWith("<!--") && line.trim().endsWith("-->")) {
                line.replaceFirst("<!--", "").replace("-->", "")
            } else {
                "<!-- $line -->"
            }
            "css" -> if (line.trim().startsWith("/*") && line.trim().endsWith("*/")) {
                line.replaceFirst("/*", "").replace("*/", "")
            } else {
                "/* $line */"
            }
            else -> if (line.trim().startsWith("//")) {
                line.replaceFirst("//", "").trimStart()
            } else {
                "// $line"
            }
        }

        val updated = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
        applyChange(updated, lineStart + newLine.length)
    }

    fun handleDuplicateLine() {
        val currentText = textFieldValue.text
        val pos = textFieldValue.selection.start.coerceIn(0, currentText.length)
        val lineStart = currentText.lastIndexOf('\n', pos - 1) + 1
        val lineEnd = currentText.indexOf('\n', pos).let { if (it == -1) currentText.length else it }
        val line = currentText.substring(lineStart, lineEnd)

        val updated = currentText.substring(0, lineEnd) + "\n" + line + currentText.substring(lineEnd)
        applyChange(updated, lineEnd + 1 + line.length)
    }

    fun handleDeleteLine() {
        val currentText = textFieldValue.text
        val pos = textFieldValue.selection.start.coerceIn(0, currentText.length)
        val lineStart = currentText.lastIndexOf('\n', pos - 1) + 1
        val lineEnd = currentText.indexOf('\n', pos).let { if (it == -1) currentText.length else it + 1 }

        val updated = currentText.substring(0, lineStart) + currentText.substring(lineEnd.coerceAtMost(currentText.length))
        applyChange(updated, lineStart)
    }

    fun handleFormatCode() {
        // Simple, clean indent formatting
        val formatted = formatSourceCode(textFieldValue.text, extension)
        applyChange(formatted, 0)
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        // Search & Replace Bar
        if (showSearchBar) {
            Surface(
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(48.dp),
                            textStyle = TextStyle(fontSize = 13.sp, color = Color.White)
                        )
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    val idx = textFieldValue.text.indexOf(searchQuery, textFieldValue.selection.end, ignoreCase = true)
                                    val matchIdx = if (idx >= 0) idx else textFieldValue.text.indexOf(searchQuery, 0, ignoreCase = true)
                                    if (matchIdx >= 0) {
                                        textFieldValue = textFieldValue.copy(
                                            selection = TextRange(matchIdx, matchIdx + searchQuery.length)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.testTag("editor_find_next_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Find Next", tint = Color(0xFF38BDF8))
                        }
                        IconButton(onClick = { showSearchBar = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Search", tint = Color.LightGray)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = replaceQuery,
                            onValueChange = { replaceQuery = it },
                            placeholder = { Text("Replace with...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(48.dp),
                            textStyle = TextStyle(fontSize = 13.sp, color = Color.White)
                        )
                        Button(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    val newText = textFieldValue.text.replace(searchQuery, replaceQuery, ignoreCase = true)
                                    applyChange(newText)
                                }
                            },
                            modifier = Modifier.testTag("editor_replace_all_button")
                        ) {
                            Text("Replace All", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Editor Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161F30))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { handleUndo() }, enabled = undoStack.isNotEmpty(), modifier = Modifier.testTag("editor_undo_btn")) {
                Icon(Icons.Default.Undo, contentDescription = "Undo", tint = if (undoStack.isNotEmpty()) Color.White else Color.Gray)
            }
            IconButton(onClick = { handleRedo() }, enabled = redoStack.isNotEmpty(), modifier = Modifier.testTag("editor_redo_btn")) {
                Icon(Icons.Default.Redo, contentDescription = "Redo", tint = if (redoStack.isNotEmpty()) Color.White else Color.Gray)
            }
            IconButton(onClick = { showSearchBar = !showSearchBar }, modifier = Modifier.testTag("editor_search_btn")) {
                Icon(Icons.Default.FindReplace, contentDescription = "Find & Replace", tint = Color(0xFF38BDF8))
            }
            IconButton(onClick = { handleFormatCode() }, modifier = Modifier.testTag("editor_format_btn")) {
                Icon(Icons.Default.FormatAlignLeft, contentDescription = "Format Code", tint = Color(0xFF4ADE80))
            }
            IconButton(onClick = { wordWrap = !wordWrap }, modifier = Modifier.testTag("editor_wrap_btn")) {
                Icon(Icons.Default.WrapText, contentDescription = "Toggle Wrap", tint = if (wordWrap) Color(0xFF38BDF8) else Color.Gray)
            }

            Spacer(Modifier.weight(1f))

            TextButton(onClick = { showGoToLineDialog = true }) {
                Text("Ln ${lines.size}", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }

            // Quick line tools menu
            IconButton(onClick = { handleCommentUncomment() }, modifier = Modifier.testTag("editor_comment_btn")) {
                Text("//", color = Color(0xFFFBBF24), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 14.sp)
            }
            IconButton(onClick = { handleDuplicateLine() }, modifier = Modifier.testTag("editor_duplicate_btn")) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Line", tint = Color.LightGray)
            }
            IconButton(onClick = { handleDeleteLine() }, modifier = Modifier.testTag("editor_delete_line_btn")) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Line", tint = Color(0xFFEF4444))
            }
        }

        // Snippets Accessory Bar (for ultra-fast mobile typing!)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(snippets) { snip ->
                Surface(
                    onClick = { insertSnippet(snip) },
                    color = Color(0xFF334155),
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.testTag("snippet_${snip.take(4)}")
                ) {
                    Text(
                        text = snip,
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Editor Core Area: Line Numbers + Text Field
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
        ) {
            // Line numbers column
            if (settings.showLineNumbers) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(42.dp)
                        .background(Color(0xFF0B1120))
                        .verticalScroll(verticalScroll)
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lines.size.coerceAtLeast(1)) {
                        Text(
                            text = "$i",
                            color = Color(0xFF475569),
                            fontSize = settings.fontSizeSp.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = (settings.fontSizeSp * 1.4f).sp
                        )
                    }
                }
            }

            // Code input surface
            val horizontalModifier = if (!wordWrap) Modifier.horizontalScroll(horizontalScroll) else Modifier
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(verticalScroll)
                    .then(horizontalModifier)
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        var updatedText = newValue.text
                        var newCursorPos = newValue.selection.start

                        // Auto-closing quotes & brackets
                        if (settings.bracketMatching && newValue.text.length == textFieldValue.text.length + 1) {
                            val insertedChar = newValue.text[newCursorPos - 1]
                            val closer = when (insertedChar) {
                                '{' -> "}"
                                '(' -> ")"
                                '[' -> "]"
                                '"' -> "\""
                                '\'' -> "'"
                                else -> null
                            }
                            if (closer != null) {
                                updatedText = newValue.text.substring(0, newCursorPos) + closer + newValue.text.substring(newCursorPos)
                            }
                        }

                        // Auto-indent on enter
                        if (settings.autoIndent && newValue.text.length == textFieldValue.text.length + 1 && newValue.text[newCursorPos - 1] == '\n') {
                            val prevLineEnd = newCursorPos - 2
                            val prevLineStart = (newValue.text.lastIndexOf('\n', prevLineEnd).takeIf { it != -1 } ?: -1) + 1
                            val prevLine = if (prevLineEnd >= prevLineStart) newValue.text.substring(prevLineStart, prevLineEnd + 1) else ""
                            val leadingSpaces = prevLine.takeWhile { it == ' ' || it == '\t' }
                            val extraIndent = if (prevLine.trimEnd().endsWith("{") || prevLine.trimEnd().endsWith(">")) "  " else ""
                            val indentToAdd = leadingSpaces + extraIndent

                            if (indentToAdd.isNotEmpty()) {
                                updatedText = newValue.text.substring(0, newCursorPos) + indentToAdd + newValue.text.substring(newCursorPos)
                                newCursorPos += indentToAdd.length
                            }
                        }

                        applyChange(updatedText, newCursorPos)
                    },
                    visualTransformation = visualTransformation,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = settings.fontSizeSp.sp,
                        color = Color(0xFFF8FAFC),
                        lineHeight = (settings.fontSizeSp * 1.4f).sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF38BDF8)),
                    modifier = Modifier.fillMaxSize().testTag("code_editor_text_field")
                )
            }
        }
    }

    // Go to line dialog
    if (showGoToLineDialog) {
        AlertDialog(
            onDismissRequest = { showGoToLineDialog = false },
            title = { Text("Go to Line") },
            text = {
                OutlinedTextField(
                    value = targetLineInput,
                    onValueChange = { targetLineInput = it },
                    placeholder = { Text("Line 1 - ${lines.size}") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val lineNum = targetLineInput.toIntOrNull()
                    if (lineNum != null && lineNum in 1..lines.size) {
                        var charIdx = 0
                        for (i in 0 until lineNum - 1) {
                            charIdx += lines[i].length + 1
                        }
                        textFieldValue = textFieldValue.copy(selection = TextRange(charIdx))
                    }
                    showGoToLineDialog = false
                }) {
                    Text("Go")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoToLineDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Simple source formatter that preserves structure
private fun formatSourceCode(source: String, extension: String): String {
    val lines = source.split("\n")
    val result = StringBuilder()
    var indentLevel = 0

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            result.append("\n")
            continue
        }

        // Adjust closing indent
        if (trimmed.startsWith("}") || trimmed.startsWith("]") || trimmed.startsWith("</") || trimmed.startsWith("-->") || trimmed.startsWith("*/")) {
            indentLevel = (indentLevel - 1).coerceAtLeast(0)
        }

        val indent = "  ".repeat(indentLevel)
        result.append(indent).append(trimmed).append("\n")

        // Adjust opening indent
        if (trimmed.endsWith("{") || (trimmed.startsWith("<") && !trimmed.startsWith("</") && !trimmed.endsWith("/>") && !trimmed.startsWith("<!") && !trimmed.contains("</")) || trimmed.endsWith("/*")) {
            indentLevel++
        }
    }
    return result.toString().trimEnd()
}
