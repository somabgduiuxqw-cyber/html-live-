package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameLevelMap
import com.example.model.MapTileType

@Composable
fun GameStudioView(
    onGenerateGameWithAi: (gamePrompt: String) -> Unit,
    onInjectLevelMapIntoProject: (json: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1E293B),
            contentColor = Color(0xFF38BDF8)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Game Creation Wizard") },
                icon = { Icon(Icons.Default.SportsEsports, null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("2D Map Editor") },
                icon = { Icon(Icons.Default.GridOn, null) }
            )
        }

        if (selectedTab == 0) {
            GameWizardContent(onGenerate = onGenerateGameWithAi)
        } else {
            GameMapEditorContent(onInject = onInjectLevelMapIntoProject)
        }
    }
}

@Composable
private fun GameWizardContent(onGenerate: (String) -> Unit) {
    var gameType by remember { mutableStateOf("2D Platformer") }
    var orientation by remember { mutableStateOf("Portrait") }
    var controls by remember { mutableStateOf("On-Screen Touch D-Pad + Buttons") }
    var mechanics by remember { mutableStateOf("Gravity, coin collection, moving platforms, and high score") }
    var visualStyle by remember { mutableStateOf("Retro Pixel / Cyberpunk Neon") }

    val gameTypes = listOf("2D Platformer", "Retro Snake", "Flappy Arcade", "Pong 2-Player", "Endless Runner", "Top-Down Maze", "Brick Breaker", "Clicker Game")
    val orientations = listOf("Portrait", "Landscape", "Responsive")
    val controlOptions = listOf("On-Screen Touch D-Pad + Buttons", "Single Tap Jump", "Swipe Gestures", "Keyboard + Touch")
    val styles = listOf("Retro Pixel / Cyberpunk Neon", "Minimalist Vector", "Dark Fantasy", "Arcade 80s")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Game Creation Wizard", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Configure your mobile canvas game and launch the generator.", fontSize = 13.sp, color = Color(0xFF94A3B8))

        // Step 1: Game Type
        WizardStepCard(step = "1", title = "Game Type") {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                gameTypes.forEach { type ->
                    FilterChip(
                        selected = gameType == type,
                        onClick = { gameType = type },
                        label = { Text(type, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8),
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }
        }

        // Step 2: Orientation
        WizardStepCard(step = "2", title = "Screen Orientation") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                orientations.forEach { o ->
                    FilterChip(
                        selected = orientation == o,
                        onClick = { orientation = o },
                        label = { Text(o, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981),
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }
        }

        // Step 3: Controls
        WizardStepCard(step = "3", title = "Touch & Input Controls") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                controlOptions.forEach { c ->
                    FilterChip(
                        selected = controls == c,
                        onClick = { controls = c },
                        label = { Text(c, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFA855F7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }
        }

        // Step 4: Mechanics
        WizardStepCard(step = "4", title = "Mechanics & Features") {
            OutlinedTextField(
                value = mechanics,
                onValueChange = { mechanics = it },
                label = { Text("Describe gameplay mechanics") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Step 5: Visual Theme
        WizardStepCard(step = "5", title = "Visual Style & Theme") {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                styles.forEach { s ->
                    FilterChip(
                        selected = visualStyle == s,
                        onClick = { visualStyle = s },
                        label = { Text(s, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF59E0B),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }
        }

        // Step 6: Generate Button
        Button(
            onClick = {
                val prompt = "Create a complete, polished, playable HTML5 Canvas mobile web game.\n" +
                        "Type: $gameType\n" +
                        "Orientation: $orientation\n" +
                        "Controls: $controls (must have on-screen touch buttons for mobile)\n" +
                        "Mechanics: $mechanics\n" +
                        "Visual Style: $visualStyle\n" +
                        "Include index.html, style.css, and script.js with physics, score HUD, and game loop."
                onGenerate(prompt)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A)),
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("wizard_generate_game_btn")
        ) {
            Icon(Icons.Default.AutoAwesome, null)
            Spacer(Modifier.width(8.dp))
            Text("Generate $gameType Project", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun WizardStepCard(step: String, title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF38BDF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(step, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

// 2D Map Editor
@Composable
private fun GameMapEditorContent(onInject: (String) -> Unit) {
    val context = LocalContext.current
    val gridWidth = 14
    val gridHeight = 8
    val mapGrid = remember {
        mutableStateListOf<MutableList<MapTileType>>().apply {
            for (y in 0 until gridHeight) {
                val row = mutableStateListOf<MapTileType>()
                for (x in 0 until gridWidth) {
                    row.add(if (y == gridHeight - 1) MapTileType.GROUND else MapTileType.EMPTY)
                }
                add(row)
            }
        }
    }

    var selectedTileType by remember { mutableStateOf(MapTileType.PLATFORM) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("2D Game Map Editor", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Design game levels visually on a grid. Tap cells to place tiles.", fontSize = 13.sp, color = Color(0xFF94A3B8))

        Spacer(Modifier.height(16.dp))

        // Tile Palette
        Text("Select Active Tile", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapTileType.values().forEach { tile ->
                val parsedColor = runCatching { Color(android.graphics.Color.parseColor(tile.colorHex)) }.getOrDefault(Color.Gray)
                FilterChip(
                    selected = selectedTileType == tile,
                    onClick = { selectedTileType = tile },
                    label = { Text("${tile.codeChar} ${tile.label}", fontSize = 11.sp) },
                    leadingIcon = {
                        Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(parsedColor))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = parsedColor,
                        selectedLabelColor = if (tile == MapTileType.COIN) Color.Black else Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Grid Board
        Surface(
            color = Color(0xFF090D16),
            shape = MaterialTheme.shapes.small,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (y in 0 until gridHeight) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (x in 0 until gridWidth) {
                            val tile = mapGrid[y][x]
                            val tileColor = runCatching { Color(android.graphics.Color.parseColor(tile.colorHex)) }.getOrDefault(Color.DarkGray)

                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(tileColor)
                                    .clickable {
                                        mapGrid[y][x] = selectedTileType
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (tile != MapTileType.EMPTY) {
                                    Text(
                                        text = "${tile.codeChar}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tile == MapTileType.COIN) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Export Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val map = GameLevelMap(gridWidth, gridHeight, mapGrid.map { it.toMutableList() }.toMutableList())
                    val json = map.toJsonData()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Level JSON", json))
                    Toast.makeText(context, "Level JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                modifier = Modifier.weight(1f).testTag("copy_map_json_btn")
            ) {
                Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy JSON", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    val map = GameLevelMap(gridWidth, gridHeight, mapGrid.map { it.toMutableList() }.toMutableList())
                    onInject(map.toJsonData())
                    Toast.makeText(context, "Map injected into project script.js!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1f).testTag("inject_map_btn")
            ) {
                Icon(Icons.Default.Code, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Inject to Code", fontSize = 12.sp)
            }

            IconButton(
                onClick = {
                    for (y in 0 until gridHeight) {
                        for (x in 0 until gridWidth) {
                            mapGrid[y][x] = if (y == gridHeight - 1) MapTileType.GROUND else MapTileType.EMPTY
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Grid", tint = Color(0xFFEF4444))
            }
        }
    }
}
