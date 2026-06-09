package com.deepseek.balance.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.deepseek.balance.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RefreshAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    accentColor: Color = Color(0xFF4D6BFE)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "refresh")

    // Continuous rotation of the signal icon (radar sweep)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Ring pulse - expands and fades
    val ringProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringPulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing radar ring
        Canvas(modifier = Modifier.size(size)) {
            val cx = size.toPx() / 2f
            val cy = size.toPx() / 2f
            val baseRadius = size.toPx() * 0.38f
            val pulseRadius = baseRadius + ringProgress * size.toPx() * 0.15f
            val ringAlpha = 0.3f - ringProgress * 0.2f

            // Outer pulse ring
            drawCircle(
                color = accentColor.copy(alpha = ringAlpha.coerceIn(0f, 1f)),
                radius = pulseRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Inner static ring (subtle)
            drawCircle(
                color = accentColor.copy(alpha = 0.08f),
                radius = baseRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Rotating signal icon
        Image(
            painter = painterResource(R.drawable.ic_seekflow_signal),
            contentDescription = "Loading",
            modifier = Modifier
                .size(size * 0.72f)
                .rotate(rotation)
        )
    }
}

