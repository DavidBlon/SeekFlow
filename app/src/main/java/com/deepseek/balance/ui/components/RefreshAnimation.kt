package com.deepseek.balance.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.deepseek.balance.R

@Composable
fun RefreshAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    isAnimating: Boolean = true
) {
    val rotation = if (isAnimating) {
        val infiniteTransition = rememberInfiniteTransition(label = "refresh")
        val r by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
        r
    } else {
        0f
    }

    Image(
        painter = painterResource(R.drawable.ic_seekflow_signal),
        contentDescription = "Loading",
        modifier = modifier
            .size(size)
            .rotate(rotation)
    )
}

