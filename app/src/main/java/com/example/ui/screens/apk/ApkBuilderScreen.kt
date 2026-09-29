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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import com.example.data.apk.ApkBuildProgress
import com.example.data.apk.ApkBuildStep
import com.example.data.apk.ApkConfig
import com.example.data.apk.ApkValidationResult
import com.example.data.apk.CompatibilityAnalysisResult
import com.example.data.apk.FeatureStatus
import com.example.data.apk.ProjectAnalyzer
import com.example.data.apk.ProjectErrorCheckResult
import com.example.data.apk.ResourceAnalysisResult
import com.example.data.apk.WarningLevel
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

    // Form inputs
    var username by remember(savedUsername) { mutableStateOf(savedUsername.ifBlank { "Developer" }) }
    var appName by remember(activeProject) { mutableStateOf(activeProject?.name ?: "My Web App") }
    var packageName by remember(activeProject) {
        val sanitized = (activeProject?.name ?: "webapp")
            .lowercase()
            .replace("[^a-z0-9]".toRegex(), "")
            .ifEmpty { "webapp" }
        mutableStateOf("com.htmllive.$sanitized")
    }
    var versionName by remember { mutableStateOf("1.0") }
    var versionCode by remember { mutableStateOf(1) }
    var entryFile by remember { mutableStateOf("index.html") }
    var internetAccess by remember { mutableStateOf(true) }
    var showStartupScreen by remember { mutableStateOf(true) }
    var selectedIcon by remember { mutableStateOf("Default Web Icon") }

    // Analysis caches
    var resourceAnalysis by remember(activeFiles) { mutableStateOf<ResourceAnalysisResult?>(null) }
    var compatAnalysis by remember(activeFiles) { mutableStateOf<CompatibilityAnalysisResult?>(null) }
    var projectCheck by remember(activeFiles) { mutableStateOf<ProjectErrorCheckResult?>(null) }

    // Re-run analyses when files change or project is chosen
    LaunchedEffect(activeFiles, entryFile) {
        if (activeFiles.isNotEmpty()) {
            resourceAnalysis = ProjectAnalyzer.analyzeResources(activeFiles)
            compatAnalysis = ProjectAnalyzer.checkCompatibility(activeFiles)
            projectCheck = ProjectAnalyzer.checkProjectErrors(activeFiles, entryFile)
        }
    }

    // Auto-advance or sync step with progress
    LaunchedEffect(buildProgress.step) {
        if (buildProgress.step == ApkBuildStep.PACKAGING || buildProgress.step == ApkBuildStep.VERIFYING || buildProgress.step == ApkBuildStep.READY || buildProgress.step == ApkBuildStep.FAILED) {
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
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("apk_builder_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Android, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "HTML → APK Builder",
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
            }
        }

        // Stepper Progress Indicators
        StepProgressRow(currentStep = currentStep)

        // Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            when (currentStep) {
                ApkBuildStep.SELECT_PROJECT -> {
                    SelectProjectStep(
                        projects = projects,
                        activeProject = activeProject,
                        onSelectProject = {
                            onSelectProject(it)
                            appName = it.name
                            val safe = it.name.lowercase().replace("[^a-z0-9]".toRegex(), "").ifEmpty { "webapp" }
                            packageName = "com.htmllive.$safe"
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
                    ApkSettingsStep(
                        projectName = activeProject?.name ?: "My Project",
                        files = activeFiles,
                        appName = appName,
                        onAppNameChange = { appName = it },
                        packageName = packageName,
                        onPackageNameChange = { packageName = it },
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
                        internetAccess = internetAccess,
                        onInternetAccessChange = { internetAccess = it },
                        showStartupScreen = showStartupScreen,
                        onShowStartupScreenChange = { showStartupScreen = it },
                        selectedIcon = selectedIcon,
                        onSelectIcon = { selectedIcon = it },
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
                        internetAccess = internetAccess,
                        onBack = { currentStep = ApkBuildStep.RESOURCE_CHECK },
                        onStartBuild = {
                            val config = ApkConfig(
                                projectId = activeProject?.id ?: "project",
                                appName = appName.trim(),
                                packageName = packageName.trim(),
                                versionName = versionName.trim(),
                                versionCode = versionCode,
                                username = username.trim(),
                                entryFile = entryFile.trim(),
                                internetAccess = internetAccess,
                                showStartupScreen = showStartupScreen,
                                customIconName = selectedIcon
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
                        onBuildAnother = { currentStep = ApkBuildStep.SELECT_PROJECT }
                    )
                }

                ApkBuildStep.FAILED -> {
                    BuildFailedStep(
                        error = buildProgress.error ?: validationResult?.errorMessage ?: "Unknown error",
                        onRetry = { currentStep = ApkBuildStep.SETTINGS }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 1: Select Project
// -------------------------------------------------------------------------------------------------
@Composable
private fun SelectProjectStep(
    projects: List<Project>,
    activeProject: Project?,
    onSelectProject: (Project) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Step 1 of 8: Select Project to Package", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "Choose the HTML/CSS/JavaScript website or game you want to compile into a lightweight Android APK.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        Text("Your Projects (${projects.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)

        if (projects.isEmpty()) {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Folder, null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No projects found", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Create a project or clone a repository first.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }
        } else {
            projects.forEach { proj ->
                val isSelected = activeProject?.id == proj.id
                Surface(
                    onClick = { onSelectProject(proj) },
                    color = if (isSelected) Color(0xFF0369A1) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_project_${proj.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Folder,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(proj.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(proj.type.name, fontSize = 11.sp, color = if (isSelected) Color(0xFFBAE6FD) else Color(0xFF94A3B8))
                            }
                        }

                        if (isSelected) {
                            Text("SELECTED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onContinue,
            enabled = activeProject != null,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("apk_step1_continue_btn")
        ) {
            Text("Continue to Project Check", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 2: Check Project
// -------------------------------------------------------------------------------------------------
@Composable
private fun CheckProjectStep(
    activeProject: Project?,
    projectCheck: ProjectErrorCheckResult?,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Step 2 of 8: Project Pre-Check", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "Verifying structure and entry points for '${activeProject?.name}'.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        if (projectCheck != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Project Check Results", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)

                    CheckResultRow(
                        isSuccess = projectCheck.hasEntryFile,
                        title = "Entry file '${projectCheck.entryFileName}' found",
                        failMessage = "Missing '${projectCheck.entryFileName}'. Android WebView requires a main HTML entry file."
                    )

                    CheckResultRow(
                        isSuccess = projectCheck.cssValid,
                        title = "CSS stylesheets verified",
                        failMessage = "No CSS file found."
                    )

                    CheckResultRow(
                        isSuccess = projectCheck.jsValid,
                        title = "JavaScript scripts verified",
                        failMessage = "No JS file found."
                    )

                    CheckResultRow(
                        isSuccess = projectCheck.localAssetsFound,
                        title = "Local bundled assets detected",
                        failMessage = "Single file project."
                    )

                    if (projectCheck.externalResourcesFound) {
                        Surface(
                            color = Color(0xFF451A03),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("⚠ External resources detected", fontWeight = FontWeight.Bold, color = Color(0xFFFDE68A), fontSize = 12.sp)
                                    Text("Found ${projectCheck.externalUrls.size} remote URLs. Ensure Internet Access is ON in APK settings.", fontSize = 11.sp, color = Color(0xFFFEF3C7))
                                }
                            }
                        }
                    } else {
                        CheckResultRow(
                            isSuccess = true,
                            title = "✓ 100% Offline Capable (No external resources required)",
                            failMessage = ""
                        )
                    }

                    if (projectCheck.missingFiles.isNotEmpty()) {
                        Surface(
                            color = Color(0xFF450A0A),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("⚠ Potential Broken References:", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                projectCheck.missingFiles.forEach {
                                    Text("• $it", color = Color(0xFFFECACA), fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        CheckResultRow(
                            isSuccess = true,
                            title = "✓ No obvious missing files or broken references",
                            failMessage = ""
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(46.dp)) {
                Text("Back", color = Color(0xFF94A3B8))
            }
            Button(
                onClick = onContinue,
                enabled = projectCheck?.canPackage == true,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f).height(46.dp).testTag("apk_step2_continue_btn")
            ) {
                Text("Continue to Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CheckResultRow(isSuccess: Boolean, title: String, failMessage: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isSuccess) title else failMessage,
            color = if (isSuccess) Color(0xFFE2E8F0) else Color(0xFFFCA5A5),
            fontSize = 12.sp
        )
    }
}

// -------------------------------------------------------------------------------------------------
// Step 3: APK Settings & Username
// -------------------------------------------------------------------------------------------------
@Composable
private fun ApkSettingsStep(
    projectName: String,
    files: List<ProjectFile>,
    appName: String,
    onAppNameChange: (String) -> Unit,
    packageName: String,
    onPackageNameChange: (String) -> Unit,
    versionName: String,
    onVersionNameChange: (String) -> Unit,
    versionCode: Int,
    onVersionCodeChange: (Int) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    entryFile: String,
    onEntryFileChange: (String) -> Unit,
    internetAccess: Boolean,
    onInternetAccessChange: (Boolean) -> Unit,
    showStartupScreen: Boolean,
    onShowStartupScreenChange: (Boolean) -> Unit,
    selectedIcon: String,
    onSelectIcon: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    var packageError by remember { mutableStateOf<String?>(null) }

    val htmlFiles = files.filter { it.extension.equals("html", ignoreCase = true) || it.extension.equals("htm", ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Step 3 of 8: APK Configuration", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "Configure app identity, startup screen, and runtime permissions.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        // Username Card (Requirement 9)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Developer Username", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    placeholder = { Text("e.g. Atp") },
                    singleLine = true,
                    colors = darkTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("apk_username_input")
                )
                Text(
                    text = "Saved locally. Displayed on the optional HTML Live startup screen.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // App & Package Name Card (Requirement 10)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("App Identity", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)

                Column {
                    Text("App Name", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = appName,
                        onValueChange = onAppNameChange,
                        singleLine = true,
                        colors = darkTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("apk_app_name_input")
                    )
                }

                Column {
                    Text("Package Name", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = packageName,
                        onValueChange = {
                            onPackageNameChange(it)
                            packageError = null
                        },
                        singleLine = true,
                        isError = packageError != null,
                        colors = darkTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("apk_package_name_input")
                    )
                    if (packageError != null) {
                        Text(packageError!!, color = Color(0xFFEF4444), fontSize = 11.sp)
                    } else {
                        Text("e.g. com.example.mygame (must have at least 2 segments)", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Version Name", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = versionName,
                            onValueChange = onVersionNameChange,
                            singleLine = true,
                            colors = darkTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Version Code", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = versionCode.toString(),
                            onValueChange = {
                                val n = it.toIntOrNull()
                                if (n != null && n > 0) onVersionCodeChange(n)
                            },
                            singleLine = true,
                            colors = darkTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Column {
                    Text("Main Entry File", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = entryFile,
                        onValueChange = onEntryFileChange,
                        singleLine = true,
                        colors = darkTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Toggles Card (Requirement 7, 9, 10)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Runtime Features", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)

                // Internet Access Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Internet Access", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                        Text(
                            if (internetAccess) "ON: Allows project to fetch remote APIs and online assets."
                            else "OFF: Pure offline mode. Blocks all external network calls.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Switch(
                        checked = internetAccess,
                        onCheckedChange = onInternetAccessChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8), checkedTrackColor = Color(0xFF0369A1))
                    )
                }

                Divider(color = Color(0xFF334155))

                // Startup Screen Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("HTML Live Startup Screen", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                        Text(
                            if (showStartupScreen) "ON: Shows 'HTML LIVE / Welcome, $username / [START]' splash before website."
                            else "OFF: Launches bundled website directly without splash.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Switch(
                        checked = showStartupScreen,
                        onCheckedChange = onShowStartupScreenChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8), checkedTrackColor = Color(0xFF0369A1))
                    )
                }
            }
        }

        // Icon Selection Card (Requirement 11)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("App Launcher Icon", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhoneAndroid, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("HTML Live Adaptive Icon", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                        Text("High-resolution vector adaptive launcher icon bundled.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(46.dp)) {
                Text("Back", color = Color(0xFF94A3B8))
            }
            Button(
                onClick = {
                    val config = ApkConfig(
                        projectId = "temp",
                        appName = appName,
                        packageName = packageName,
                        versionName = versionName,
                        versionCode = versionCode,
                        username = username,
                        entryFile = entryFile,
                        internetAccess = internetAccess,
                        showStartupScreen = showStartupScreen
                    )
                    val err = config.validate()
                    if (err != null) {
                        packageError = err
                    } else {
                        onContinue()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f).height(46.dp).testTag("apk_step3_continue_btn")
            ) {
                Text("Continue to Resource Check", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 4: Resource Analyzer (Requirement 12)
// -------------------------------------------------------------------------------------------------
@Composable
private fun ResourceCheckStep(
    analysis: ResourceAnalysisResult?,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Step 4 of 8: Project Resource Analyzer", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "Detailed size breakdown of bundled website code and media assets.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        if (analysis != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Project Resources", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)

                    ResourceSizeRow("HTML:", analysis.formattedHtmlSize, Color(0xFFF97316))
                    ResourceSizeRow("CSS:", analysis.formattedCssSize, Color(0xFF38BDF8))
                    ResourceSizeRow("JavaScript:", analysis.formattedJsSize, Color(0xFFFACC15))
                    ResourceSizeRow("Images:", analysis.formattedImagesSize, Color(0xFF10B981))
                    ResourceSizeRow("Audio:", analysis.formattedAudioSize, Color(0xFFA855F7))
                    ResourceSizeRow("Fonts:", analysis.formattedFontsSize, Color(0xFFEC4899))

                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Project total (${analysis.totalFilesCount} files):", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text(analysis.formattedTotalSize, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 14.sp)
                    }
                }
            }

            // Warnings Section
            if (analysis.warnings.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Resource Diagnostics (${analysis.warnings.size})", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), fontSize = 13.sp)

                        analysis.warnings.forEach { w ->
                            val color = when (w.level) {
                                WarningLevel.ERROR -> Color(0xFFEF4444)
                                WarningLevel.WARNING -> Color(0xFFF59E0B)
                                WarningLevel.INFO -> Color(0xFF38BDF8)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Warning, null, tint = color, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(w.title, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 12.sp)
                                    Text(w.description, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    color = Color(0xFF064E3B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("All asset sizes within optimal mobile WebView ranges.", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(46.dp)) {
                Text("Back", color = Color(0xFF94A3B8))
            }
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f).height(46.dp).testTag("apk_step4_continue_btn")
            ) {
                Text("Continue to Compatibility Check", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ResourceSizeRow(label: String, value: String, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
            Spacer(Modifier.width(8.dp))
            Text(label, color = Color(0xFFCBD5E1), fontSize = 12.sp)
        }
        Text(value, fontFamily = FontFamily.Monospace, color = Color.White, fontSize = 12.sp)
    }
}

// -------------------------------------------------------------------------------------------------
// Step 5: Compatibility Check (Requirement 13)
// -------------------------------------------------------------------------------------------------
@Composable
private fun CompatibilityCheckStep(
    analysis: CompatibilityAnalysisResult?,
    internetAccess: Boolean,
    onBack: () -> Unit,
    onStartBuild: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Step 5 of 8: Android WebView Compatibility", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "Verifying web runtime feature compatibility on mobile.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        if (analysis != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Compatibility Matrix", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)

                    analysis.features.forEach { feat ->
                        val (icon, color) = when (feat.status) {
                            FeatureStatus.SUPPORTED -> Pair(Icons.Default.CheckCircle, Color(0xFF10B981))
                            FeatureStatus.WARNING -> Pair(Icons.Default.Warning, Color(0xFFF59E0B))
                            FeatureStatus.UNSUPPORTED -> Pair(Icons.Default.Error, Color(0xFFEF4444))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(feat.name, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                                Text(feat.details, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        // Summary banner
        Surface(
            color = Color(0xFF0F2744),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RocketLaunch, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Ready for Lightweight Packaging", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("The reusable WebView template will package and sign this project into a real APK.", fontSize = 11.sp, color = Color(0xFFBAE6FD))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(46.dp)) {
                Text("Back", color = Color(0xFF94A3B8))
            }
            Button(
                onClick = onStartBuild,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f).height(46.dp).testTag("apk_step5_build_btn")
            ) {
                Icon(Icons.Default.Build, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Package APK", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 6 & 7: Packaging & Verifying Progress
// -------------------------------------------------------------------------------------------------
@Composable
private fun PackagingProgressStep(progress: ApkBuildProgress) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.size(54.dp),
                    strokeWidth = 4.dp
                )

                Text(
                    text = if (progress.step == ApkBuildStep.VERIFYING) "Verifying Generated APK..." else "Packaging HTML → APK...",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )

                LinearProgressIndicator(
                    progress = { progress.percentage.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF38BDF8),
                    trackColor = Color(0xFF334155)
                )

                Text(
                    text = progress.statusMessage,
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 8: APK Ready (Requirement 21)
// -------------------------------------------------------------------------------------------------
@Composable
private fun ApkReadyStep(
    validation: ApkValidationResult?,
    appName: String,
    onInstall: () -> Unit,
    onShare: () -> Unit,
    onPreview: () -> Unit,
    onBuildAnother: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF34D399), modifier = Modifier.size(52.dp))
                Spacer(Modifier.height(10.dp))
                Text("APK Ready", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                Text(
                    text = validation?.outputFile?.name ?: "${appName}.apk",
                    fontSize = 13.sp,
                    color = Color(0xFF6EE7B7),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Details Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Package Summary", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("File size:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text(validation?.formattedSize ?: "0 B", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Internet permission:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text(if (validation?.internetEnabled == true) "Enabled" else "Disabled (Offline Only)", color = Color.White, fontSize = 12.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Local files bundled:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text("${validation?.localFilesCount ?: 0}", color = Color.White, fontSize = 12.sp)
                }

                Divider(color = Color(0xFF334155))

                Text("Validation Status", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                CheckResultRow(isSuccess = validation?.hasManifest == true, title = "✓ Valid AndroidManifest.xml", failMessage = "Missing manifest")
                CheckResultRow(isSuccess = validation?.hasClassesDex == true, title = "✓ WebView runtime classes.dex compiled", failMessage = "Missing runtime")
                CheckResultRow(isSuccess = validation?.hasWebsiteAssets == true, title = "✓ Website bundled in assets/website/", failMessage = "Missing assets")
                CheckResultRow(isSuccess = validation?.isSigned == true, title = "✓ Signed with cryptographic signature (v1 JAR)", failMessage = "Unsigned")
            }
        }

        // Actions
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onInstall,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1f).height(46.dp).testTag("apk_install_btn")
            ) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Install", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f).height(46.dp).testTag("apk_share_btn")
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share APK", fontWeight = FontWeight.Bold)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onPreview, modifier = Modifier.weight(1f).height(44.dp)) {
                Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Preview in App", color = Color(0xFF38BDF8))
            }
            OutlinedButton(onClick = onBuildAnother, modifier = Modifier.weight(1f).height(44.dp)) {
                Text("Build Another", color = Color(0xFFCBD5E1))
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step Failed
// -------------------------------------------------------------------------------------------------
@Composable
private fun BuildFailedStep(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Error, null, tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
                Text("APK Creation Failed", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                Text(
                    text = "Reason:\n$error",
                    color = Color(0xFFFECACA),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Return to Settings & Fix")
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Stepper Indicator Row
// -------------------------------------------------------------------------------------------------
@Composable
private fun StepProgressRow(currentStep: ApkBuildStep) {
    val steps = listOf(
        ApkBuildStep.SELECT_PROJECT to "1. Select",
        ApkBuildStep.CHECK_PROJECT to "2. Check",
        ApkBuildStep.SETTINGS to "3. Config",
        ApkBuildStep.RESOURCE_CHECK to "4. Size",
        ApkBuildStep.COMPATIBILITY_CHECK to "5. Compat",
        ApkBuildStep.PACKAGING to "6. Build",
        ApkBuildStep.READY to "7. Ready"
    )

    Surface(color = Color(0xFF161F30), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            steps.forEach { (step, label) ->
                val isCurrent = currentStep == step || (step == ApkBuildStep.PACKAGING && currentStep == ApkBuildStep.VERIFYING)
                val isPassed = currentStep.stepNumber > step.stepNumber

                Surface(
                    color = when {
                        isCurrent -> Color(0xFF0284C7)
                        isPassed -> Color(0xFF065F46)
                        else -> Color(0xFF1E293B)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = label,
                        color = when {
                            isCurrent -> Color.White
                            isPassed -> Color(0xFF6EE7B7)
                            else -> Color(0xFF64748B)
                        },
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun darkTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF38BDF8),
    unfocusedBorderColor = Color(0xFF334155),
    focusedContainerColor = Color(0xFF0F172A),
    unfocusedContainerColor = Color(0xFF0F172A)
)
