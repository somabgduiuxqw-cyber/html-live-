package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.ColorLabState

@Composable
fun ColorLabView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var red by remember { mutableIntStateOf(66) }
    var green by remember { mutableIntStateOf(135) }
    var blue by remember { mutableIntStateOf(245) }
    var alpha by remember { mutableFloatStateOf(1.0f) }

    val colorState = remember(red, green, blue, alpha) {
        ColorLabState(red, green, blue, alpha)
    }

    fun copyToClipboard(label: String, value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(context, "Copied $value to clipboard", Toast.LENGTH_SHORT).show()
    }

    val samplePalettes = listOf(
        listOf("#38bdf8", "#3b82f6", "#1d4ed8", "#0284c7"),
        listOf("#4ade80", "#22c55e", "#16a34a", "#15803d"),
        listOf("#f43f5e", "#e11d48", "#be123c", "#9f1239"),
        listOf("#a855f7", "#9333ea", "#7e22ce", "#6b21a8"),
        listOf("#fbbf24", "#f59e0b", "#d97706", "#b45309"),
        listOf("#0f172a", "#1e293b", "#334155", "#475569")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("Color Laboratory", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Text("Real-time HEX, RGB, HSL conversions and color education", fontSize = 13.sp, color = Color(0xFF94A3B8))

        Spacer(Modifier.height(16.dp))

        // Large Color Preview Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(colorState.composeColor)
                .border(2.dp, Color(0xFF334155), MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = colorState.hexString,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if ((red * 0.299 + green * 0.587 + blue * 0.114) > 150) Color.Black else Color.White,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(16.dp))

        // Copy Value Badges
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorValueCard("HEX", colorState.hexString) { copyToClipboard("HEX", colorState.hexString) }
            ColorValueCard("RGB", colorState.rgbString) { copyToClipboard("RGB", colorState.rgbString) }
            ColorValueCard("RGBA", colorState.rgbaString) { copyToClipboard("RGBA", colorState.rgbaString) }
            ColorValueCard("HSL", colorState.hslString) { copyToClipboard("HSL", colorState.hslString) }
        }

        Spacer(Modifier.height(20.dp))

        // Sliders Section
        Text("Fine Tune Channels", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)

        ColorSliderRow("Red (R)", red, 0..255, Color(0xFFEF4444)) { red = it }
        ColorSliderRow("Green (G)", green, 0..255, Color(0xFF10B981)) { green = it }
        ColorSliderRow("Blue (B)", blue, 0..255, Color(0xFF3B82F6)) { blue = it }
        AlphaSliderRow("Alpha (A)", alpha) { alpha = it }

        Spacer(Modifier.height(20.dp))

        // Preset Swatches
        Text("Curated Web Palettes", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            samplePalettes.forEach { palette ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    palette.forEach { hex ->
                        val parsed = runCatching {
                            android.graphics.Color.parseColor(hex)
                        }.getOrDefault(android.graphics.Color.GRAY)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(parsed))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                .clickable {
                                    red = android.graphics.Color.red(parsed)
                                    green = android.graphics.Color.green(parsed)
                                    blue = android.graphics.Color.blue(parsed)
                                }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Color Education Card (#RRGGBB)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Understanding #RRGGBB Hexadecimal",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "In HTML & CSS, hexadecimal colors are represented as 6 characters: #RRGGBB.\n\n" +
                            "• RR (Red): 00 (0) to FF (255)\n" +
                            "• GG (Green): 00 (0) to FF (255)\n" +
                            "• BB (Blue): 00 (0) to FF (255)\n\n" +
                            "Current values: " +
                            String.format("RR=%02X (%d), GG=%02X (%d), BB=%02X (%d)", red, red, green, green, blue, blue),
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ColorValueCard(label: String, value: String, onCopy: () -> Unit) {
    Surface(
        color = Color(0xFF1E293B),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(50.dp))
                Text(value, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
            IconButton(onClick = onCopy, modifier = Modifier.size(28.dp).testTag("copy_color_${label.lowercase()}")) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy $label", tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ColorSliderRow(label: String, value: Int, range: ClosedRange<Int>, tintColor: Color, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = tintColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = tintColor,
                activeTrackColor = tintColor,
                inactiveTrackColor = Color(0xFF334155)
            )
        )
        Text(
            text = "$value",
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.width(36.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun AlphaSliderRow(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFA855F7),
                activeTrackColor = Color(0xFFA855F7),
                inactiveTrackColor = Color(0xFF334155)
            )
        )
        Text(
            text = String.format("%.2f", value),
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.width(36.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
