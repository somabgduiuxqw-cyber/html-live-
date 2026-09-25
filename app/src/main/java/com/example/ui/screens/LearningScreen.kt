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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.LearningRepository
import com.example.model.CourseCategory
import com.example.model.Lesson

@Composable
fun LearningScreen(
    onOpenInEditor: (title: String, html: String, css: String, js: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(CourseCategory.HTML) }
    var activeLesson by remember { mutableStateOf<Lesson?>(null) }

    val categoryLessons = remember(selectedCategory) {
        LearningRepository.getLessonsForCategory(selectedCategory)
    }

    if (activeLesson != null) {
        LessonDetailView(
            lesson = activeLesson!!,
            onBack = { activeLesson = null },
            onTryInEditor = {
                onOpenInEditor(
                    activeLesson!!.title,
                    activeLesson!!.defaultHtml,
                    activeLesson!!.defaultCss,
                    activeLesson!!.defaultJs
                )
            }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Developer Academy", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text("Complete offline curriculum from beginner to canvas game developer", fontSize = 12.sp, color = Color(0xFF94A3B8))

            Spacer(Modifier.height(14.dp))

            // Category Tabs Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CourseCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.title, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8),
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Course Overview Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(selectedCategory.title, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(selectedCategory.description, fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${categoryLessons.size} Interactive Modules", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        Text("100% Offline Ready", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Lessons List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categoryLessons) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        onClick = { activeLesson = lesson }
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: Lesson, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_card_${lesson.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(lesson.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(lesson.summary, fontSize = 12.sp, color = Color(0xFF94A3B8), maxLines = 2)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = Color(0xFF0F172A), shape = MaterialTheme.shapes.extraSmall) {
                        Text(lesson.level, fontSize = 10.sp, color = Color(0xFF38BDF8), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Surface(color = Color(0xFF0F172A), shape = MaterialTheme.shapes.extraSmall) {
                        Text("${lesson.durationMinutes} min", fontSize = 10.sp, color = Color(0xFFCBD5E1), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun LessonDetailView(
    lesson: Lesson,
    onBack: () -> Unit,
    onTryInEditor: () -> Unit
) {
    var showHint by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("lesson_back_btn")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(lesson.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${lesson.level} • ${lesson.durationMinutes} mins", fontSize = 11.sp, color = Color(0xFF38BDF8))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Theory Section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(lesson.theoryMarkdown, fontSize = 13.sp, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Code Example Preview Box
        Text("Interactive Example Code", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(6.dp))
        Surface(
            color = Color(0xFF0B1120),
            shape = MaterialTheme.shapes.small,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = lesson.defaultHtml,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Action: Try In Editor
        Button(
            onClick = onTryInEditor,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF0F172A)),
            modifier = Modifier.fillMaxWidth().testTag("try_in_editor_btn")
        ) {
            Icon(Icons.Default.Code, null)
            Spacer(Modifier.width(8.dp))
            Text("Try in Live Editor", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(Modifier.height(20.dp))

        // Common Mistakes Section
        if (lesson.commonMistakes.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1E1E)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Common Pitfalls to Avoid", fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5), fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    lesson.commonMistakes.forEach { m ->
                        Text("• $m", fontSize = 12.sp, color = Color(0xFFFECACA), modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Mini Challenge Box
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Hands-On Challenge", fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), fontSize = 14.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(lesson.challengeQuestion, fontSize = 13.sp, color = Color.White)

                Spacer(Modifier.height(10.dp))
                if (showHint) {
                    Text("Hint: ${lesson.challengeHint}", fontSize = 12.sp, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace)
                } else {
                    OutlinedButton(onClick = { showHint = true }) {
                        Icon(Icons.Default.HelpOutline, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Show Hint", fontSize = 11.sp, color = Color(0xFF38BDF8))
                    }
                }
            }
        }
    }
}
