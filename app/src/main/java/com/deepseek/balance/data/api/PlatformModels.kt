package com.deepseek.balance.data.api

import com.google.gson.annotations.SerializedName

// ===== 通用响应包装 =====

/** 平台 API 统一响应外层 */
data class PlatformResponse<T>(
    val code: Int,
    val msg: String,
    val data: PlatformData<T>?
)

data class PlatformData<T>(
    @SerializedName("biz_code") val bizCode: Int,
    @SerializedName("biz_msg") val bizMsg: String,
    @SerializedName("biz_data") val bizData: T?
)

// ===== 用户概览 (get_user_summary) =====

data class UserSummary(
    @SerializedName("current_token") val currentToken: Long,
    @SerializedName("monthly_usage") val monthlyUsage: String,
    @SerializedName("total_usage") val totalUsage: Long,
    @SerializedName("normal_wallets") val normalWallets: List<WalletInfo>,
    @SerializedName("bonus_wallets") val bonusWallets: List<WalletInfo>,
    @SerializedName("total_available_token_estimation") val totalAvailableTokenEstimation: String,
    @SerializedName("monthly_costs") val monthlyCosts: List<MonthlyCost>,
    @SerializedName("monthly_token_usage") val monthlyTokenUsage: String
)

data class WalletInfo(
    val currency: String,
    val balance: String,
    @SerializedName("token_estimation") val tokenEstimation: String
)

data class MonthlyCost(
    val currency: String,
    val amount: String
)

// ===== 用量统计 (usage/amount) =====

data class UsageAmountData(
    val total: List<ModelUsage>,
    val days: List<DailyUsage>
)

data class ModelUsage(
    val model: String,
    val usage: List<UsageItem>
)

data class UsageItem(
    val type: String,
    val amount: String
)

data class DailyUsage(
    val date: String,
    val data: List<ModelUsage>
)

// ===== 费用统计 (usage/cost) =====

data class UsageCostData(
    val total: List<ModelUsage>,
    val days: List<DailyUsage>,
    val currency: String
)

// ===== 聚合后的展示数据 =====

/** 每日汇总（token + 费用） */
data class DailySummary(
    val date: String,
    val totalTokens: Long,
    val totalCost: Double,
    val modelTokens: Map<String, Long>  // model -> tokens
)

/** 模型汇总 */
data class ModelSummary(
    val model: String,
    val totalTokens: Long,
    val totalCost: Double
)
