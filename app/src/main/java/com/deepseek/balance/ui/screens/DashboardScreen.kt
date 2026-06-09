package com.deepseek.balance.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

@Composable
private fun DashboardContent(
    state: DashboardState,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HeaderBar(onRefresh = onRefresh)

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

