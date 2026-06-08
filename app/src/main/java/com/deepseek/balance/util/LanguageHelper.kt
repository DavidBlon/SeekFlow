package com.deepseek.balance.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

data class LanguageOption(
    val displayName: String,
    val localeCode: String
)

val languageOptions = listOf(
    LanguageOption("简体中文", "zh"),
    LanguageOption("繁體中文", "zh-rTW"),
    LanguageOption("English", "en"),
    LanguageOption("日本語", "ja"),
    LanguageOption("한국어", "ko"),
    LanguageOption("Español", "es"),
    LanguageOption("Français", "fr"),
    LanguageOption("Deutsch", "de"),
    LanguageOption("Русский", "ru"),
    LanguageOption("Italiano", "it"),
    LanguageOption("Türkçe", "tr"),
    LanguageOption("Tiếng Việt", "vi"),
)

fun findLanguageByCode(code: String): LanguageOption {
    return languageOptions.find { it.localeCode == code } ?: languageOptions[0]
}

fun applyLocale(context: Context, localeCode: String): Context {
    val locale = parseLocale(localeCode)
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    return context.createConfigurationContext(config)
}

private fun parseLocale(code: String): Locale {
    return when (code) {
        "zh-rTW" -> Locale("zh", "TW")
        else -> Locale(code)
    }
}
