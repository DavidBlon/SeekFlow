package com.deepseek.balance.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.deepseek.balance.R
import com.deepseek.balance.ui.components.BalanceForecast
import com.deepseek.balance.ui.components.ModelPieChart
import com.deepseek.balance.ui.components.RefreshAnimation
import com.deepseek.balance.ui.components.TrendLineChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshIfNotLoaded()
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RefreshAnimation(size = 52.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.loading_analytics_data),
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
            onRefresh = { viewModel.refresh() },
            state = pullRefreshState,
            indicator = {
                val fraction = pullRefreshState.distanceFraction.coerceIn(0f, 1f)
                var showIndicator by remember { mutableStateOf(false) }
                var wasRefreshing by remember { mutableStateOf(false) }
                var allowPullShow by remember { mutableStateOf(true) }

                // 刷新刚完成 → 立即隐藏，禁止下拉显示
                if (wasRefreshing && !state.isRefreshing) {
                    showIndicator = false
                    allowPullShow = false
                }

                // 刷新中 → 显示
                if (state.isRefreshing) {
                    showIndicator = true
                    allowPullShow = true
                }

                // 下拉中 → 显示
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
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.title_analytics),
                        color = Color(0xFF1A1A1A),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.loading_refreshing), tint = Color(0xFF333333))
                    }
                }

                BalanceForecast(
                    balance = state.balance,
                    avgDailyCost = state.avgDailyCost
                )

                TrendLineChart(dailyData = state.trendData)

                ModelPieChart(modelCosts = state.modelCosts)
            }
        }
    }
}
