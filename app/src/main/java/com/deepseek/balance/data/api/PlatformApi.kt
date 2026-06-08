package com.deepseek.balance.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/** platform.deepseek.com 内部 API */
interface PlatformApi {

    /** 用户概览：余额 + 本月消费 + 本月token */
    @GET("api/v0/users/get_user_summary")
    suspend fun getUserSummary(
        @Header("Authorization") auth: String
    ): PlatformResponse<UserSummary>

    /** 每月用量（token）：按天+按模型 */
    @GET("api/v0/usage/amount")
    suspend fun getUsageAmount(
        @Header("Authorization") auth: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): PlatformResponse<UsageAmountData>

    /** 每月费用：按天+按模型 */
    @GET("api/v0/usage/cost")
    suspend fun getUsageCost(
        @Header("Authorization") auth: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): PlatformResponse<List<UsageCostData>>
}
