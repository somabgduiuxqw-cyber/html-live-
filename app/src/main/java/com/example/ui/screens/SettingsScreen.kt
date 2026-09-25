package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiService
import com.example.data.storage.SecurePreferences
import com.example.model.AiProvider
import com.example.model.AiSettings
import com.example.model.EditorSettings
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    aiSettings: AiSettings,
    editorSettings: EditorSettings,
    onSaveAiSettings: (AiSettings) -> Unit,
    onSaveEditorSettings: (EditorSettings) -> Unit,
    aiService: AiService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeProvider by remember { mutableStateOf(aiSettings.provider) }
    var geminiKey by remember { mutableStateOf(aiSettings.geminiApiKey) }
    var openAiKey by remember { mutableStateOf(aiSettings.openAiApiKey) }
    var customEndpoint by remember { mutableStateOf(aiSettings.customEndpoint) }
    var customKey by remember { mutableStateOf(aiSettings.customApiKey) }
    var selectedModel by remember { mutableStateOf(aiSettings.selectedModel) }
    var aiMode by remember { mutableStateOf(aiSettings.mode) }
    var experienceLevel by remember { mutableStateOf(aiSettings.experienceLevel) }

    var fontSize by remember { mutableFloatStateOf(editorSettings.fontSizeSp) }
    var tabSize by remember { mutableIntStateOf(editorSettings.tabSize) }
    var wordWrap by remember { mutableStateOf(editorSettings.wordWrap) }
    var autoIndent by remember { mutableStateOf(editorSettings.autoIndent) }
    var bracketMatching by remember { mutableStateOf(editorSettings.bracketMatching) }
    var showLineNumbers by remember { mutableStateOf(editorSettings.showLineNumbers) }
    var beginnerMode by remember { mutableStateOf(editorSettings.beginnerMode) }

    var showKeyInputDialog by remember { mutableStateOf<AiProvider?>(null) }
    var keyInputValue by remember { mutableStateOf("") }
    var isTestingConnection by remember { mutableStateOf(false) }
    var availableModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var isFetchingModels by remember { mutableStateOf(false) }

    fun commitAiChanges() {
        val updated = aiSettings.copy(
            provider = activeProvider,
            geminiApiKey = geminiKey,
            openAiApiKey = openAiKey,
            customEndpoint = customEndpoint,
            customApiKey = customKey,
            selectedModel = selectedModel,
            mode = aiMode,
            experienceLevel = experienceLevel
        )
        onSaveAiSettings(updated)
    }

    fun commitEditorChanges() {
        val updated = editorSettings.copy(
            fontSizeSp = fontSize,
            tabSize = tabSize,
            wordWrap = wordWrap,
            autoIndent = autoIndent,
            bracketMatching = bracketMatching,
            showLineNumbers = showLineNumbers,
            beginnerMode = beginnerMode
        )
        onSaveEditorSettings(updated)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("Settings & Configurations", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Section 1: AI Providers & Keys
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("AI Providers & Credentials", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }
                Text("Select your AI engine. Keys are saved in local encrypted storage and never exposed.", fontSize = 12.sp, color = Color(0xFF94A3B8))

                // Provider Selection Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiProvider.values().forEach { prov ->
                        FilterChip(
                            selected = activeProvider == prov,
                            onClick = {
                                activeProvider = prov
                                selectedModel = if (prov == AiProvider.GEMINI) "gemini-2.5-flash" else "gpt-4o"
                                commitAiChanges()
                            },
                            label = { Text(prov.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF38BDF8),
                                selectedLabelColor = Color(0xFF0F172A),
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }

                // Active Provider Status Box
                val currentKey = when (activeProvider) {
                    AiProvider.GEMINI -> geminiKey
                    AiProvider.OPENAI -> openAiKey
                    AiProvider.CUSTOM -> customKey
                }
                val hasKey = currentKey.isNotBlank()

                Surface(
                    color = Color(0xFF0F172A),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(activeProvider.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text(
                                text = if (hasKey) "Key: ${SecurePreferences.maskKey(currentKey)}" else "Status: No API key configured",
                                fontSize = 12.sp,
                                color = if (hasKey) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    keyInputValue = currentKey
                                    showKeyInputDialog = activeProvider
                                },
                                modifier = Modifier.testTag("set_key_btn")
                            ) {
                                Text(if (hasKey) "Change" else "Add Key", fontSize = 11.sp, color = Color(0xFF38BDF8))
                            }

                            if (hasKey) {
                                IconButton(
                                    onClick = {
                                        when (activeProvider) {
                                            AiProvider.GEMINI -> geminiKey = ""
                                            AiProvider.OPENAI -> openAiKey = ""
                                            AiProvider.CUSTOM -> customKey = ""
                                        }
                                        commitAiChanges()
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Key", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }

                // Test Connection & Dynamic Model Discovery Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isTestingConnection = true
                            scope.launch {
                                val currentSettings = aiSettings.copy(
                                    provider = activeProvider,
                                    geminiApiKey = geminiKey,
                                    openAiApiKey = openAiKey,
                                    customApiKey = customKey
                                )
                                val res = aiService.testConnection(currentSettings)
                                isTestingConnection = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, res.getOrNull() ?: "Success!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isTestingConnection,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF0F172A)),
                        modifier = Modifier.weight(1f).testTag("test_ai_connection_btn")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF0F172A), strokeWidth = 2.dp)
                        } else {
                            Text("Test Connection", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            isFetchingModels = true
                            scope.launch {
                                val currentSettings = aiSettings.copy(
                                    provider = activeProvider,
                                    geminiApiKey = geminiKey,
                                    openAiApiKey = openAiKey,
                                    customApiKey = customKey
                                )
                                availableModels = aiService.fetchAvailableModels(currentSettings)
                                isFetchingModels = false
                                Toast.makeText(context, "Loaded ${availableModels.size} available models", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isFetchingModels,
                        modifier = Modifier.weight(1f).testTag("load_models_btn")
                    ) {
                        if (isFetchingModels) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF38BDF8), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, null, Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                            Spacer(Modifier.width(4.dp))
                            Text("Load Models", fontSize = 12.sp, color = Color(0xFF38BDF8))
                        }
                    }
                }

                // Model Selection Chips
                val modelList = if (availableModels.isNotEmpty()) availableModels else when (activeProvider) {
                    AiProvider.GEMINI -> listOf("gemini-2.5-flash", "gemini-1.5-pro", "gemini-1.5-flash")
                    AiProvider.OPENAI -> listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo")
                    AiProvider.CUSTOM -> listOf("gpt-4o", "gpt-4o-mini")
                }

                Text("Active Model", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    modelList.forEach { m ->
                        FilterChip(
                            selected = selectedModel == m,
                            onClick = {
                                selectedModel = m
                                commitAiChanges()
                            },
                            label = { Text(m, fontSize = 11.sp) },
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

        // Section 2: AI Tutoring & Interaction Mode
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("AI Tutoring & Persona", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)

                Text("Experience Level", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Beginner", "Intermediate", "Advanced").forEach { lvl ->
                        FilterChip(
                            selected = experienceLevel.equals(lvl, ignoreCase = true),
                            onClick = {
                                experienceLevel = lvl.lowercase()
                                commitAiChanges()
                            },
                            label = { Text(lvl, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF38BDF8),
                                selectedLabelColor = Color(0xFF0F172A),
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }

                Text("Generation Mode", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "build_explain" to "Build & Explain",
                        "just_build" to "Build It For Me",
                        "teach_build" to "Teach While Building",
                        "hints" to "Give Me Hints"
                    ).forEach { (mKey, mLabel) ->
                        FilterChip(
                            selected = aiMode == mKey,
                            onClick = {
                                aiMode = mKey
                                commitAiChanges()
                            },
                            label = { Text(mLabel, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color(0xFF0F172A),
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }
            }
        }

        // Section 3: Code Editor Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Editor Preferences", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }

                // Font Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Font Size: ${fontSize.toInt()}sp", color = Color(0xFFCBD5E1), fontSize = 13.sp, modifier = Modifier.width(110.dp))
                    Slider(
                        value = fontSize,
                        onValueChange = {
                            fontSize = it
                            commitEditorChanges()
                        },
                        valueRange = 10f..24f,
                        steps = 7,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Tab Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tab Indent Size", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(2, 4).forEach { size ->
                            FilterChip(
                                selected = tabSize == size,
                                onClick = {
                                    tabSize = size
                                    commitEditorChanges()
                                },
                                label = { Text("$size spaces", fontSize = 11.sp) },
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

                // Toggles
                SettingToggleRow("Show Line Numbers", showLineNumbers) { showLineNumbers = it; commitEditorChanges() }
                SettingToggleRow("Auto Indent on Enter", autoIndent) { autoIndent = it; commitEditorChanges() }
                SettingToggleRow("Auto Close Brackets & Quotes", bracketMatching) { bracketMatching = it; commitEditorChanges() }
                SettingToggleRow("Beginner Error Explanations", beginnerMode) { beginnerMode = it; commitEditorChanges() }
            }
        }
    }

    // Key Input Dialog
    if (showKeyInputDialog != null) {
        val targetProvider = showKeyInputDialog!!
        AlertDialog(
            onDismissRequest = { showKeyInputDialog = null },
            title = { Text("Configure ${targetProvider.displayName} Key") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your private API key. It is stored securely on device.", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = keyInputValue,
                        onValueChange = { keyInputValue = it },
                        singleLine = true,
                        placeholder = { Text(if (targetProvider == AiProvider.GEMINI) "AIzaSy..." else "sk-...") },
                        modifier = Modifier.fillMaxWidth().testTag("api_key_dialog_input")
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    when (targetProvider) {
                        AiProvider.GEMINI -> geminiKey = keyInputValue.trim()
                        AiProvider.OPENAI -> openAiKey = keyInputValue.trim()
                        AiProvider.CUSTOM -> customKey = keyInputValue.trim()
                    }
                    commitAiChanges()
                    showKeyInputDialog = null
                    Toast.makeText(context, "API Key saved securely", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeyInputDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFFCBD5E1), fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF38BDF8),
                checkedTrackColor = Color(0xFF0284C7)
            )
        )
    }
}
