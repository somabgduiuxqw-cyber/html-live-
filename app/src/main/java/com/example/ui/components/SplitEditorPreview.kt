package com.example.ui.components

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class ViewDisplayMode {
    SPLIT,
    EDITOR_ONLY,
    PREVIEW_ONLY
}

@Composable
fun SplitEditorPreview(
    mode: ViewDisplayMode,
    editorContent: @Composable () -> Unit,
    previewContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val externalDisplays = remember {
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        dm?.displays?.filter { it.displayId != Display.DEFAULT_DISPLAY } ?: emptyList()
    }

    when (mode) {
        ViewDisplayMode.EDITOR_ONLY -> {
            Box(modifier = modifier.fillMaxSize()) {
                editorContent()
            }
        }
        ViewDisplayMode.PREVIEW_ONLY -> {
            Box(modifier = modifier.fillMaxSize()) {
                previewContent()
            }
        }
        ViewDisplayMode.SPLIT -> {
            var splitRatio by remember { mutableFloatStateOf(0.5f) }

            BoxWithConstraints(modifier = modifier.fillMaxSize()) {
                val totalHeight = maxHeight

                Column(modifier = Modifier.fillMaxSize()) {
                    // Top: Editor
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(totalHeight * splitRatio)
                    ) {
                        editorContent()
                    }

                    // Draggable Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .background(Color(0xFF1E293B))
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val deltaRatio = dragAmount.y / totalHeight.toPx()
                                    splitRatio = (splitRatio + deltaRatio).coerceIn(0.2f, 0.8f)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Resize Split",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Bottom: Live Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        previewContent()
                    }
                }
            }
        }
    }
}
