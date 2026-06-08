package com.deepseek.balance.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.deepseek.balance.MainActivity
import com.deepseek.balance.R
import com.deepseek.balance.data.worker.WidgetRefreshWorker
import java.util.concurrent.TimeUnit

/**
 * 小组件基类 — 三个尺寸共用刷新调度和更新逻辑
 */
open class BalanceWidgetProvider : AppWidgetProvider() {

    /**
     * 子类重写返回对应的布局 ID
     */
    protected open fun getLayoutId(): Int = R.layout.widget_balance

    /**
     * 子类重写返回是否显示标题标签（当日消耗 / 本月消耗）
     */
    protected open fun hasLabels(): Boolean = false

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        schedulePeriodicRefresh(context)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, getLayoutId(), hasLabels())
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        schedulePeriodicRefresh(context)
    }

    private fun schedulePeriodicRefresh(context: Context) {
        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "widget_balance_refresh", ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            layoutId: Int,
            labels: Boolean
        ) {
            val cache = WidgetDataCache(context)
            val data = cache.getBalanceData()
            val prefs = context.getSharedPreferences("whale_prefs", Context.MODE_PRIVATE)
            val isBlue = prefs.getBoolean("is_whale_blue", false)

            val views = RemoteViews(context.packageName, layoutId)

            // 有 logo 的布局才设置颜色
            if (layoutId != R.layout.widget_balance) {
                views.setImageViewResource(
                    R.id.widget_logo,
                    if (isBlue) R.drawable.ic_deepseek_logo_blue else R.drawable.ic_deepseek_logo
                )
            }

            // 点击打开应用
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            views.setTextViewText(R.id.widget_balance_amount, "¥${data.totalBalance}")
            views.setTextViewText(R.id.widget_daily_amount,
                if (labels) "¥${data.dailyCost}" else "日 ¥${data.dailyCost}")
            views.setTextViewText(R.id.widget_monthly_amount,
                if (labels) "¥${data.monthlyCost}" else "月 ¥${data.monthlyCost}")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

/** 2×1 紧凑 — 无标题，仅余额 + 日/月消耗 */
class BalanceWidgetSmall : BalanceWidgetProvider() {
    override fun getLayoutId() = R.layout.widget_balance
    override fun hasLabels() = false
}

/** 2×1 经典 — 带标题和鲸鱼 logo */
class BalanceWidgetMedium : BalanceWidgetProvider() {
    override fun getLayoutId() = R.layout.widget_balance_medium
    override fun hasLabels() = true
}

/** 2×2 宽版 — 带鲸鱼 logo，大字 */
class BalanceWidgetLarge : BalanceWidgetProvider() {
    override fun getLayoutId() = R.layout.widget_balance_large
    override fun hasLabels() = false
}
