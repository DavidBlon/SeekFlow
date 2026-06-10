package com.deepseek.balance.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepseek.balance.R
import kotlinx.coroutines.launch

data class TabItem(
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainTabScreen() {
    val tabs = listOf(
        TabItem(stringResource(R.string.tab_overview), Icons.Default.Dashboard),
        TabItem(stringResource(R.string.tab_analytics), Icons.Default.BarChart),
        TabItem(stringResource(R.string.tab_chat), Icons.Default.Chat),
        TabItem(stringResource(R.string.tab_settings), Icons.Default.Settings)
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.scrollToPage(index) }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF4D6BFE),
                            selectedTextColor = Color(0xFF4D6BFE),
                            unselectedIconColor = Color(0xFF999999),
                            unselectedTextColor = Color(0xFF999999),
                            indicatorColor = Color(0xFF4D6BFE).copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            key(page) {
                when (page) {
                    0 -> DashboardScreen(
                        onNavigateToSettings = {
                            coroutineScope.launch { pagerState.scrollToPage(3) }
                        }
                    )
                    1 -> AnalyticsScreen()
                    2 -> ChatScreen()
                    3 -> SettingsScreen(
                        onBack = null,
                        onSaveSuccess = {
                            coroutineScope.launch { pagerState.scrollToPage(0) }
                        }
                    )
                }
            }
        }
    }
}
