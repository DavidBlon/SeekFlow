package com.deepseek.balance.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.deepseek.balance.R

object NotificationHelper {

    private const val CHANNEL_ID = "balance_alert"
    private const val CHANNEL_NAME = "余额预警"
    private const val CHANNEL_DESC = "当账户余额低于设定阈值时发出提醒"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply { description = CHANNEL_DESC }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun showBalanceAlert(context: Context, currentBalance: String, threshold: String) {
        val nm = NotificationManagerCompat.from(context)
        // 检查是否有通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !nm.areNotificationsEnabled()
        ) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("余额不足提醒")
            .setContentText("当前余额 ¥$currentBalance，低于阈值 ¥$threshold")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("当前余额 ¥$currentBalance\n设置阈值 ¥$threshold\n请及时充值以避免服务中断。"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }
}
