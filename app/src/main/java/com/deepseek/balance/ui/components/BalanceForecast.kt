package com.deepseek.balance.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun BalanceForecast(
    balance: Double,
    avgDailyCost: Double,
    modifier: Modifier = Modifier
) {
    GlassPanel(modifier = modifier.fillMaxWidth(), radius = 22) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFF4D6BFE),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "余额预估",
                    color = Color(0xFF1A1A1A),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))

            if (avgDailyCost <= 0.0001) {
                Text(
                    "暂无消耗数据，无法预估",
                    color = Color(0xFF999999),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                val daysRemaining = (balance / avgDailyCost).toInt()
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, daysRemaining)
                val dateFormat = SimpleDateFormat("M月d日", Locale.CHINA)
                val estimatedDate = dateFormat.format(cal.time)

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "还能用 ",
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "$daysRemaining",
                        color = Color(0xFF4D6BFE),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 30.sp
                    )
                    Text(
                        " 天",
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "按当前消耗速度，余额预计 $estimatedDate 用完",
                    color = Color(0xFF999999),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "日均消耗 ¥${String.format("%.2f", avgDailyCost)}",
                    color = Color(0xFF666666),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
