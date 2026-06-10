package com.deepseek.balance.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.deepseek.balance.data.api.BalanceInfo
import com.deepseek.balance.data.api.BalanceResponse
import com.deepseek.balance.data.api.DeepSeekApi
import com.deepseek.balance.data.api.PlatformApi
import com.deepseek.balance.data.api.UserSummary
import com.deepseek.balance.data.db.DailyUsageSummary
import com.deepseek.balance.data.db.ModelCostSummary
import com.deepseek.balance.data.db.UsageDao
import com.deepseek.balance.data.db.UsageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepository @Inject constructor(
    private val api: DeepSeekApi,
    private val platformApi: PlatformApi,
    private val usageDao: UsageDao,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_API_KEY = stringPreferencesKey("api_key")
        val KEY_USER_TOKEN = stringPreferencesKey("user_token")
        val KEY_LAST_BALANCE = doublePreferencesKey("last_balance")
        val KEY_LAST_TOTAL_BALANCE = doublePreferencesKey("last_total_balance")

        const val MODEL_FLASH = "deepseek-v4-flash"
        const val MODEL_PRO = "deepseek-v4-pro"
        const val MODEL_CHAT_REASONER = "deepseek-chat & deepseek-reasoner"
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    val apiKey: Flow<String> = dataStore.data.map { it[KEY_API_KEY] ?: "" }

    suspend fun saveApiKey(key: String) {
        dataStore.edit { it[KEY_API_KEY] = key }
    }

    val userToken: Flow<String> = dataStore.data.map { it[KEY_USER_TOKEN] ?: "" }

    suspend fun saveUserToken(token: String) {
        dataStore.edit { it[KEY_USER_TOKEN] = token }
    }

    suspend fun fetchBalance(): Result<BalanceResponse> {
        return try {
            val key = dataStore.data.first()[KEY_API_KEY] ?: ""
            if (key.isBlank()) {
                return Result.failure(Exception("Please set API Key first"))
            }

            val response = api.getBalance("Bearer $key")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserSummary(): Result<UserSummary> {
        return try {
            val token = dataStore.data.first()[KEY_USER_TOKEN] ?: ""
            if (token.isBlank()) {
                return Result.failure(Exception("Please set User Token first"))
            }

            val response = platformApi.getUserSummary("Bearer $token")
            if (response.code != 0 || response.data?.bizData == null) {
                return Result.failure(
                    Exception(response.msg.ifBlank { "Failed to fetch user summary" })
                )
            }

            Result.success(response.data.bizData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchMonthlyUsage(year: Int, month: Int): Result<Unit> {
        return try {
            val token = dataStore.data.first()[KEY_USER_TOKEN] ?: ""
            if (token.isBlank()) {
                return Result.failure(Exception("Please set User Token first"))
            }

            val auth = "Bearer $token"
            val amountResp = platformApi.getUsageAmount(auth, month, year)
            val costResp = platformApi.getUsageCost(auth, month, year)

            if (amountResp.code != 0) {
                return Result.failure(Exception("Failed to fetch usage amount: ${amountResp.msg}"))
            }
            if (costResp.code != 0) {
                return Result.failure(Exception("Failed to fetch usage cost: ${costResp.msg}"))
            }

            val amountData = amountResp.data?.bizData
            val costDataList = costResp.data?.bizData
            val costMap = mutableMapOf<String, Double>()

            costDataList?.forEach { currencyData ->
                currencyData.days.forEach { day ->
                    day.data.forEach { modelUsage ->
                        val totalCost = modelUsage.usage.sumOf {
                            it.amount.toDoubleOrNull() ?: 0.0
                        }
                        costMap["${day.date}|${modelUsage.model}"] = totalCost
                    }
                }
            }

            val monthStr = String.format("%04d-%02d", year, month)
            amountData?.days?.forEach { day ->
                day.data.forEach { modelUsage ->
                    val totalTokens = modelUsage.usage.sumOf {
                        it.amount.toLongOrNull() ?: 0L
                    }
                    val cost = costMap["${day.date}|${modelUsage.model}"] ?: 0.0

                    usageDao.deleteByDateAndModel(day.date, modelUsage.model)
                    usageDao.insert(
                        UsageEntity(
                            timestamp = System.currentTimeMillis(),
                            date = day.date,
                            month = monthStr,
                            model = modelUsage.model,
                            inputTokens = 0,
                            outputTokens = 0,
                            totalTokens = totalTokens,
                            costAmount = cost
                        )
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshAndRecord(): Result<BalanceResponse> {
        val token = dataStore.data.first()[KEY_USER_TOKEN] ?: ""

        if (token.isNotBlank()) {
            return try {
                val summaryResp = platformApi.getUserSummary("Bearer $token")
                if (summaryResp.code == 0 && summaryResp.data?.bizData != null) {
                    val summary = summaryResp.data.bizData
                    val normalBalance = summary.normalWallets.firstOrNull()?.balance ?: "0"
                    val bonusBalance = summary.bonusWallets.firstOrNull()?.balance ?: "0"

                    val cal = Calendar.getInstance()
                    fetchMonthlyUsage(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)

                    Result.success(
                        BalanceResponse(
                            isAvailable = true,
                            balanceInfos = listOf(
                                BalanceInfo(
                                    currency = summary.normalWallets.firstOrNull()?.currency ?: "CNY",
                                    totalBalance = String.format(
                                        "%.2f",
                                        normalBalance.toDoubleOrNull() ?: 0.0
                                    ),
                                    grantedBalance = String.format(
                                        "%.2f",
                                        bonusBalance.toDoubleOrNull() ?: 0.0
                                    ),
                                    toppedUpBalance = String.format(
                                        "%.2f",
                                        normalBalance.toDoubleOrNull() ?: 0.0
                                    )
                                )
                            )
                        )
                    )
                } else {
                    fetchBalance()
                }
            } catch (_: Exception) {
                fetchBalance()
            }
        }

        val result = fetchBalance()
        result.onSuccess { response ->
            val info = response.balanceInfos.firstOrNull()
            val currentTotal = info?.totalBalance?.toDoubleOrNull() ?: return@onSuccess
            val lastTotal = dataStore.data.first()[KEY_LAST_TOTAL_BALANCE] ?: currentTotal
            val delta = lastTotal - currentTotal

            if (delta > 0.001) {
                val today = dateFormat.format(Date())
                val month = monthFormat.format(Date())
                usageDao.deleteByDateAndModel(today, "balance-delta")
                usageDao.insert(
                    UsageEntity(
                        timestamp = System.currentTimeMillis(),
                        date = today,
                        month = month,
                        model = "balance-delta",
                        totalTokens = 0,
                        costAmount = delta
                    )
                )
            }

            dataStore.edit { it[KEY_LAST_TOTAL_BALANCE] = currentTotal }
        }
        return result
    }

    suspend fun getDailyCost(date: String = dateFormat.format(Date())): Double {
        return usageDao.getDailyCost(date)
    }

    suspend fun getMonthlyCost(month: String = monthFormat.format(Date())): Double {
        return usageDao.getMonthlyCost(month)
    }

    suspend fun getDailyTotalTokens(date: String = dateFormat.format(Date())): Long {
        return usageDao.getDailyTotalTokens(date)
    }

    suspend fun getMonthlyTotalTokens(month: String = monthFormat.format(Date())): Long {
        return usageDao.getMonthlyTotalTokens(month)
    }

    suspend fun getDailyModelTokens(model: String, date: String = dateFormat.format(Date())): Long {
        return usageDao.getDailyModelTokens(date, model)
    }

    suspend fun getMonthlyModelTokens(model: String, month: String = monthFormat.format(Date())): Long {
        return usageDao.getMonthlyModelTokens(month, model)
    }

    fun getDailyUsageSince(fromDate: String): Flow<List<DailyUsageSummary>> {
        return usageDao.getDailyUsageSince(fromDate)
    }

    suspend fun addManualRecord(
        model: String,
        inputTokens: Long,
        outputTokens: Long,
        costAmount: Double
    ) {
        val now = Date()
        usageDao.insert(
            UsageEntity(
                timestamp = now.time,
                date = dateFormat.format(now),
                month = monthFormat.format(now),
                model = model,
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                totalTokens = inputTokens + outputTokens,
                costAmount = costAmount
            )
        )
    }

    // ===== 鍒嗘瀽鐩稿叧 =====

    /** 鑾峰彇鏈€杩慛澶╃殑姣忔棩娑堣€楀垪琛紙涓嶉€氳繃Flow锛岀洿鎺ヨ繑鍥烇級 */
    suspend fun getDailyCostList(days: Int = 30): List<DailyUsageSummary> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        val fromDate = dateFormat.format(cal.time)
        return usageDao.getDailyCostListSince(fromDate)
    }

    /** 鑾峰彇鎸夋ā鍨嬫眹鎬荤殑娑堣垂 */
    suspend fun getModelCosts(days: Int = 30): List<ModelCostSummary> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        val fromDate = dateFormat.format(cal.time)
        return usageDao.getModelCostSince(fromDate)
    }

    /** 鑾峰彇鏃ュ潎娑堣€?*/
    suspend fun getAvgDailyCost(days: Int = 7): Double {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        val fromDate = dateFormat.format(cal.time)
        return usageDao.getAvgDailyCostSince(fromDate)
    }

}
