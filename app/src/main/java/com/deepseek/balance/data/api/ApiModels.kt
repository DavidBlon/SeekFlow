package com.deepseek.balance.data.api

import com.google.gson.annotations.SerializedName

/** 余额查询响应 */
data class BalanceResponse(
    @SerializedName("is_available") val isAvailable: Boolean,
    @SerializedName("balance_infos") val balanceInfos: List<BalanceInfo>
)

data class BalanceInfo(
    @SerializedName("currency") val currency: String,
    @SerializedName("total_balance") val totalBalance: String,
    @SerializedName("granted_balance") val grantedBalance: String,
    @SerializedName("topped_up_balance") val toppedUpBalance: String
)

