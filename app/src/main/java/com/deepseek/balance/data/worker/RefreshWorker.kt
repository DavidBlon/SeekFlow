package com.deepseek.balance.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.deepseek.balance.data.repository.UsageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: UsageRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val apiResult = repository.refreshAndRecord()
            if (apiResult.isSuccess) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
