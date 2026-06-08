package com.deepseek.balance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.deepseek.balance.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BalanceCard(
    totalBalance: String,
    grantedBalance: String,
    toppedUpBalance: String,
    dailyCost: String,
    monthlyCost: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    GlassPanel(modifier = modifier.fillMaxWidth(), radius = 26) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color(0xFF333333),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    stringResource(R.string.balance_title),
                    modifier = Modifier.padding(start = 12.dp).weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE8ECF0))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF51F0AE))
                    )
                    Text(
                        stringResource(R.string.balance_available),
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color(0xFF6EF0B6),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF83E8FF), modifier = Modifier.size(34.dp))
            } else {
                Text(
                    "\u00a5$totalBalance",
                    fontSize = 46.sp,
                    lineHeight = 50.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF000000)
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BalanceUsageItem(
                        title = stringResource(R.string.balance_daily),
                        amount = "\u00a5$dailyCost",
                        modifier = Modifier.weight(1f)
                    )
                    BalanceUsageItem(
                        title = stringResource(R.string.balance_monthly),
                        amount = "\u00a5$monthlyCost",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceUsageItem(
    title: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF0F2F5))
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            title,
            color = Color(0xFF666666),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(5.dp))
        Text(
            amount,
            color = Color(0xFF000000),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    radius: Int = 24,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius.dp))
            .background(Color(0xFFF5F7FA))
    ) {
        content()
    }
}
