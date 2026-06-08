package com.deepseek.balance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UsageCard(
    title: String,
    amount: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    GlassPanel(modifier = modifier, radius = 20) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Text(
                    title,
                    modifier = Modifier.padding(start = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(amount, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

@Composable
fun ModelTokenRow(
    modelName: String,
    tokens: Long,
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier
) {
    GlassPanel(modifier = modifier.fillMaxWidth(), radius = 22) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.46f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        modelName,
                        color = Color(0xFF1A1A1A),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(formatTokens(tokens), color = Color(0xFF333333), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${tokens} Tokens",
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(96.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = accent.copy(alpha = 0.96f),
                        trackColor = Color.Black.copy(alpha = 0.10f)
                    )
                }
            }
        }
    }
}

private fun formatTokens(tokens: Long): String {
    return when {
        tokens >= 1_000_000 -> String.format("%.1fM", tokens / 1_000_000.0)
        tokens >= 1_000 -> String.format("%.1fK", tokens / 1_000.0)
        else -> tokens.toString()
    }
}
