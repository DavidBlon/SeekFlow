package com.deepseek.balance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deepseek.balance.ui.screens.DashboardScreen
import com.deepseek.balance.ui.screens.SettingsScreen
import com.deepseek.balance.ui.screens.SplashScreen
import com.deepseek.balance.ui.theme.DeepSeekBalanceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeepSeekBalanceTheme {
                var showSplash by remember { mutableStateOf(true) }

                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White
                    ) {
                        DeepSeekNavHost()
                    }

                    if (showSplash) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeepSeekNavHost() {
    val navController = rememberNavController()

    val animSpec = tween<IntOffset>(300)

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable(
            route = "dashboard",
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = animSpec
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = animSpec
                )
            }
        ) {
            DashboardScreen(
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable(
            route = "settings",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = animSpec
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = animSpec
                )
            }
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
