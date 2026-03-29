package com.christelldev.easyreferplus.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SwipeToConfirmButton(
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward,
    backgroundColor: Color = Color(0xFF1565C0),
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val view = LocalView.current
    val density = LocalDensity.current

    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    val thumbSizeDp = 52.dp
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }
    val maxDragPx = (containerWidthPx - thumbSizePx).coerceAtLeast(0f)
    val threshold = 0.85f

    var offsetX by remember { mutableFloatStateOf(0f) }
    var confirmed by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (confirmed) maxDragPx else offsetX,
        animationSpec = tween(if (confirmed) 200 else 150),
        label = "swipeOffset"
    )

    val progress = if (maxDragPx > 0) (animatedOffset / maxDragPx).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundColor.copy(alpha = if (enabled) 0.15f else 0.08f))
            .onSizeChanged { containerWidthPx = it.width.toFloat() },
        contentAlignment = Alignment.CenterStart
    ) {
        // Label text (fades as you swipe)
        Text(
            text = text,
            color = backgroundColor.copy(alpha = (1f - progress * 2f).coerceIn(0f, 1f)),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.Center)
        )

        // Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .padding(4.dp)
                .size(thumbSizeDp - 8.dp)
                .clip(CircleShape)
                .background(if (confirmed) Color(0xFF2E7D32) else backgroundColor)
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = enabled && !confirmed && !isLoading,
                    state = rememberDraggableState { delta ->
                        offsetX = (offsetX + delta).coerceIn(0f, maxDragPx)
                    },
                    onDragStopped = {
                        if (offsetX / maxDragPx >= threshold) {
                            confirmed = true
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            } else {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            }
                            onConfirm()
                        } else {
                            offsetX = 0f
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    if (confirmed) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    // Reset when confirmed action completes (caller changes isLoading back to false)
    LaunchedEffect(isLoading) {
        if (!isLoading && confirmed) {
            confirmed = false
            offsetX = 0f
        }
    }
}
