package com.deepseek.balance.data.api

import retrofit2.http.GET
import retrofit2.http.Header

interface DeepSeekApi {

    /** 查询账户余额 */
    @GET("user/balance")
    suspend fun getBalance(
        @Header("Authorization") auth: String
    ): BalanceResponse
}
