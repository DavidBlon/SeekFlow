package com.deepseek.balance.ui.widget

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetDataCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveBalanceData(
        totalBalance: String,
        dailyCost: String,
        monthlyCost: String
    ) {
        prefs.edit()
            .putString(KEY_BALANCE, totalBalance)
            .putString(KEY_DAILY_COST, dailyCost)
            .putString(KEY_MONTHLY_COST, monthlyCost)
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply()
    }

    fun getBalanceData(): WidgetBalanceData {
        return WidgetBalanceData(
            totalBalance = prefs.getString(KEY_BALANCE, "0.00") ?: "0.00",
            dailyCost = prefs.getString(KEY_DAILY_COST, "0.00") ?: "0.00",
            monthlyCost = prefs.getString(KEY_MONTHLY_COST, "0.00") ?: "0.00",
            lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0L)
        )
    }

    companion object {
        private const val PREFS_NAME = "widget_balance_cache"
        private const val KEY_BALANCE = "total_balance"
        private const val KEY_DAILY_COST = "daily_cost"
        private const val KEY_MONTHLY_COST = "monthly_cost"
        private const val KEY_LAST_UPDATE = "last_update"
    }
}

data class WidgetBalanceData(
    val totalBalance: String,
    val dailyCost: String,
    val monthlyCost: String,
    val lastUpdate: Long
)
