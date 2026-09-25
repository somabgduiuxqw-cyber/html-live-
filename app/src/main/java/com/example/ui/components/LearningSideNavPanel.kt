package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.LearningRepository
import com.example.model.CourseCategory
import com.example.model.Lesson
import com.example.model.TutorialModule

@Composable
fun LearningSideNavPanel(
    activeLesson: Lesson?,
    downloadedModuleIds: Set<String>,
    completedLessonIds: Set<String>,
    onSelectLesson: (Lesson) -> Unit,
    onToggleModuleDownload: (String) -> Unit,
    onDownloadAll: () -> Unit,
    onClearOfflineCache: () -> Unit,
    onToggleLessonComplete: (String) -> Unit,
    onCloseDrawer: (() -> Unit)? = null,
    onOpenInEditor: ((title: String, html: String, css: String, js: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf<CourseCategory?>(null) }
    var showOnlyDownloaded by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }

    // Track which modules are expanded in accordion view
    var expandedModuleIds by remember {
        mutableStateOf(
            if (activeLesson != null) setOf(activeLesson.moduleId)
            else setOf(LearningRepository.modules.firstOrNull()?.id ?: "")
        )
    }

    val allModules = remember { LearningRepository.modules }

    // Filter modules based on search, category, and downloaded toggle
    val filteredModules = allModules.filter { module ->
        val matchesCategory = selectedFilterCategory == null || module.category == selectedFilterCategory
        val isDownloaded = downloadedModuleIds.contains(module.id)
        val matchesDownloadFilter = !showOnlyDownloaded || isDownloaded

        val matchesSearch = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            module.title.lowercase().contains(q) ||
                    module.description.lowercase().contains(q) ||
                    module.tags.any { it.lowercase().contains(q) } ||
                    LearningRepository.getLessonsForModule(module.id).any { it.title.lowercase().contains(q) }
        }

        matchesCategory && matchesDownloadFilter && matchesSearch
    }

    val totalStorageKb = allModules.sumOf { it.sizeKb }
    val usedStorageKb = allModules.filter { downloadedModuleIds.contains(it.id) }.sumOf { it.sizeKb }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color(0xFF0F172A))
            .border(width = 1.dp, color = Color(0xFF1E293B))
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Tutorial Modules",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Tutorial Modules",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Offline Curriculum",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Storage management dialog trigger
                Surface(
                    onClick = { showStorageDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.testTag("storage_manager_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = "Manage Storage",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${downloadedModuleIds.size}/${allModules.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }

                if (onCloseDrawer != null) {
                    Spacer(Modifier.width(4.dp))
                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("drawer_close_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }
            }
        }

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tutorials, modules, tags...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("module_search_input")
            )
        }

        // Category & Downloaded Filter Chips Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedFilterCategory == null && !showOnlyDownloaded,
                onClick = {
                    selectedFilterCategory = null
                    showOnlyDownloaded = false
                },
                label = { Text("All", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF38BDF8),
                    selectedLabelColor = Color(0xFF0F172A),
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color(0xFFCBD5E1)
                )
            )

            FilterChip(
                selected = showOnlyDownloaded,
                onClick = { showOnlyDownloaded = !showOnlyDownloaded },
                leadingIcon = {
                    Icon(
                        Icons.Default.DownloadDone,
                        null,
                        tint = if (showOnlyDownloaded) Color(0xFF0F172A) else Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = { Text("Downloaded (${downloadedModuleIds.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF10B981),
                    selectedLabelColor = Color(0xFF0F172A),
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color(0xFFCBD5E1)
                )
            )

            CourseCategory.values().forEach { cat ->
                FilterChip(
                    selected = selectedFilterCategory == cat,
                    onClick = {
                        selectedFilterCategory = if (selectedFilterCategory == cat) null else cat
                    },
                    label = { Text(cat.title.split(" ").first(), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF38BDF8),
                        selectedLabelColor = Color(0xFF0F172A),
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))

        // Modules List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredModules, key = { it.id }) { module ->
                val isExpanded = expandedModuleIds.contains(module.id)
                val isDownloaded = downloadedModuleIds.contains(module.id)
                val moduleLessons = remember(module.id) { LearningRepository.getLessonsForModule(module.id) }
                val completedCount = moduleLessons.count { completedLessonIds.contains(it.id) }
                val hasActiveLesson = moduleLessons.any { it.id == activeLesson?.id }

                ModuleCard(
                    module = module,
                    lessons = moduleLessons,
                    isExpanded = isExpanded,
                    isDownloaded = isDownloaded,
                    hasActiveLesson = hasActiveLesson,
                    activeLessonId = activeLesson?.id,
                    completedLessonIds = completedLessonIds,
                    onToggleExpand = {
                        expandedModuleIds = if (isExpanded) {
                            expandedModuleIds - module.id
                        } else {
                            expandedModuleIds + module.id
                        }
                    },
                    onToggleDownload = { onToggleModuleDownload(module.id) },
                    onSelectLesson = onSelectLesson,
                    onToggleLessonComplete = onToggleLessonComplete,
                    onOpenInEditor = onOpenInEditor
                )
            }

            if (filteredModules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, null, tint = Color(0xFF64748B), modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No matching modules found", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            TextButton(onClick = {
                                searchQuery = ""
                                selectedFilterCategory = null
                                showOnlyDownloaded = false
                            }) {
                                Text("Reset Filters", color = Color(0xFF38BDF8), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Offline Status Banner
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "100% Offline Ready",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "${usedStorageKb} KB / ${totalStorageKb} KB cached",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                if (downloadedModuleIds.size < allModules.size) {
                    OutlinedButton(
                        onClick = onDownloadAll,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Get All", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Storage Management Dialog
    if (showStorageDialog) {
        StorageManagementDialog(
            allModules = allModules,
            downloadedModuleIds = downloadedModuleIds,
            usedStorageKb = usedStorageKb,
            totalStorageKb = totalStorageKb,
            onDismiss = { showStorageDialog = false },
            onDownloadAll = {
                onDownloadAll()
                showStorageDialog = false
            },
            onClearCache = {
                onClearOfflineCache()
                showStorageDialog = false
            }
        )
    }
}

@Composable
private fun ModuleCard(
    module: TutorialModule,
    lessons: List<Lesson>,
    isExpanded: Boolean,
    isDownloaded: Boolean,
    hasActiveLesson: Boolean,
    activeLessonId: String?,
    completedLessonIds: Set<String>,
    onToggleExpand: () -> Unit,
    onToggleDownload: () -> Unit,
    onSelectLesson: (Lesson) -> Unit,
    onToggleLessonComplete: (String) -> Unit,
    onOpenInEditor: ((title: String, html: String, css: String, js: String) -> Unit)?
) {
    val completedCount = lessons.count { completedLessonIds.contains(it.id) }
    val progressRatio = if (lessons.isNotEmpty()) completedCount.toFloat() / lessons.size else 0f

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasActiveLesson) Color(0xFF16253B) else Color(0xFF161F30)
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (hasActiveLesson) Color(0xFF38BDF8).copy(alpha = 0.6f) else Color(0xFF243248)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("module_item_${module.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Module Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = when (module.category) {
                                CourseCategory.HTML -> Color(0xFFE34F26).copy(alpha = 0.2f)
                                CourseCategory.CSS -> Color(0xFF1572B6).copy(alpha = 0.2f)
                                CourseCategory.JAVASCRIPT -> Color(0xFFF7DF1E).copy(alpha = 0.2f)
                                CourseCategory.GAME_DEV -> Color(0xFFA855F7).copy(alpha = 0.2f)
                                CourseCategory.WEB_PROJECTS -> Color(0xFF10B981).copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = module.category.title.split(" ").first(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (module.category) {
                                CourseCategory.HTML -> Color(0xFFFF8A65)
                                CourseCategory.CSS -> Color(0xFF60A5FA)
                                CourseCategory.JAVASCRIPT -> Color(0xFFFDE047)
                                CourseCategory.GAME_DEV -> Color(0xFFD8B4FE)
                                CourseCategory.WEB_PROJECTS -> Color(0xFF6EE7B7)
                            },
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(Modifier.width(6.dp))

                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = module.level,
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = "${module.estimatedMinutes}m",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = module.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Download / Offline Status Action
                    IconButton(
                        onClick = onToggleDownload,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("download_module_btn_${module.id}")
                    ) {
                        if (isDownloaded) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Downloaded offline",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Download module",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Progress bar
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LinearProgressIndicator(
                    progress = { progressRatio },
                    color = if (progressRatio >= 1f) Color(0xFF10B981) else Color(0xFF38BDF8),
                    trackColor = Color(0xFF0F172A),
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$completedCount/${lessons.size}",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Expanded Lesson List
            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFF243248), thickness = 0.5.dp)
                Spacer(Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    lessons.forEach { lesson ->
                        val isLessonActive = lesson.id == activeLessonId
                        val isLessonCompleted = completedLessonIds.contains(lesson.id)

                        Surface(
                            onClick = { onSelectLesson(lesson) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isLessonActive) Color(0xFF1E3A5F) else Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isLessonActive) Color(0xFF38BDF8) else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("lesson_nav_item_${lesson.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onToggleLessonComplete(lesson.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        if (isLessonCompleted) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Completed",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.RadioButtonUnchecked,
                                                contentDescription = "Not completed",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(4.dp))

                                    Column {
                                        Text(
                                            text = lesson.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isLessonActive) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isLessonActive) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${lesson.durationMinutes} min",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                if (isLessonActive) {
                                    Surface(
                                        color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageManagementDialog(
    allModules: List<TutorialModule>,
    downloadedModuleIds: Set<String>,
    usedStorageKb: Int,
    totalStorageKb: Int,
    onDismiss: () -> Unit,
    onDownloadAll: () -> Unit,
    onClearCache: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF38BDF8))
                Spacer(Modifier.width(8.dp))
                Text("Offline Storage Manager", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Manage cached coding courses and practice environments. All modules execute locally in your mobile browser sandbox with zero network required.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(Modifier.height(14.dp))

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Downloaded Modules", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("${downloadedModuleIds.size} of ${allModules.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Local Storage Used", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("${usedStorageKb} KB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (totalStorageKb > 0) usedStorageKb.toFloat() / totalStorageKb else 0f },
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDownloadAll,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DownloadDone, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Download All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onClearCache,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Clear Cache", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF38BDF8))
            }
        }
    )
}
