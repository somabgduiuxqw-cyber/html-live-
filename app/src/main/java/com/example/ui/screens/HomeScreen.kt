package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.repository.TemplateProject
import com.example.data.repository.TemplateRepository
import com.example.model.Project

@Composable
fun HomeScreen(
    recentProjects: List<Project>,
    userExperienceLevel: String,
    onExperienceLevelSelected: (String) -> Unit,
    onOpenProject: (Project) -> Unit,
    // Core Project & Editor
    onMyProjects: () -> Unit,
    onQuickEditor: () -> Unit,
    onCodeEditor: () -> Unit,
    onLivePreview: () -> Unit,
    onSourceCode: () -> Unit,
    // Learning & Reference
    onHtmlExamples: () -> Unit,
    onHtmlTutorials: () -> Unit,
    onHtmlTags: () -> Unit,
    onHtmlQna: () -> Unit,
    // Git Suite
    onGitCloneProject: () -> Unit,
    onGitProjects: () -> Unit,
    onGitStatus: () -> Unit,
    // Tools
    onHttpRequest: () -> Unit,
    onPhotoToCode: () -> Unit,
    onInAppBrowser: () -> Unit,
    onColorLab: () -> Unit,
    // AI Suite
    onAiAssistant: () -> Unit,
    onAiWebsiteBuilder: () -> Unit,
    onAiGameBuilder: () -> Unit,
    // Builders
    onWebsiteBuilder: () -> Unit,
    onGameBuilder: () -> Unit,
    onGameMapEditor: () -> Unit,
    onSelectTemplate: (TemplateProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val experienceLevels = listOf(
        "I'm completely new",
        "I know a little HTML",
        "I know HTML/CSS",
        "I know JavaScript",
        "I'm an advanced developer"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Hero Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF38BDF8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Code, null, tint = Color(0xFF0F172A), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("HTML Live IDE & Studio", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Web, Canvas Games & Full Git Version Control", fontSize = 12.sp, color = Color(0xFF38BDF8))
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Build production-ready websites, clone GitHub repositories, execute JavaScript with error console, and build games with real Git integration.",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(14.dp))

                Text("Your Coding Background:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    experienceLevels.forEach { level ->
                        val isSelected = userExperienceLevel.equals(level, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onExperienceLevelSelected(level) },
                            label = { Text(level, fontSize = 11.sp) },
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

        // -------------------------------------------------------------
        // SECTION: GIT INTEGRATION (Requirements 41, 68, 69)
        // -------------------------------------------------------------
        SectionHeader(title = "Git & GitHub Suite", subtitle = "Clone, commit, pull, and branch real Git repositories")

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                title = "GIT CLONE PROJECT",
                subtitle = "Import from GitHub or remote",
                icon = Icons.Default.Download,
                accentColor = Color(0xFF38BDF8),
                onClick = onGitCloneProject,
                modifier = Modifier.weight(1f).testTag("action_git_clone")
            )
            ActionTile(
                title = "GIT PROJECTS",
                subtitle = "Manage cloned repos & branches",
                icon = Icons.Default.Commit,
                accentColor = Color(0xFF10B981),
                onClick = onGitProjects,
                modifier = Modifier.weight(1f).testTag("action_git_projects")
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            ActionTile(
                title = "GIT STATUS",
                subtitle = "Inspect modified files & diffs",
                icon = Icons.Default.Refresh,
                accentColor = Color(0xFFF59E0B),
                onClick = onGitStatus,
                modifier = Modifier.fillMaxWidth().testTag("action_git_status")
            )
        }

        // -------------------------------------------------------------
        // SECTION: CORE DEVELOPMENT & EDITORS
        // -------------------------------------------------------------
        SectionHeader(title = "Core IDE & Editors", subtitle = "Code, live preview, source inspector, and projects")

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                title = "MY PROJECTS",
                subtitle = "Saved websites & games",
                icon = Icons.Default.Folder,
                accentColor = Color(0xFF38BDF8),
                onClick = onMyProjects,
                modifier = Modifier.weight(1f).testTag("action_my_projects")
            )
            ActionTile(
                title = "QUICK EDITOR",
                subtitle = "Rapid scratchpad editing",
                icon = Icons.Default.Speed,
                accentColor = Color(0xFFA855F7),
                onClick = onQuickEditor,
                modifier = Modifier.weight(1f).testTag("action_quick_editor")
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                title = "CODE EDITOR",
                subtitle = "Full-featured multi-file editor",
                icon = Icons.Default.Code,
                accentColor = Color(0xFF0284C7),
                onClick = onCodeEditor,
                modifier = Modifier.weight(1f).testTag("action_code_editor")
            )
            ActionTile(
                title = "LIVE PREVIEW",
                subtitle = "Real-time web & game runner",
                icon = Icons.Default.PlayArrow,
                accentColor = Color(0xFF10B981),
                onClick = onLivePreview,
                modifier = Modifier.weight(1f).testTag("action_live_preview")
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            ActionTile(
                title = "SOURCE CODE",
                subtitle = "Inspect raw HTML, CSS & JavaScript",
                icon = Icons.Default.Source,
                accentColor = Color(0xFFF97316),
                onClick = onSourceCode,
                modifier = Modifier.fillMaxWidth().testTag("action_source_code")
            )
        }

        // -------------------------------------------------------------
        // SECTION: LEARNING, TAGS & EXAMPLES
        // -------------------------------------------------------------
        SectionHeader(title = "Learning & Documentation", subtitle = "Interactive courses, tags reference, examples & Q&A")

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                title = "HTML EXAMPLES",
                subtitle = "Interactive demos & sandboxes",
                icon = Icons.Default.Style,
                accentColor = Color(0xFFEC4899),
                onClick = onHtmlExamples,
                modifier = Modifier.weight(1f).testTag("action_html_examples")
            )
            ActionTile(
                title = "HTML TUTORIALS",
                subtitle = "Offline lessons with challenges",
                icon = Icons.Default.School,
                accentColor = Color(0xFF38BDF8),
                onClick = onHtmlTutorials,
                modifier = Modifier.weight(1f).testTag("action_html_tutorials")
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                title = "HTML TAGS",
                subtitle = "Searchable HTML5 tags directory",
                icon = Icons.Default.Terminal,
                accentColor = Color(0xFFF97316),
                onClick = onHtmlTags,
                modifier = Modifier.weight(1f).testTag("action_html_tags")
            )
            ActionTile(
                title = "HTML Q&A",
                subtitle = "Web development interview Q&A",
                icon = Icons.Default.QuestionAnswer,
                accentColor = Color(0xFFA855F7),
                onClick = onHtmlQna,
                modifier = Modifier.weight(1f).testTag("action_html_qna")
            )
        }

        // -------------------------------------------------------------
        // SECTION: DEVELOPER TOOLS
        // -------------------------------------------------------------
        SectionHeader(title = "Developer Utility Lab", subtitle = "HTTP network tester, wireframe scanner & browser")

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                title = "HTTP REQUEST",
                subtitle = "Test REST APIs & save JSON",
                icon = Icons.Default.Http,
                accentColor = Color(0xFF38BDF8),
                onClick = onHttpRequest,
                modifier = Modifier.weight(1f).testTag("action_http_request")
            )
            ActionTile(
                title = "PHOTO TO CODE",
                subtitle = "Scan layout wireframes to HTML",
                icon = Icons.Default.PhotoCamera,
                accentColor = Color(0xFFF59E0B),
                onClick = onPhotoToCode,
                modifier = Modifier.weight(1f).testTag("action_photo_to_code")
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                title = "IN-APP BROWSER",
                subtitle = "Inspect live external websites",
                icon = Icons.Default.Language,
                accentColor = Color(0xFF10B981),
                onClick = onInAppBrowser,
                modifier = Modifier.weight(1f).testTag("action_in_app_browser")
            )
            ActionTile(
                title = "COLOR LAB",
                subtitle = "HEX, RGB, HSL color palette tool",
                icon = Icons.Default.Palette,
                accentColor = Color(0xFFEC4899),
                onClick = onColorLab,
                modifier = Modifier.weight(1f).testTag("action_color_lab")
            )
        }

        // -------------------------------------------------------------
        // SECTION: AI INTELLIGENCE
        // -------------------------------------------------------------
        SectionHeader(title = "AI Coding Engine", subtitle = "Describe features and generate verified code")

        Row(modifier = Modifier.fillMaxWidth()) {
            ActionTile(
                title = "AI ASSISTANT",
                subtitle = "Intelligent pair programming & bug fixing",
                icon = Icons.Default.AutoAwesome,
                accentColor = Color(0xFFF59E0B),
                onClick = onAiAssistant,
                modifier = Modifier.fillMaxWidth().testTag("action_ai_assistant")
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                title = "AI WEBSITE BUILDER",
                subtitle = "Generate full websites via prompts",
                icon = Icons.Default.Web,
                accentColor = Color(0xFF38BDF8),
                onClick = onAiWebsiteBuilder,
                modifier = Modifier.weight(1f).testTag("action_ai_website_builder")
            )
            ActionTile(
                title = "AI GAME BUILDER",
                subtitle = "Generate Canvas games with physics",
                icon = Icons.Default.SportsEsports,
                accentColor = Color(0xFF10B981),
                onClick = onAiGameBuilder,
                modifier = Modifier.weight(1f).testTag("action_ai_game_builder")
            )
        }

        // -------------------------------------------------------------
        // SECTION: VISUAL BUILDERS & GAME MAPS
        // -------------------------------------------------------------
        SectionHeader(title = "Visual Builders & Game Studio", subtitle = "Visual page builder & 2D tile map editor")

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                title = "WEBSITE BUILDER",
                subtitle = "Drag & drop visual block editor",
                icon = Icons.Default.Web,
                accentColor = Color(0xFFA855F7),
                onClick = onWebsiteBuilder,
                modifier = Modifier.weight(1f).testTag("action_website_builder")
            )
            ActionTile(
                title = "GAME BUILDER",
                subtitle = "Canvas 2D arcade studio",
                icon = Icons.Default.SportsEsports,
                accentColor = Color(0xFF10B981),
                onClick = onGameBuilder,
                modifier = Modifier.weight(1f).testTag("action_game_builder")
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            ActionTile(
                title = "GAME MAP EDITOR",
                subtitle = "Paint tile grids & export JSON to game",
                icon = Icons.Default.GridOn,
                accentColor = Color(0xFF38BDF8),
                onClick = onGameMapEditor,
                modifier = Modifier.fillMaxWidth().testTag("action_game_map_editor")
            )
        }

        // -------------------------------------------------------------
        // SECTION: RECENT PROJECTS
        // -------------------------------------------------------------
        if (recentProjects.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Projects", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                OutlinedButton(onClick = onMyProjects) {
                    Text("View All", fontSize = 11.sp, color = Color(0xFF38BDF8))
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recentProjects.take(5)) { proj ->
                    Surface(
                        onClick = { onOpenProject(proj) },
                        color = Color(0xFF1E293B),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.width(180.dp).testTag("recent_project_${proj.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(proj.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1)
                            Spacer(Modifier.height(4.dp))
                            Text(proj.type.name, fontSize = 10.sp, color = Color(0xFF38BDF8))
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Resume Coding", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            }
                        }
                    }
                }
            }
        }

        // Starter Templates
        Text("Starter Templates", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(TemplateRepository.templates) { template ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.width(220.dp),
                    onClick = { onSelectTemplate(template) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(template.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1)
                        Spacer(Modifier.height(4.dp))
                        Text(template.description, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 2)
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onSelectTemplate(template) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.fillMaxWidth().height(32.dp)
                        ) {
                            Text("Open Template", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
        Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF334155), modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = Color(0xFF94A3B8), maxLines = 1)
        }
    }
}
