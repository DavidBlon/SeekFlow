package com.deepseek.balance.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.deepseek.balance.R
import com.deepseek.balance.ui.components.BalanceCard
import com.deepseek.balance.ui.components.DailyBarChart
import com.deepseek.balance.ui.components.ModelTokenRow
import com.deepseek.balance.ui.components.RefreshAnimation

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
            EmptyDashboard(onNavigateToSettings = onNavigateToSettings)
        } else {
            DashboardContent(state = state, onRefresh = { viewModel.refresh() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    state: DashboardState,
    onRefresh: () -> Unit
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RefreshAnimation(size = 52.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.loading_refreshing),
                    color = Color(0xFF666666),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    } else {
        val pullRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            state = pullRefreshState,
            indicator = {
                val fraction = pullRefreshState.distanceFraction.coerceIn(0f, 1f)
                var showIndicator by remember { mutableStateOf(false) }
                var wasRefreshing by remember { mutableStateOf(false) }
                var allowPullShow by remember { mutableStateOf(true) }

                // 刷新刚完成 → 立即隐藏，禁止下拉显示（防止distanceFraction动画回弹导致重新出现）
                if (wasRefreshing && !state.isRefreshing) {
                    showIndicator = false
                    allowPullShow = false
                }

                // 刷新中 → 显示
                if (state.isRefreshing) {
                    showIndicator = true
                    allowPullShow = true
                }

                // 下拉中（手指在拉，且不是刷新完成后的回弹）→ 显示
                if (!state.isRefreshing && fraction > 0f && allowPullShow && !showIndicator) {
                    showIndicator = true
                }

                // 下拉完全收起 → 隐藏
                if (!state.isRefreshing && fraction == 0f) {
                    showIndicator = false
                    allowPullShow = true
                }

                wasRefreshing = state.isRefreshing

                if (showIndicator) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .offset(y = -(40.dp + 16.dp) * (1f - fraction))
                            .alpha(if (state.isRefreshing) 1f else fraction),
                        contentAlignment = Alignment.Center
                    ) {
                        RefreshAnimation(size = 40.dp, isAnimating = state.isRefreshing)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeaderBar(onRefresh = onRefresh)

                state.errorMessage?.let {
                    ErrorStrip(message = it)
                }

                BalanceCard(
                    totalBalance = state.totalBalance,
                    grantedBalance = state.grantedBalance,
                    toppedUpBalance = state.toppedUpBalance,
                    dailyCost = state.dailyCost,
                    monthlyCost = state.monthlyCost,
                    isLoading = false
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
}

@Composable
private fun HeaderBar(onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.ic_seekflow_signal),
                contentDescription = "SeekFlow logo",
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.width(3.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color(0xFF4D6BFE))) { append("Seek") }
                withStyle(SpanStyle(color = Color(0xFF000000))) { append("Flow") }
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.loading_refreshing), tint = Color(0xFF333333))
        }
    }
}

@Composable
private fun ErrorStrip(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFFF6B6B).copy(alpha = 0.18f), shape = RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(message, color = Color(0xFFFFD8D8), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyDashboard(onNavigateToSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_seekflow_signal),
            contentDescription = "SeekFlow logo",
            modifier = Modifier
                .size(86.dp)
                .background(Color(0xFFF0F0F5), shape = RoundedCornerShape(24.dp))
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
            stringResource(R.string.empty_dashboard_desc),
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
            Text(stringResource(R.string.go_to_settings), fontWeight = FontWeight.SemiBold)
        }
    }
}

