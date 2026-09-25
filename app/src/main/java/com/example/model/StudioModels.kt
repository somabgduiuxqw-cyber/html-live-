package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

data class ColorLabState(
    val red: Int = 59,
    val green: Int = 130,
    val blue: Int = 246,
    val alpha: Float = 1.0f
) {
    val hexString: String
        get() = String.format("#%02X%02X%02X", red, green, blue)

    val hex8String: String
        get() = String.format("#%02X%02X%02X%02X", (alpha * 255).roundToInt(), red, green, blue)

    val rgbString: String
        get() = "rgb($red, $green, $blue)"

    val rgbaString: String
        get() = "rgba($red, $green, $blue, ${String.format("%.2f", alpha)})"

    val hslValues: Triple<Int, Int, Int>
        get() {
            val r = red / 255f
            val g = green / 255f
            val b = blue / 255f
            val max = maxOf(r, g, b)
            val min = minOf(r, g, b)
            val delta = max - min

            var h = 0f
            if (delta != 0f) {
                h = when (max) {
                    r -> ((g - b) / delta) % 6f
                    g -> ((b - r) / delta) + 2f
                    else -> ((r - g) / delta) + 4f
                } * 60f
                if (h < 0) h += 360f
            }

            val l = (max + min) / 2f
            val s = if (delta == 0f) 0f else delta / (1f - kotlin.math.abs(2f * l - 1f))

            return Triple(h.roundToInt(), (s * 100).roundToInt(), (l * 100).roundToInt())
        }

    val hslString: String
        get() {
            val (h, s, l) = hslValues
            return "hsl($h°, $s%, $l%)"
        }

    val composeColor: Color
        get() = Color(red, green, blue, (alpha * 255).roundToInt())
}

// Game Map Editor Models
enum class MapTileType(val label: String, val codeChar: Char, val colorHex: String) {
    EMPTY("Empty", '.', "#1e293b"),
    PLAYER("Player", 'P', "#3b82f6"),
    GROUND("Ground", '#', "#854d0e"),
    PLATFORM("Platform", '=', "#64748b"),
    COIN("Coin", '*', "#eab308"),
    ENEMY("Enemy", 'E', "#ef4444"),
    SPIKE("Hazard", '^', "#dc2626"),
    GOAL("Goal Flag", 'G', "#22c55e"),
    KEY("Key", 'K', "#a855f7")
}

data class GameLevelMap(
    val width: Int = 16,
    val height: Int = 10,
    val grid: MutableList<MutableList<MapTileType>> = MutableList(height) {
        MutableList(width) { MapTileType.EMPTY }
    }
) {
    fun toAscii(): String {
        return grid.joinToString("\n") { row ->
            row.map { it.codeChar }.joinToString("")
        }
    }

    fun toJsonData(): String {
        val objects = mutableListOf<String>()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val tile = grid[y][x]
                if (tile != MapTileType.EMPTY) {
                    objects.add("""{"type": "${tile.name}", "x": $x, "y": $y}""")
                }
            }
        }
        return """{"width": $width, "height": $height, "tiles": [${objects.joinToString(", ")}]}"""
    }
}

// Website Visual Builder Models
enum class WebsiteSectionType(val displayName: String, val description: String) {
    NAVBAR("Navigation Bar", "Header with logo, navigation links, and theme toggle"),
    HERO("Hero Banner", "Main attention-grabbing headline, subtitle, and CTA buttons"),
    FEATURES("Feature Cards", "3-column grid highlighting key benefits or features"),
    TEXT_IMAGE("Content Split", "Side-by-side headline & description with an image"),
    STATS("Stat Counter", "Numerical metrics showing achievements or stats"),
    GALLERY("Photo Grid", "Responsive image showcase with captions"),
    CTA("Call to Action", "High-contrast action banner prompting the user to act"),
    CONTACT("Contact Form", "Interactive inquiry form with inputs and submit button"),
    FOOTER("Footer", "Links, copyright info, and social buttons")
}

data class WebsiteSection(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: WebsiteSectionType,
    val title: String,
    val subtitle: String = "",
    val buttonText: String = "",
    val bgColorHex: String = "#0f172a",
    val textColorHex: String = "#f8fafc",
    val accentColorHex: String = "#38bdf8"
)
