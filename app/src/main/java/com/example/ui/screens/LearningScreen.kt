package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.LearningRepository
import com.example.data.storage.SecurePreferences
import com.example.model.CourseCategory
import com.example.model.Lesson
import com.example.ui.components.LearningSideNavPanel

@Composable
fun LearningScreen(
    onOpenInEditor: (title: String, html: String, css: String, js: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences = remember { SecurePreferences(context) }

    var downloadedModuleIds by remember { mutableStateOf(preferences.getDownloadedModuleIds()) }
    var completedLessonIds by remember { mutableStateOf(preferences.getCompletedLessonIds()) }

    var selectedCategory by remember { mutableStateOf(CourseCategory.HTML) }
    var activeLesson by remember { mutableStateOf<Lesson?>(null) }
    var isSideNavOpen by remember { mutableStateOf(false) }

    // Intercept back button: close drawer first, then back from lesson detail
    BackHandler(enabled = isSideNavOpen || activeLesson != null) {
        if (isSideNavOpen) {
            isSideNavOpen = false
        } else if (activeLesson != null) {
            activeLesson = null
        }
    }

    val categoryLessons = remember(selectedCategory) {
        LearningRepository.getLessonsForCategory(selectedCategory)
    }

    fun handleToggleModuleDownload(moduleId: String) {
        downloadedModuleIds = if (downloadedModuleIds.contains(moduleId)) {
            downloadedModuleIds - moduleId
        } else {
            downloadedModuleIds + moduleId
        }
        preferences.setDownloadedModuleIds(downloadedModuleIds)
    }

    fun handleDownloadAll() {
        val allIds = LearningRepository.modules.map { it.id }.toSet()
        downloadedModuleIds = allIds
        preferences.setDownloadedModuleIds(downloadedModuleIds)
    }

    fun handleClearOfflineCache() {
        downloadedModuleIds = emptySet()
        preferences.setDownloadedModuleIds(downloadedModuleIds)
    }

    fun handleToggleLessonComplete(lessonId: String) {
        completedLessonIds = if (completedLessonIds.contains(lessonId)) {
            completedLessonIds - lessonId
        } else {
            completedLessonIds + lessonId
        }
        preferences.setCompletedLessonIds(completedLessonIds)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        val isWideScreen = maxWidth >= 768.dp

        if (isWideScreen) {
            // Tablet / Desktop Dual-Pane Layout: Left Side Nav Panel + Right Content Area
            Row(modifier = Modifier.fillMaxSize()) {
                LearningSideNavPanel(
                    activeLesson = activeLesson,
                    downloadedModuleIds = downloadedModuleIds,
                    completedLessonIds = completedLessonIds,
                    onSelectLesson = { lesson -> activeLesson = lesson },
                    onToggleModuleDownload = { handleToggleModuleDownload(it) },
                    onDownloadAll = { handleDownloadAll() },
                    onClearOfflineCache = { handleClearOfflineCache() },
                    onToggleLessonComplete = { handleToggleLessonComplete(it) },
                    onCloseDrawer = null,
                    onOpenInEditor = onOpenInEditor,
                    modifier = Modifier.width(320.dp).fillMaxHeight()
                )

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (activeLesson != null) {
                        LessonDetailView(
                            lesson = activeLesson!!,
                            isCompleted = completedLessonIds.contains(activeLesson!!.id),
                            onBack = { activeLesson = null },
                            onToggleComplete = { handleToggleLessonComplete(activeLesson!!.id) },
                            onOpenSideNav = { isSideNavOpen = true },
                            onNavigateNext = {
                                val next = LearningRepository.getNextLesson(activeLesson!!.id)
                                if (next != null) activeLesson = next
                            },
                            onNavigatePrevious = {
                                val prev = LearningRepository.getPreviousLesson(activeLesson!!.id)
                                if (prev != null) activeLesson = prev
                            },
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
                        MainLearningCurriculumView(
                            selectedCategory = selectedCategory,
                            categoryLessons = categoryLessons,
                            completedLessonIds = completedLessonIds,
                            downloadedModuleCount = downloadedModuleIds.size,
                            onSelectCategory = { selectedCategory = it },
                            onSelectLesson = { activeLesson = it },
                            onOpenSideNav = { isSideNavOpen = true }
                        )
                    }
                }
            }
        } else {
            // Compact Mobile Layout: Full-Screen Content + Sliding Side Navigation Drawer
            Box(modifier = Modifier.fillMaxSize()) {
                if (activeLesson != null) {
                    LessonDetailView(
                        lesson = activeLesson!!,
                        isCompleted = completedLessonIds.contains(activeLesson!!.id),
                        onBack = { activeLesson = null },
                        onToggleComplete = { handleToggleLessonComplete(activeLesson!!.id) },
                        onOpenSideNav = { isSideNavOpen = true },
                        onNavigateNext = {
                            val next = LearningRepository.getNextLesson(activeLesson!!.id)
                            if (next != null) activeLesson = next
                        },
                        onNavigatePrevious = {
                            val prev = LearningRepository.getPreviousLesson(activeLesson!!.id)
                            if (prev != null) activeLesson = prev
                        },
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
                    MainLearningCurriculumView(
                        selectedCategory = selectedCategory,
                        categoryLessons = categoryLessons,
                        completedLessonIds = completedLessonIds,
                        downloadedModuleCount = downloadedModuleIds.size,
                        onSelectCategory = { selectedCategory = it },
                        onSelectLesson = { activeLesson = it },
                        onOpenSideNav = { isSideNavOpen = true }
                    )
                }

                // Modal Sliding Side Navigation Panel
                if (isSideNavOpen) {
                    // Dark semi-transparent scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { isSideNavOpen = false }
                    )

                    // Side Navigation Panel
                    AnimatedVisibility(
                        visible = isSideNavOpen,
                        enter = slideInHorizontally(initialOffsetX = { -it }),
                        exit = slideOutHorizontally(targetOffsetX = { -it })
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(320.dp)
                        ) {
                            LearningSideNavPanel(
                                activeLesson = activeLesson,
                                downloadedModuleIds = downloadedModuleIds,
                                completedLessonIds = completedLessonIds,
                                onSelectLesson = { lesson ->
                                    activeLesson = lesson
                                    isSideNavOpen = false
                                },
                                onToggleModuleDownload = { handleToggleModuleDownload(it) },
                                onDownloadAll = { handleDownloadAll() },
                                onClearOfflineCache = { handleClearOfflineCache() },
                                onToggleLessonComplete = { handleToggleLessonComplete(it) },
                                onCloseDrawer = { isSideNavOpen = false },
                                onOpenInEditor = onOpenInEditor,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainLearningCurriculumView(
    selectedCategory: CourseCategory,
    categoryLessons: List<Lesson>,
    completedLessonIds: Set<String>,
    downloadedModuleCount: Int,
    onSelectCategory: (CourseCategory) -> Unit,
    onSelectLesson: (Lesson) -> Unit,
    onOpenSideNav: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        // Top Header with Side Navigation Panel Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenSideNav,
                    modifier = Modifier.testTag("open_modules_drawer_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Modules Panel",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Developer Academy",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Interactive offline coding modules",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Quick side navigation trigger chip
            Surface(
                onClick = onOpenSideNav,
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                modifier = Modifier.testTag("quick_modules_panel_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Modules ($downloadedModuleCount)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }

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
                    onClick = { onSelectCategory(cat) },
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
                    Text("${categoryLessons.size} Interactive Lessons", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DownloadDone, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("100% Offline Ready", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
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
                val isCompleted = completedLessonIds.contains(lesson.id)
                LessonCard(
                    lesson = lesson,
                    isCompleted = isCompleted,
                    onClick = { onSelectLesson(lesson) }
                )
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: Lesson, isCompleted: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF243248)
        ),
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
                    if (isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
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
    isCompleted: Boolean,
    onBack: () -> Unit,
    onToggleComplete: () -> Unit,
    onOpenSideNav: () -> Unit,
    onNavigateNext: () -> Unit,
    onNavigatePrevious: () -> Unit,
    onTryInEditor: () -> Unit
) {
    var showHint by remember { mutableStateOf(false) }

    val nextLesson = remember(lesson.id) { LearningRepository.getNextLesson(lesson.id) }
    val prevLesson = remember(lesson.id) { LearningRepository.getPreviousLesson(lesson.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Navigation bar in detail view
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("lesson_back_btn")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = lesson.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${lesson.level} • ${lesson.durationMinutes} mins",
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Modules side nav button
                IconButton(
                    onClick = onOpenSideNav,
                    modifier = Modifier.testTag("detail_open_modules_btn")
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = "Switch Module",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Complete toggle
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.testTag("toggle_complete_btn")
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = if (isCompleted) "Completed" else "Mark Complete",
                        tint = if (isCompleted) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

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

        Spacer(Modifier.height(16.dp))

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

        Spacer(Modifier.height(20.dp))

        // Navigation Footer: Previous & Next Lesson
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (prevLesson != null) {
                OutlinedButton(
                    onClick = onNavigatePrevious,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
                    modifier = Modifier.testTag("prev_lesson_btn")
                ) {
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Previous", fontSize = 12.sp)
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            if (nextLesson != null) {
                Button(
                    onClick = onNavigateNext,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A)),
                    modifier = Modifier.testTag("next_lesson_btn")
                ) {
                    Text("Next Lesson", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
