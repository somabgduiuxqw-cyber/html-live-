package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ToolsScreen(
    onSourceCode: () -> Unit,
    onInAppBrowser: () -> Unit,
    onHttpRequest: () -> Unit,
    onHtmlToApk: () -> Unit,
    onGitClone: () -> Unit,
    onGitProject: () -> Unit,
    onAiAssistant: () -> Unit,
    onPhotoToCode: () -> Unit,
    onColorLab: () -> Unit,
    onGameStudio: () -> Unit,
    onWebsiteBuilder: () -> Unit,
    onTutorials: () -> Unit,
    onHtmlTags: () -> Unit,
    onHtmlQna: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Developer Tools Hub",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Specialized utilities for web development, inspection & release",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Section 1: Inspection & Web
        ToolSectionHeader("Web Inspection & Network")
        ToolCard(
            title = "Get Webpage Source Code",
            description = "Real HTTP raw source retrieval and live WebView DOM inspector with syntax highlighting, search, and editor export.",
            badge = "CORE TOOL",
            icon = Icons.Default.Source,
            iconTint = Color(0xFFF97316),
            testTag = "tool_card_source_code",
            onClick = onSourceCode
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactToolCard(
                title = "In-App Browser",
                subtitle = "Test responsive sites & URLs",
                icon = Icons.Default.Language,
                iconTint = Color(0xFF38BDF8),
                testTag = "tool_card_browser",
                onClick = onInAppBrowser,
                modifier = Modifier.weight(1f)
            )
            CompactToolCard(
                title = "HTTP Request Tester",
                subtitle = "GET/POST API calls via OkHttp",
                icon = Icons.Default.Http,
                iconTint = Color(0xFF34D399),
                testTag = "tool_card_http",
                onClick = onHttpRequest,
                modifier = Modifier.weight(1f)
            )
        }

        // Section 2: Build & Release
        ToolSectionHeader("Build, Release & Version Control")
        ToolCard(
            title = "HTML → APK Packager",
            description = "Package current project into a signed Android APK with relative asset support, custom icons, and offline WebView capability.",
            badge = "PACKAGER",
            icon = Icons.Default.Android,
            iconTint = Color(0xFF10B981),
            testTag = "tool_card_apk",
            onClick = onHtmlToApk
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactToolCard(
                title = "Git Clone Project",
                subtitle = "Clone GitHub / remote repos",
                icon = Icons.Default.Download,
                iconTint = Color(0xFF818CF8),
                testTag = "tool_card_git_clone",
                onClick = onGitClone,
                modifier = Modifier.weight(1f)
            )
            CompactToolCard(
                title = "Git Repository Hub",
                subtitle = "Branches, commits & diffs",
                icon = Icons.Default.Commit,
                iconTint = Color(0xFFA78BFA),
                testTag = "tool_card_git_repo",
                onClick = onGitProject,
                modifier = Modifier.weight(1f)
            )
        }

        // Section 3: AI & Creative Studios
        ToolSectionHeader("AI & Creative Studios")
        ToolCard(
            title = "AI Coding Assistant",
            description = "Context-aware AI coding companion. Generates code, explains concepts, fixes runtime console errors, and applies patches directly.",
            badge = "AI POWERED",
            icon = Icons.Default.AutoAwesome,
            iconTint = Color(0xFFFBBF24),
            testTag = "tool_card_ai",
            onClick = onAiAssistant
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactToolCard(
                title = "Photo → Code",
                subtitle = "UI layouts from wireframes",
                icon = Icons.Default.PhotoCamera,
                iconTint = Color(0xFFF472B6),
                testTag = "tool_card_photo_code",
                onClick = onPhotoToCode,
                modifier = Modifier.weight(1f)
            )
            CompactToolCard(
                title = "Color Lab",
                subtitle = "Palettes & contrast checker",
                icon = Icons.Default.Palette,
                iconTint = Color(0xFF38BDF8),
                testTag = "tool_card_color_lab",
                onClick = onColorLab,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactToolCard(
                title = "Game Studio",
                subtitle = "2D Canvas games & map editor",
                icon = Icons.Default.SportsEsports,
                iconTint = Color(0xFFE879F9),
                testTag = "tool_card_game_studio",
                onClick = onGameStudio,
                modifier = Modifier.weight(1f)
            )
            CompactToolCard(
                title = "Website Builder",
                subtitle = "Visual section & layout builder",
                icon = Icons.Default.Web,
                iconTint = Color(0xFF60A5FA),
                testTag = "tool_card_website_builder",
                onClick = onWebsiteBuilder,
                modifier = Modifier.weight(1f)
            )
        }

        // Section 4: Learning & Reference
        ToolSectionHeader("Learning & Reference")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactToolCard(
                title = "Interactive Lessons",
                subtitle = "HTML, CSS & JS tutorials",
                icon = Icons.Default.School,
                iconTint = Color(0xFF34D399),
                testTag = "tool_card_lessons",
                onClick = onTutorials,
                modifier = Modifier.weight(1f)
            )
            CompactToolCard(
                title = "HTML5 Tags Guide",
                subtitle = "Directory of all HTML tags",
                icon = Icons.Default.Tag,
                iconTint = Color(0xFF38BDF8),
                testTag = "tool_card_tags",
                onClick = onHtmlTags,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactToolCard(
                title = "Web Dev Q&A",
                subtitle = "Common questions & solutions",
                icon = Icons.Default.QuestionAnswer,
                iconTint = Color(0xFFFBBF24),
                testTag = "tool_card_qna",
                onClick = onHtmlQna,
                modifier = Modifier.weight(1f)
            )
            CompactToolCard(
                title = "Preferences & Keys",
                subtitle = "AI models, API keys & editor",
                icon = Icons.Default.Settings,
                iconTint = Color(0xFF94A3B8),
                testTag = "tool_card_settings",
                onClick = onSettings,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ToolSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF38BDF8),
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun ToolCard(
    title: String,
    description: String,
    badge: String,
    icon: ImageVector,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        color = iconTint.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            badge,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = iconTint,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    description,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun CompactToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.testTag(testTag),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 12.sp, maxLines = 1)
                Text(subtitle, color = Color(0xFF94A3B8), fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}
