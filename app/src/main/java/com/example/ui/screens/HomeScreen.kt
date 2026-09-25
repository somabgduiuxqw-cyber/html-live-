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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onStartLearning: () -> Unit,
    onCreateWebsite: () -> Unit,
    onCreateGame: () -> Unit,
    onAskAi: () -> Unit,
    onExploreProjects: () -> Unit,
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF38BDF8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Code, null, tint = Color(0xFF0F172A), modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Welcome to HTML Live", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Mobile-first Web & Canvas Game IDE", fontSize = 12.sp, color = Color(0xFF38BDF8))
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Build real websites, execute actual JavaScript, craft 2D canvas games, and learn web development with an intelligent coding engine.",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(16.dp))

                // User Experience Selector
                Text("Select your coding background:", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
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

        // Primary Quick Actions Grid
        Text("Quick Actions", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                title = "Start Learning",
                subtitle = "HTML, CSS, JS & Canvas",
                icon = Icons.Default.School,
                accentColor = Color(0xFF38BDF8),
                onClick = onStartLearning,
                modifier = Modifier.weight(1f).testTag("home_start_learning_btn")
            )
            ActionTile(
                title = "Create Game",
                subtitle = "Canvas 2D Studio",
                icon = Icons.Default.SportsEsports,
                accentColor = Color(0xFF10B981),
                onClick = onCreateGame,
                modifier = Modifier.weight(1f).testTag("home_create_game_btn")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                title = "Create Website",
                subtitle = "Visual Builder or Code",
                icon = Icons.Default.Web,
                accentColor = Color(0xFFA855F7),
                onClick = onCreateWebsite,
                modifier = Modifier.weight(1f).testTag("home_create_website_btn")
            )
            ActionTile(
                title = "Ask AI to Build",
                subtitle = "Describe and generate",
                icon = Icons.Default.AutoAwesome,
                accentColor = Color(0xFFF59E0B),
                onClick = onAskAi,
                modifier = Modifier.weight(1f).testTag("home_ask_ai_btn")
            )
        }

        // Recent Projects Section
        if (recentProjects.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Projects", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                OutlinedButton(onClick = onExploreProjects) {
                    Text("View All", fontSize = 11.sp, color = Color(0xFF38BDF8))
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recentProjects.take(4)) { proj ->
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

        // Starter Templates Showcase
        Text("Explore Working Templates", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
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
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}
