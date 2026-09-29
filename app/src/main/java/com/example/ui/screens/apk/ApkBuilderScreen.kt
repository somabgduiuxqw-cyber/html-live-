package com.example.ui.screens.apk

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.apk.ApkBuildProfile
import com.example.data.apk.ApkBuildProgress
import com.example.data.apk.ApkBuildStep
import com.example.data.apk.ApkConfig
import com.example.data.apk.ApkPreset
import com.example.data.apk.ApkValidationResult
import com.example.data.apk.BackButtonBehavior
import com.example.data.apk.CompatibilityAnalysisResult
import com.example.data.apk.DisplayMode
import com.example.data.apk.ExternalLinkBehavior
import com.example.data.apk.FeatureStatus
import com.example.data.apk.IconMode
import com.example.data.apk.InternetAccessMode
import com.example.data.apk.ProjectAnalyzer
import com.example.data.apk.ProjectErrorCheckResult
import com.example.data.apk.ResourceAnalysisResult
import com.example.data.apk.ScreenOrientation
import com.example.data.apk.StartupType
import com.example.data.apk.WarningLevel
import com.example.data.apk.WebRuntimeConfig
import com.example.model.Project
import com.example.model.ProjectFile

@Composable
fun ApkBuilderScreen(
    projects: List<Project>,
    activeProject: Project?,
    activeFiles: List<ProjectFile>,
    savedUsername: String,
    onSaveUsername: (String) -> Unit,
    onSelectProject: (Project) -> Unit,
    onBuildApk: (ApkConfig) -> Unit,
    buildProgress: ApkBuildProgress,
    validationResult: ApkValidationResult?,
    onInstallApk: () -> Unit,
    onShareApk: () -> Unit,
    onOpenProjectPreview: (Project) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(ApkBuildStep.SELECT_PROJECT) }

    // Core Form inputs
    var username by remember(savedUsername) { mutableStateOf(savedUsername.ifBlank { "Developer" }) }
    var appName by remember(activeProject) { mutableStateOf(activeProject?.name ?: "My HTML Game") }
    var packageName by remember(activeProject, username, appName) {
        mutableStateOf(ApkConfig.autoGeneratePackage(username, appName))
    }
    var versionName by remember { mutableStateOf("1.0") }
    var versionCode by remember { mutableStateOf(1) }
    var entryFile by remember { mutableStateOf("index.html") }
    var internetMode by remember { mutableStateOf(InternetAccessMode.OFFLINE_ONLY) }
    var showStartupScreen by remember { mutableStateOf(true) }
    var startupType by remember { mutableStateOf(StartupType.USERNAME) }
    var startupDuration by remember { mutableStateOf(2) }
    var startupBg by remember { mutableStateOf("#0F172A") }
    var orientation by remember { mutableStateOf(ScreenOrientation.LANDSCAPE) }
    var displayMode by remember { mutableStateOf(DisplayMode.FULLSCREEN) }
    var iconMode by remember { mutableStateOf(IconMode.GENERATED) }
    var profile by remember { mutableStateOf(ApkBuildProfile.RELEASE) }
    var externalLinkBehavior by remember { mutableStateOf(ExternalLinkBehavior.OPEN_BROWSER) }
    var backButtonBehavior by remember { mutableStateOf(BackButtonBehavior.BROWSER_HISTORY) }
    var webRuntime by remember { mutableStateOf(WebRuntimeConfig()) }

    // Preset save/load modal
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var savedPresets by remember {
        mutableStateOf(
            listOf(
                ApkPreset(name = "Arcade Game Preset", appName = "My HTML Game", packageName = "com.developer.game", username = "Developer"),
                ApkPreset(name = "Offline Tool Preset", appName = "My Offline App", packageName = "com.developer.app", username = "Developer")
            )
        )
    }

    // Analysis caches
    var resourceAnalysis by remember(activeFiles) { mutableStateOf<ResourceAnalysisResult?>(null) }
    var compatAnalysis by remember(activeFiles) { mutableStateOf<CompatibilityAnalysisResult?>(null) }
    var projectCheck by remember(activeFiles, entryFile) { mutableStateOf<ProjectErrorCheckResult?>(null) }

    LaunchedEffect(activeFiles, entryFile) {
        if (activeFiles.isNotEmpty()) {
            resourceAnalysis = ProjectAnalyzer.analyzeResources(activeFiles)
            compatAnalysis = ProjectAnalyzer.checkCompatibility(activeFiles)
            projectCheck = ProjectAnalyzer.checkProjectErrors(activeFiles, entryFile)
        }
    }

    LaunchedEffect(buildProgress.step) {
        if (buildProgress.step == ApkBuildStep.PACKAGING ||
            buildProgress.step == ApkBuildStep.VERIFYING ||
            buildProgress.step == ApkBuildStep.READY ||
            buildProgress.step == ApkBuildStep.FAILED
        ) {
            currentStep = buildProgress.step
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Header
        Surface(color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("apk_builder_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Android, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "HTML → APK Configuration",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Lightweight offline Android WebView packager",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Profile Badge
                Surface(
                    color = if (profile == ApkBuildProfile.RELEASE) Color(0xFF047857) else Color(0xFFB45309),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = profile.displayName,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Stepper Progress Indicators
        StepProgressRow(currentStep = currentStep)

        // Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            when (currentStep) {
                ApkBuildStep.SELECT_PROJECT -> {
                    SelectProjectStep(
                        projects = projects,
                        activeProject = activeProject,
                        onSelectProject = {
                            onSelectProject(it)
                            appName = it.name
                            packageName = ApkConfig.autoGeneratePackage(username, it.name)
                        },
                        onContinue = { currentStep = ApkBuildStep.CHECK_PROJECT }
                    )
                }

                ApkBuildStep.CHECK_PROJECT -> {
                    CheckProjectStep(
                        activeProject = activeProject,
                        projectCheck = projectCheck,
                        onBack = { currentStep = ApkBuildStep.SELECT_PROJECT },
                        onContinue = { currentStep = ApkBuildStep.SETTINGS }
                    )
                }

                ApkBuildStep.SETTINGS -> {
                    ApkConfigurationStep(
                        projectName = activeProject?.name ?: "My HTML Game",
                        files = activeFiles,
                        appName = appName,
                        onAppNameChange = { appName = it },
                        packageName = packageName,
                        onPackageNameChange = { packageName = it },
                        onAutoGeneratePackage = { packageName = ApkConfig.autoGeneratePackage(username, appName) },
                        versionName = versionName,
                        onVersionNameChange = { versionName = it },
                        versionCode = versionCode,
                        onVersionCodeChange = { versionCode = it },
                        username = username,
                        onUsernameChange = {
                            username = it
                            onSaveUsername(it)
                        },
                        entryFile = entryFile,
                        onEntryFileChange = { entryFile = it },
                        internetMode = internetMode,
                        onInternetModeChange = { internetMode = it },
                        showStartupScreen = showStartupScreen,
                        onShowStartupScreenChange = { showStartupScreen = it },
                        startupType = startupType,
                        onStartupTypeChange = { startupType = it },
                        startupDuration = startupDuration,
                        onStartupDurationChange = { startupDuration = it },
                        startupBg = startupBg,
                        onStartupBgChange = { startupBg = it },
                        orientation = orientation,
                        onOrientationChange = { orientation = it },
                        displayMode = displayMode,
                        onDisplayModeChange = { displayMode = it },
                        iconMode = iconMode,
                        onIconModeChange = { iconMode = it },
                        profile = profile,
                        onProfileChange = { profile = it },
                        externalLinkBehavior = externalLinkBehavior,
                        onExternalLinkBehaviorChange = { externalLinkBehavior = it },
                        backButtonBehavior = backButtonBehavior,
                        onBackButtonBehaviorChange = { backButtonBehavior = it },
                        webRuntime = webRuntime,
                        onWebRuntimeChange = { webRuntime = it },
                        onSavePreset = { showSavePresetDialog = true },
                        savedPresets = savedPresets,
                        onLoadPreset = { preset ->
                            appName = preset.appName
                            packageName = preset.packageName
                            username = preset.username
                            entryFile = preset.entryFile
                        },
                        onBack = { currentStep = ApkBuildStep.CHECK_PROJECT },
                        onContinue = { currentStep = ApkBuildStep.RESOURCE_CHECK }
                    )
                }

                ApkBuildStep.RESOURCE_CHECK -> {
                    ResourceCheckStep(
                        analysis = resourceAnalysis,
                        onBack = { currentStep = ApkBuildStep.SETTINGS },
                        onContinue = { currentStep = ApkBuildStep.COMPATIBILITY_CHECK }
                    )
                }

                ApkBuildStep.COMPATIBILITY_CHECK -> {
                    CompatibilityCheckStep(
                        analysis = compatAnalysis,
                        internetMode = internetMode,
                        onBack = { currentStep = ApkBuildStep.RESOURCE_CHECK },
                        onContinue = { currentStep = ApkBuildStep.PREVIEW }
                    )
                }

                ApkBuildStep.PREVIEW -> {
                    BuildPreviewStep(
                        appName = appName,
                        packageName = packageName,
                        username = username,
                        versionName = versionName,
                        versionCode = versionCode,
                        entryFile = entryFile,
                        internetMode = internetMode,
                        orientation = orientation,
                        displayMode = displayMode,
                        iconMode = iconMode,
                        resourceAnalysis = resourceAnalysis,
                        onBackToEdit = { currentStep = ApkBuildStep.SETTINGS },
                        onBuildApk = {
                            val config = ApkConfig(
                                projectId = activeProject?.id ?: "project",
                                appName = appName.trim(),
                                packageName = packageName.trim(),
                                versionName = versionName.trim(),
                                versionCode = versionCode,
                                username = username.trim(),
                                entryFile = entryFile.trim(),
                                internetAccess = internetMode != InternetAccessMode.OFFLINE_ONLY,
                                internetMode = internetMode,
                                showStartupScreen = showStartupScreen,
                                startupType = startupType,
                                startupDurationSeconds = startupDuration,
                                startupBackgroundColor = startupBg,
                                orientation = orientation,
                                displayMode = displayMode,
                                iconMode = iconMode,
                                profile = profile,
                                externalLinkBehavior = externalLinkBehavior,
                                backButtonBehavior = backButtonBehavior,
                                webRuntime = webRuntime
                            )
                            onBuildApk(config)
                        }
                    )
                }

                ApkBuildStep.PACKAGING, ApkBuildStep.VERIFYING -> {
                    PackagingProgressStep(progress = buildProgress)
                }

                ApkBuildStep.READY -> {
                    ApkReadyStep(
                        validation = validationResult,
                        appName = appName,
                        onInstall = onInstallApk,
                        onShare = onShareApk,
                        onPreview = {
                            if (activeProject != null) onOpenProjectPreview(activeProject)
                        },
                        onBuildAnother = { currentStep = ApkBuildStep.SELECT_PROJECT },
                        onEditConfig = { currentStep = ApkBuildStep.SETTINGS }
                    )
                }

                ApkBuildStep.FAILED -> {
                    BuildFailedStep(
                        error = buildProgress.error ?: validationResult?.errorMessage ?: "Build verification failed.",
                        onRetry = { currentStep = ApkBuildStep.SETTINGS }
                    )
                }
            }
        }
    }

    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save APK Preset", color = Color.White) },
            text = {
                Column {
                    Text("Save this configuration as a reusable preset:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Preset Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            savedPresets = savedPresets + ApkPreset(
                                name = presetNameInput.trim(),
                                appName = appName,
                                packageName = packageName,
                                username = username,
                                entryFile = entryFile
                            )
                            showSavePresetDialog = false
                            presetNameInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun StepProgressRow(currentStep: ApkBuildStep) {
    val steps = listOf(
        ApkBuildStep.SELECT_PROJECT,
        ApkBuildStep.CHECK_PROJECT,
        ApkBuildStep.SETTINGS,
        ApkBuildStep.RESOURCE_CHECK,
        ApkBuildStep.COMPATIBILITY_CHECK,
        ApkBuildStep.PREVIEW,
        ApkBuildStep.PACKAGING,
        ApkBuildStep.READY
    )

    Surface(color = Color(0xFF162032), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                val isCompleted = currentStep.ordinal > step.ordinal || currentStep == ApkBuildStep.READY
                val isCurrent = currentStep == step ||
                    (step == ApkBuildStep.PACKAGING && (currentStep == ApkBuildStep.PACKAGING || currentStep == ApkBuildStep.VERIFYING))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> Color(0xFF10B981)
                                    isCurrent -> Color(0xFF3B82F6)
                                    else -> Color(0xFF334155)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Text(
                                text = "${step.stepNumber}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = step.title,
                        color = if (isCurrent) Color.White else Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                    if (index < steps.size - 1) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(1.dp)
                                .background(Color(0xFF334155))
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SelectProjectStep(
    projects: List<Project>,
    activeProject: Project?,
    onSelectProject: (Project) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("1. Select Project to Package", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Choose the HTML5 website or game to convert into an installable Android APK.", fontSize = 12.sp, color = Color(0xFF94A3B8))
        Spacer(Modifier.height(12.dp))

        if (projects.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Folder, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No projects found in HTML Live workspace.", color = Color.White, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                items(projects) { proj ->
                    val isSelected = proj.id == activeProject?.id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1E3A5F) else Color(0xFF1E293B)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectProject(proj) }
                            .testTag("select_project_${proj.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF10B981) else Color(0xFF334155)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Code, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(proj.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "Type: ${proj.type.name} • Entry: ${proj.lastOpenedFile}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onContinue,
            enabled = activeProject != null,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("continue_to_check_btn")
        ) {
            Text("Continue to Project Check")
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun CheckProjectStep(
    activeProject: Project?,
    projectCheck: ProjectErrorCheckResult?,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("2. Pre-Build Project Validation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Verifying project structure, relative links, HTML/CSS/JS syntax, and assets.", fontSize = 12.sp, color = Color(0xFF94A3B8))
        Spacer(Modifier.height(12.dp))

        if (projectCheck != null) {
            val canPackage = projectCheck.canPackage
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (canPackage) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (canPackage) Icons.Default.CheckCircle else Icons.Default.Error,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (canPackage) "Build can continue" else "Build blocked",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (canPackage) "Project passes essential structural requirements for WebView."
                            else (projectCheck.fatalMessage ?: "Project has fatal structural problems."),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Verification checklist
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    CheckItemRow("Entry HTML file exists", projectCheck.hasEntryFile, projectCheck.entryFileName)
                    CheckItemRow("CSS syntax check", projectCheck.cssValid, "Stylesheets loaded")
                    CheckItemRow("JavaScript syntax check", projectCheck.jsValid, "Scripts verified")
                    CheckItemRow("Local Assets bundled", projectCheck.localAssetsFound, "Images & resources")
                    CheckItemRow("Offline self-contained", !projectCheck.externalResourcesFound, if (projectCheck.externalResourcesFound) "${projectCheck.externalUrls.size} external URLs" else "Zero external dependencies")
                }
            }

            if (projectCheck.missingFiles.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Missing Local Assets Referenced:", color = Color(0xFFFDBA74), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        projectCheck.missingFiles.forEach {
                            Text("• $it", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back", color = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onContinue,
                enabled = projectCheck?.canPackage == true,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1.5f)
            ) {
                Text("Configure APK")
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun CheckItemRow(title: String, passed: Boolean, details: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
            null,
            tint = if (passed) Color(0xFF10B981) else Color(0xFFF59E0B),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(title, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(details, color = Color(0xFF94A3B8), fontSize = 11.sp)
    }
}

@Composable
fun ApkConfigurationStep(
    projectName: String,
    files: List<ProjectFile>,
    appName: String,
    onAppNameChange: (String) -> Unit,
    packageName: String,
    onPackageNameChange: (String) -> Unit,
    onAutoGeneratePackage: () -> Unit,
    versionName: String,
    onVersionNameChange: (String) -> Unit,
    versionCode: Int,
    onVersionCodeChange: (Int) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    entryFile: String,
    onEntryFileChange: (String) -> Unit,
    internetMode: InternetAccessMode,
    onInternetModeChange: (InternetAccessMode) -> Unit,
    showStartupScreen: Boolean,
    onShowStartupScreenChange: (Boolean) -> Unit,
    startupType: StartupType,
    onStartupTypeChange: (StartupType) -> Unit,
    startupDuration: Int,
    onStartupDurationChange: (Int) -> Unit,
    startupBg: String,
    onStartupBgChange: (String) -> Unit,
    orientation: ScreenOrientation,
    onOrientationChange: (ScreenOrientation) -> Unit,
    displayMode: DisplayMode,
    onDisplayModeChange: (DisplayMode) -> Unit,
    iconMode: IconMode,
    onIconModeChange: (IconMode) -> Unit,
    profile: ApkBuildProfile,
    onProfileChange: (ApkBuildProfile) -> Unit,
    externalLinkBehavior: ExternalLinkBehavior,
    onExternalLinkBehaviorChange: (ExternalLinkBehavior) -> Unit,
    backButtonBehavior: BackButtonBehavior,
    onBackButtonBehaviorChange: (BackButtonBehavior) -> Unit,
    webRuntime: WebRuntimeConfig,
    onWebRuntimeChange: (WebRuntimeConfig) -> Unit,
    onSavePreset: () -> Unit,
    savedPresets: List<ApkPreset>,
    onLoadPreset: (ApkPreset) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val packageError = remember(packageName) { ApkConfig.validatePackageName(packageName) }
    val appNameError = remember(appName) { if (appName.isBlank()) "App name cannot be blank." else null }
    val htmlFiles = remember(files) { files.filter { it.extension in listOf("html", "htm") }.map { it.name } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("3. Real APK Customization", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Customize branding, metadata, runtime behavior, and permissions.", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
            OutlinedButton(onClick = onSavePreset) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(14.dp), tint = Color(0xFF10B981))
                Spacer(Modifier.width(4.dp))
                Text("Save Preset", fontSize = 11.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Preset selector bar
        if (savedPresets.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                savedPresets.forEach { p ->
                    FilterChip(
                        selected = false,
                        onClick = { onLoadPreset(p) },
                        label = { Text(p.name, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Bookmark, null, modifier = Modifier.size(12.dp)) },
                        colors = FilterChipDefaults.filterChipColors(labelColor = Color.White)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Section 1: Creator & App Identity
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("CREATOR & APP IDENTITY", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))

                // Creator / Username
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = { Text("Creator / Username (e.g. Atp)") },
                    supportingText = { Text("Exposed safely to JS via window.HTMLLive.username and startup splash", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // App Name
                OutlinedTextField(
                    value = appName,
                    onValueChange = onAppNameChange,
                    label = { Text("App Name (e.g. My HTML Game)") },
                    isError = appNameError != null,
                    supportingText = { if (appNameError != null) Text(appNameError, color = Color(0xFFEF4444)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // Package Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = packageName,
                        onValueChange = onPackageNameChange,
                        label = { Text("Package Name (e.g. com.atp.mygame)") },
                        isError = packageError != null,
                        supportingText = {
                            if (packageError != null) {
                                Text(packageError, color = Color(0xFFEF4444), fontSize = 11.sp)
                            } else {
                                Text("Valid Android package format", color = Color(0xFF10B981), fontSize = 11.sp)
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = onAutoGeneratePackage) {
                        Icon(Icons.Default.Refresh, contentDescription = "Auto Generate", tint = Color(0xFF10B981))
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Section 2: Versioning & Profile
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("VERSION & BUILD PROFILE", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = versionName,
                        onValueChange = onVersionNameChange,
                        label = { Text("Version Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = versionCode.toString(),
                        onValueChange = { onVersionCodeChange(it.toIntOrNull() ?: versionCode) },
                        label = { Text("Version Code") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { onVersionCodeChange(versionCode + 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Increment Version Code (${versionCode + 1})", fontSize = 12.sp)
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Build Profile:", color = Color.White, fontSize = 13.sp)
                    Spacer(Modifier.width(8.dp))
                    ApkBuildProfile.values().forEach { p ->
                        FilterChip(
                            selected = profile == p,
                            onClick = { onProfileChange(p) },
                            label = { Text(p.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                labelColor = Color.White
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Section 3: App Icon
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("APP ICON", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Actual live icon preview
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = appName.trim().firstOrNull()?.uppercase() ?: "A",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Icon Preview", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(iconMode.displayName, color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    IconMode.values().forEach { mode ->
                        FilterChip(
                            selected = iconMode == mode,
                            onClick = { onIconModeChange(mode) },
                            label = { Text(mode.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                labelColor = Color.White
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Section 4: Startup Screen & Splash
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("STARTUP & SPLASH SCREEN", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Show username on startup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Displays 'HTML Live / Welcome, $username'", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = showStartupScreen,
                        onCheckedChange = onShowStartupScreenChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981))
                    )
                }

                if (showStartupScreen) {
                    Spacer(Modifier.height(8.dp))
                    // Duration selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Duration:", color = Color.White, fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                        listOf(1, 2, 3, 5).forEach { sec ->
                            FilterChip(
                                selected = startupDuration == sec,
                                onClick = { onStartupDurationChange(sec) },
                                label = { Text("${sec}s", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF10B981),
                                    labelColor = Color.White
                                ),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    // Startup preview box
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("HTML Live", color = Color(0xFF10B981), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text("Welcome, $username", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("$appName • v$versionName", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Section 5: Orientation, Display & Web Runtime
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("ORIENTATION & DISPLAY MODE", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))

                Text("Screen Orientation:", color = Color.White, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    ScreenOrientation.values().forEach { o ->
                        FilterChip(
                            selected = orientation == o,
                            onClick = { onOrientationChange(o) },
                            label = { Text(o.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF10B981), labelColor = Color.White),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Display Mode (Immersive for Games):", color = Color.White, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    DisplayMode.values().forEach { d ->
                        FilterChip(
                            selected = displayMode == d,
                            onClick = { onDisplayModeChange(d) },
                            label = { Text(d.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF10B981), labelColor = Color.White),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Startup HTML File:", color = Color.White, fontSize = 12.sp)
                if (htmlFiles.isNotEmpty()) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        htmlFiles.forEach { file ->
                            FilterChip(
                                selected = entryFile == file,
                                onClick = { onEntryFileChange(file) },
                                label = { Text(file, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF10B981), labelColor = Color.White),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = entryFile,
                        onValueChange = onEntryFileChange,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Section 6: Permissions & Runtime Navigation
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("INTERNET & PERMISSIONS", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))

                Text("Internet Access:", color = Color.White, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    InternetAccessMode.values().forEach { mode ->
                        FilterChip(
                            selected = internetMode == mode,
                            onClick = { onInternetModeChange(mode) },
                            label = { Text(mode.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF10B981), labelColor = Color.White),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Back Button Behavior:", color = Color.White, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    BackButtonBehavior.values().forEach { b ->
                        FilterChip(
                            selected = backButtonBehavior == b,
                            onClick = { onBackButtonBehaviorChange(b) },
                            label = { Text(b.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF10B981), labelColor = Color.White),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back", color = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onContinue,
                enabled = packageError == null && appNameError == null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1.5f)
            ) {
                Text("Check Resources")
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ResourceCheckStep(
    analysis: ResourceAnalysisResult?,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("4. Project Size & Asset Breakdown", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Calculated from bundled HTML, CSS, JavaScript, media, and fonts.", fontSize = 12.sp, color = Color(0xFF94A3B8))
        Spacer(Modifier.height(12.dp))

        if (analysis != null) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Project size:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(analysis.formattedTotalSize, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                    SizeRow("HTML", analysis.formattedHtmlSize)
                    SizeRow("CSS", analysis.formattedCssSize)
                    SizeRow("JavaScript", analysis.formattedJsSize)
                    SizeRow("Images", analysis.formattedImagesSize)
                    SizeRow("Audio", analysis.formattedAudioSize)
                    SizeRow("Other", analysis.formattedOtherSize)

                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Expected APK size:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(analysis.formattedEstimatedApkSize, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back", color = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1.5f)
            ) {
                Text("Compatibility Check")
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SizeRow(label: String, formattedSize: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 13.sp)
        Text(formattedSize, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
fun CompatibilityCheckStep(
    analysis: CompatibilityAnalysisResult?,
    internetMode: InternetAccessMode,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("5. Compatibility Report", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Feature-by-feature evaluation for Android WebView runtime.", fontSize = 12.sp, color = Color(0xFF94A3B8))
        Spacer(Modifier.height(12.dp))

        if (analysis != null) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    analysis.features.forEach { feat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(feat.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            val isSupported = feat.status == FeatureStatus.SUPPORTED
                            Icon(
                                if (isSupported) Icons.Default.Check else Icons.Default.Warning,
                                null,
                                tint = if (isSupported) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(feat.details, color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.padding(bottom = 6.dp))
                        Divider(color = Color(0xFF2D3748))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back", color = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1.5f)
            ) {
                Text("Review Preview")
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun BuildPreviewStep(
    appName: String,
    packageName: String,
    username: String,
    versionName: String,
    versionCode: Int,
    entryFile: String,
    internetMode: InternetAccessMode,
    orientation: ScreenOrientation,
    displayMode: DisplayMode,
    iconMode: IconMode,
    resourceAnalysis: ResourceAnalysisResult?,
    onBackToEdit: () -> Unit,
    onBuildApk: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("6. Final Build Preview", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Verify all parameters before packing into an authentic Android APK.", fontSize = 12.sp, color = Color(0xFF94A3B8))
        Spacer(Modifier.height(12.dp))

        // Center Preview Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appName.trim().firstOrNull()?.uppercase() ?: "A",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(appName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Spacer(Modifier.height(4.dp))
                Text("Welcome, $username", color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text("Version $versionName (Code $versionCode)", color = Color(0xFF94A3B8), fontSize = 12.sp)

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 14.dp))

                PreviewMetaRow("Package:", packageName)
                PreviewMetaRow("Startup:", entryFile)
                PreviewMetaRow("Internet:", internetMode.displayName)
                PreviewMetaRow("Orientation:", orientation.displayName)
                PreviewMetaRow("Display:", displayMode.displayName)
                PreviewMetaRow("Project size:", resourceAnalysis?.formattedTotalSize ?: "0 B")
                PreviewMetaRow("Expected APK:", resourceAnalysis?.formattedEstimatedApkSize ?: "~2.5 MB")
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBackToEdit, modifier = Modifier.weight(1f)) {
                Text("Back to Edit", color = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onBuildApk,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1.5f)
            ) {
                Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Build APK")
            }
        }
    }
}

@Composable
fun PreviewMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PackagingProgressStep(progress: ApkBuildProgress) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            progress = { progress.percentage },
            color = Color(0xFF10B981),
            strokeWidth = 6.dp,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = progress.statusMessage.ifBlank { "Packaging Android APK..." },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress.percentage },
            color = Color(0xFF10B981),
            trackColor = Color(0xFF334155),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${(progress.percentage * 100).toInt()}% completed",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )
    }
}

@Composable
fun ApkReadyStep(
    validation: ApkValidationResult?,
    appName: String,
    onInstall: () -> Unit,
    onShare: () -> Unit,
    onPreview: () -> Unit,
    onBuildAnother: () -> Unit,
    onEditConfig: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Success Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(10.dp))
                Text("APK Built Successfully!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Real genuine signed APK generated and verified.", color = Color(0xFFD1FAE5), fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // 10-Point Verification Checklist
        if (validation != null && validation.verificationChecks.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("10-POINT BUILD VERIFICATION", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    validation.verificationChecks.forEach { check ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (check.passed) Icons.Default.CheckCircle else Icons.Default.Error,
                                null,
                                tint = if (check.passed) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(check.title, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(check.details, color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Action Buttons
        Button(
            onClick = onInstall,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Test APK (Install / Open)")
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Share APK", color = Color.White, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onPreview, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.PhoneAndroid, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Test in Editor", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onEditConfig, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Tune, null, modifier = Modifier.size(14.dp), tint = Color(0xFF94A3B8))
                Spacer(Modifier.width(4.dp))
                Text("Edit Configuration", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
            TextButton(onClick = onBuildAnother, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp), tint = Color(0xFF94A3B8))
                Spacer(Modifier.width(4.dp))
                Text("Build Another", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun BuildFailedStep(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Error, null, tint = Color(0xFFEF4444), modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(14.dp))
        Text("Build Failed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)), modifier = Modifier.fillMaxWidth()) {
            Text(text = error, color = Color(0xFFFCA5A5), fontSize = 13.sp, modifier = Modifier.padding(14.dp))
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Modify Configuration & Retry")
        }
    }
}
