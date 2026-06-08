package com.deepseek.balance.ui.screens

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.deepseek.balance.R
import com.deepseek.balance.data.db.DailyUsageSummary
import com.deepseek.balance.data.repository.UsageRepository
import com.deepseek.balance.data.worker.RefreshWorker
import com.deepseek.balance.ui.widget.BalanceWidgetLarge
import com.deepseek.balance.ui.widget.BalanceWidgetMedium
import com.deepseek.balance.ui.widget.BalanceWidgetProvider
import com.deepseek.balance.ui.widget.BalanceWidgetSmall
import com.deepseek.balance.ui.widget.WidgetDataCache
import com.deepseek.balance.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class DashboardState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isWhaleBlue: Boolean = false,
    val errorMessage: String? = null,
    val totalBalance: String = "0.00",
    val grantedBalance: String = "0.00",
    val toppedUpBalance: String = "0.00",
    val dailyCost: String = "0.00",
    val monthlyCost: String = "0.00",
    val flashTokens: Long = 0,
    val proTokens: Long = 0,
    val dailyData: List<DailyUsageSummary> = emptyList(),
    val hasApiKey: Boolean = false,
    val hasUserToken: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val application: Application,
    private val repository: UsageRepository,
    private val widgetDataCache: WidgetDataCache
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val prefs = application.getSharedPreferences("whale_prefs", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    init {
        // 恢复上次的鲸鱼颜色
        val saved = prefs.getBoolean("is_whale_blue", false)
        if (saved) _state.update { it.copy(isWhaleBlue = true) }

        viewModelScope.launch {
            combine(repository.apiKey, repository.userToken) { key, token -> key to token }
                .collect { (key, token) ->
                    _state.update {
                        it.copy(hasApiKey = key.isNotBlank(), hasUserToken = token.isNotBlank())
                    }
                    if (key.isNotBlank()) {
                        refresh()
                        schedulePeriodicRefresh()
                    } else {
                        _state.update { it.copy(isLoading = false, isRefreshing = false) }
                    }
                }
        }
    }

    private fun schedulePeriodicRefresh() {
        runCatching {
            val request = PeriodicWorkRequestBuilder<RefreshWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(application).enqueueUniquePeriodicWork(
                "balance_refresh",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    fun toggleWhaleColor() {
        val newValue = !_state.value.isWhaleBlue
        _state.update { it.copy(isWhaleBlue = newValue) }
        prefs.edit().putBoolean("is_whale_blue", newValue).apply()
        refreshAllWidgets()
    }

    private fun refreshAllWidgets() {
        runCatching {
            val manager = AppWidgetManager.getInstance(application)
            val widgetConfigs = listOf(
                BalanceWidgetSmall::class.java to R.layout.widget_balance,
                BalanceWidgetMedium::class.java to R.layout.widget_balance_medium,
                BalanceWidgetLarge::class.java to R.layout.widget_balance_large
            )
            for ((cls, layoutId) in widgetConfigs) {
                val ids = manager.getAppWidgetIds(ComponentName(application, cls))
                for (id in ids) {
                    val labels = layoutId == R.layout.widget_balance_medium
                    BalanceWidgetProvider.updateAppWidget(application, manager, id, layoutId, labels)
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, errorMessage = null) }

            runCatching {
                repository.refreshAndRecord()
                    .onSuccess { response ->
                        val info = response.balanceInfos.firstOrNull()
                        _state.update {
                            it.copy(
                                totalBalance = info?.totalBalance ?: "0.00",
                                grantedBalance = info?.grantedBalance ?: "0.00",
                                toppedUpBalance = info?.toppedUpBalance ?: "0.00",
                                errorMessage = null
                            )
                        }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(errorMessage = e.message ?: "\u7f51\u7edc\u9519\u8bef") }
                    }

                val today = dateFormat.format(System.currentTimeMillis())
                val month = monthFormat.format(System.currentTimeMillis())
                val dailyCost = repository.getDailyCost(today)
                val monthlyCost = repository.getMonthlyCost(month)
                val flashTokens = repository.getMonthlyModelTokens(UsageRepository.MODEL_FLASH, month)
                val proTokens = repository.getMonthlyModelTokens(UsageRepository.MODEL_PRO, month)

                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -30)
                val fromDate = dateFormat.format(cal.time)
                val data = repository.getDailyUsageSince(fromDate).first()

                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        dailyCost = String.format("%.4f", dailyCost),
                        monthlyCost = String.format("%.4f", monthlyCost),
                        flashTokens = flashTokens,
                        proTokens = proTokens,
                        dailyData = data
                    )
                }

                // Update widget cache and refresh widgets
                val currentState = _state.value
                widgetDataCache.saveBalanceData(
                    totalBalance = currentState.totalBalance,
                    dailyCost = currentState.dailyCost,
                    monthlyCost = currentState.monthlyCost
                )

                // 余额预警检测
                val thresholdStr = prefs.getString("balance_threshold", "") ?: ""
                if (thresholdStr.isNotBlank()) {
                    val threshold = thresholdStr.toFloatOrNull()
                    val balance = currentState.totalBalance.toFloatOrNull()
                    if (threshold != null && balance != null && balance < threshold) {
                        NotificationHelper.showBalanceAlert(
                            application, currentState.totalBalance, thresholdStr
                        )
                    }
                }
                refreshAllWidgets()
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "\u5237\u65b0\u5931\u8d25"
                    )
                }
            }
        }
    }
}
