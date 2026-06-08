package com.deepseek.balance

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.deepseek.balance.util.applyLocale
import com.deepseek.balance.util.findLanguageByCode
import com.deepseek.balance.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DeepSeekApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun attachBaseContext(base: Context?) {
        val code = base?.getSharedPreferences("whale_prefs", Context.MODE_PRIVATE)
            ?.getString("app_language", "zh") ?: "zh"
        super.attachBaseContext(applyLocale(base ?: return, code))
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}
