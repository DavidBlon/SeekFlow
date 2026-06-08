package com.deepseek.balance.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {

    @Insert
    suspend fun insert(record: UsageEntity)

    /** 删除指定日期+模型的记录（用于刷新时替换） */
    @Query("DELETE FROM usage_records WHERE date = :date AND model = :model")
    suspend fun deleteByDateAndModel(date: String, model: String)

    /** 查询指定日期的总消耗 */
    @Query("SELECT COALESCE(SUM(costAmount), 0.0) FROM usage_records WHERE date = :date")
    suspend fun getDailyCost(date: String): Double

    /** 查询指定月份的总消耗 */
    @Query("SELECT COALESCE(SUM(costAmount), 0.0) FROM usage_records WHERE month = :month")
    suspend fun getMonthlyCost(month: String): Double

    /** 查询指定日期指定模型的token总量 */
    @Query("SELECT COALESCE(SUM(totalTokens), 0) FROM usage_records WHERE date = :date AND model = :model")
    suspend fun getDailyModelTokens(date: String, model: String): Long

    /** 查询指定月份指定模型的token总量 */
    @Query("SELECT COALESCE(SUM(totalTokens), 0) FROM usage_records WHERE month = :month AND model = :model")
    suspend fun getMonthlyModelTokens(month: String, model: String): Long

    /** 查询最近N天的每日消耗（用于柱状图） */
    @Query("""
        SELECT date, SUM(totalTokens) as totalTokens, SUM(costAmount) as costAmount
        FROM usage_records
        WHERE date >= :fromDate
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyUsageSince(fromDate: String): Flow<List<DailyUsageSummary>>

    /** 查询指定日期的总token */
    @Query("SELECT COALESCE(SUM(totalTokens), 0) FROM usage_records WHERE date = :date")
    suspend fun getDailyTotalTokens(date: String): Long

    /** 查询指定月份的总token */
    @Query("SELECT COALESCE(SUM(totalTokens), 0) FROM usage_records WHERE month = :month")
    suspend fun getMonthlyTotalTokens(month: String): Long

    /** 查询所有记录 */
    @Query("SELECT * FROM usage_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRecords(limit: Int = 100): Flow<List<UsageEntity>>

    /** 按模型汇总消费（用于饼图） */
    @Query("""
        SELECT model, SUM(costAmount) as costAmount, SUM(totalTokens) as totalTokens
        FROM usage_records
        WHERE date >= :fromDate AND model != 'balance-delta'
        GROUP BY model
    """)
    suspend fun getModelCostSince(fromDate: String): List<ModelCostSummary>

    /** 计算日均消耗 */
    @Query("""
        SELECT COALESCE(SUM(costAmount), 0.0) / MAX(1, COUNT(DISTINCT date))
        FROM usage_records
        WHERE date >= :fromDate AND model != 'balance-delta'
    """)
    suspend fun getAvgDailyCostSince(fromDate: String): Double

    /** 查询每天的消耗（用于趋势图，包含balance-delta） */
    @Query("""
        SELECT date, SUM(totalTokens) as totalTokens, SUM(costAmount) as costAmount
        FROM usage_records
        WHERE date >= :fromDate
        GROUP BY date
        ORDER BY date ASC
    """)
    suspend fun getDailyCostListSince(fromDate: String): List<DailyUsageSummary>
}

/** 每日汇总 */
data class DailyUsageSummary(
    val date: String,
    val totalTokens: Long,
    val costAmount: Double
)

/** 模型消费汇总 */
data class ModelCostSummary(
    val model: String,
    val costAmount: Double,
    val totalTokens: Long
)
