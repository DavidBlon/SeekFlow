package com.deepseek.balance.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.deepseek.balance.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val isWhaleBlue = remember {
        context.getSharedPreferences("whale_prefs", Context.MODE_PRIVATE)
            .getBoolean("is_whale_blue", false)
    }

    LaunchedEffect(Unit) {
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(
                    id = if (isWhaleBlue) R.drawable.ic_deepseek_logo_blue
                    else R.drawable.ic_deepseek_logo
                ),
                contentDescription = "DeepSeek Logo",
                modifier = Modifier.size(160.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF4D6BFE))) { append("Seek") }
                    withStyle(SpanStyle(color = Color(0xFF000000))) { append("Flow") }
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
