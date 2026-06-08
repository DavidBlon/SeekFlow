package com.deepseek.balance.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.deepseek.balance.R
import com.deepseek.balance.ui.components.BalanceCard
import com.deepseek.balance.ui.components.DailyBarChart
import com.deepseek.balance.ui.components.ModelTokenRow

@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (!state.hasApiKey) {
            EmptyDashboard(
                isWhaleBlue = state.isWhaleBlue,
                onNavigateToSettings = onNavigateToSettings
            )
        } else {
            DashboardContent(
                state = state,
                onRefresh = { viewModel.refresh() },
                onToggleWhaleColor = { viewModel.toggleWhaleColor() }
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardState,
    onRefresh: () -> Unit,
    onToggleWhaleColor: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HeaderBar(
            isWhaleBlue = state.isWhaleBlue,
            onRefresh = onRefresh,
            onToggleWhaleColor = onToggleWhaleColor
        )

        state.errorMessage?.let {
            ErrorStrip(message = it)
        }

        if (state.isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val infiniteTransition = rememberInfiniteTransition(label = "loading")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            tween(1000, easing = LinearEasing),
                            RepeatMode.Restart
                        ),
                        label = "loading_rotation"
                    )

                    Image(
                        painter = painterResource(
                            if (state.isWhaleBlue) R.drawable.ic_deepseek_logo_blue
                            else R.drawable.ic_deepseek_logo
                        ),
                        contentDescription = "刷新中",
                        modifier = Modifier
                            .size(52.dp)
                            .rotate(rotation)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "正在刷新数据",
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            BalanceCard(
                totalBalance = state.totalBalance,
                grantedBalance = state.grantedBalance,
                toppedUpBalance = state.toppedUpBalance,
                dailyCost = state.dailyCost,
                monthlyCost = state.monthlyCost,
                isLoading = state.isLoading
            )

            val maxTokens = maxOf(state.flashTokens, state.proTokens, 1L)
            ModelTokenRow(
                modelName = "V4 Flash",
                tokens = state.flashTokens,
                progress = state.flashTokens.toFloat() / maxTokens,
                accent = Color(0xFF19C9FF)
            )
            ModelTokenRow(
                modelName = "V4 Pro",
                tokens = state.proTokens,
                progress = state.proTokens.toFloat() / maxTokens,
                accent = Color(0xFFB84DFF)
            )

            DailyBarChart(dailyData = state.dailyData)
        }
    }
}

@Composable
private fun HeaderBar(
    isWhaleBlue: Boolean,
    onRefresh: () -> Unit,
    onToggleWhaleColor: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(
                    if (isWhaleBlue) R.drawable.ic_deepseek_logo_blue
                    else R.drawable.ic_deepseek_logo
                ),
                contentDescription = "DeepSeek logo",
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color(0xFF4D6BFE))) { append("Seek") }
                withStyle(SpanStyle(color = Color(0xFF000000))) { append("Flow") }
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        // 鲸鱼变色滑块
        WhaleColorToggle(isWhaleBlue = isWhaleBlue, onToggle = onToggleWhaleColor)
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = Color(0xFF333333))
        }
    }
}

@Composable
private fun ErrorStrip(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFF6B6B).copy(alpha = 0.18f))
            .padding(14.dp)
    ) {
        Text(message, color = Color(0xFFFFD8D8), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyDashboard(
    isWhaleBlue: Boolean,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(
                if (isWhaleBlue) R.drawable.ic_deepseek_logo_blue
                else R.drawable.ic_deepseek_logo
            ),
            contentDescription = "DeepSeek logo",
            modifier = Modifier
                .size(86.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF0F0F5))
                .padding(10.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color(0xFF4D6BFE))) { append("Seek") }
                withStyle(SpanStyle(color = Color(0xFF000000))) { append("Flow") }
            },
            color = Color(0xFF1A1A1A),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "请先在设置中填写 DeepSeek API Key，之后即可查看余额与消耗趋势。",
            color = Color(0xFF888888),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onNavigateToSettings,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF74D9FF),
                contentColor = Color(0xFF06222C)
            )
        ) {
            Text("前往设置", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun WhaleColorToggle(isWhaleBlue: Boolean, onToggle: () -> Unit) {
    val circleSize = 20.dp
    val thumbOffset by animateDpAsState(
        targetValue = if (isWhaleBlue) 0.dp else circleSize,
        label = "whaleToggle"
    )

    Box(
        modifier = Modifier
            .width(circleSize * 2)
            .height(circleSize)
            .clip(RoundedCornerShape(circleSize / 2))
            .clickable { onToggle() }
    ) {
        // 整条背景纯色
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    if (isWhaleBlue) Color(0xFF4D6BFE)
                    else Color(0xFF424242)
                )
        )
        // 圆形滑块
        Box(
            modifier = Modifier
                .size(circleSize)
                .offset(x = thumbOffset)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
