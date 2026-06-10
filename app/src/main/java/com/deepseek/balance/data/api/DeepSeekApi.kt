package com.deepseek.balance.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface DeepSeekApi {

    /** 查询账户余额 */
    @GET("user/balance")
    suspend fun getBalance(
        @Header("Authorization") auth: String
    ): BalanceResponse

    /** 聊天补全 */
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") auth: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}
