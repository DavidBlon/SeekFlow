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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.deepseek.balance.R
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
                    stringResource(R.string.forecast_title),
                    color = Color(0xFF1A1A1A),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))

            if (avgDailyCost <= 0.0001) {
                Text(
                    stringResource(R.string.forecast_no_data),
                    color = Color(0xFF999999),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                val daysRemaining = (balance / avgDailyCost).toInt()
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, daysRemaining)
                val dateFormat = SimpleDateFormat(stringResource(R.string.forecast_date_format), Locale.getDefault())
                val estimatedDate = dateFormat.format(cal.time)

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        stringResource(R.string.forecast_days_prefix),
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
                        stringResource(R.string.forecast_days_suffix),
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.forecast_estimated_date, estimatedDate),
                    color = Color(0xFF999999),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.forecast_daily_avg, String.format("%.2f", avgDailyCost)),
                    color = Color(0xFF666666),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
