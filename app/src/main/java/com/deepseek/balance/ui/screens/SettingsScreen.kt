package com.deepseek.balance.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.balance.R
import com.deepseek.balance.data.repository.UsageRepository
import com.deepseek.balance.util.LanguageOption
import com.deepseek.balance.util.findLanguageByCode
import com.deepseek.balance.util.languageOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UsageRepository
) : ViewModel() {

    var apiKey by mutableStateOf("")
        private set
    var userToken by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            apiKey = repository.apiKey.first()
            userToken = repository.userToken.first()
        }
    }

    fun updateApiKey(key: String) {
        apiKey = key
    }

    fun updateUserToken(token: String) {
        userToken = token
    }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val key = apiKey.trim()
            val token = userToken.trim()
            if (key.isNotBlank()) repository.saveApiKey(key)
            if (token.isNotBlank()) repository.saveUserToken(token)
            onSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: (() -> Unit)?,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val accent = Color(0xFF4D6BFE)

    var showKey by remember { mutableStateOf(false) }
    var showToken by remember { mutableStateOf(false) }
    var threshold by remember {
        mutableStateOf(
            context.getSharedPreferences("whale_prefs", Context.MODE_PRIVATE)
                .getString("balance_threshold", "") ?: ""
        )
    }
    var selectedLang by remember {
        mutableStateOf(
            findLanguageByCode(
                context.getSharedPreferences("whale_prefs", Context.MODE_PRIVATE)
                    .getString("app_language", "zh") ?: "zh"
            )
        )
    }
    var langExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.title_settings), tint = Color(0xFF333333))
                }
            }
            Text(
                stringResource(R.string.title_settings),
                color = Color(0xFF1A1A1A),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        SettingsPanel {
            Text(
                stringResource(R.string.api_settings),
                color = Color(0xFF1A1A1A),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(14.dp))

            SecretField(
                value = viewModel.apiKey,
                onValueChange = viewModel::updateApiKey,
                label = stringResource(R.string.label_api_key),
                placeholder = "sk-...",
                visible = showKey,
                onToggleVisible = { showKey = !showKey },
                accent = accent
            )

            Spacer(Modifier.height(14.dp))

            SecretField(
                value = viewModel.userToken,
                onValueChange = viewModel::updateUserToken,
                label = stringResource(R.string.label_user_token),
                placeholder = "eyJ...",
                visible = showToken,
                onToggleVisible = { showToken = !showToken },
                accent = accent
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = threshold,
                onValueChange = { threshold = it },
                label = { Text(stringResource(R.string.label_threshold)) },
                placeholder = { Text(stringResource(R.string.threshold_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("\u00a5") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF000000),
                    unfocusedTextColor = Color(0xFF000000),
                    focusedLabelColor = accent,
                    unfocusedLabelColor = Color(0xFF666666),
                    focusedBorderColor = accent,
                    unfocusedBorderColor = Color(0xFFCCCCCC),
                    focusedPlaceholderColor = Color(0xFF999999),
                    unfocusedPlaceholderColor = Color(0xFF999999),
                    cursorColor = accent
                )
            )
            Text(
                stringResource(R.string.threshold_desc),
                color = Color(0xFF999999),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    context.getSharedPreferences("whale_prefs", Context.MODE_PRIVATE)
                        .edit().putString("balance_threshold", threshold).apply()
                    viewModel.save(onBack ?: {})
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.btn_save), fontWeight = FontWeight.SemiBold)
            }
        }

        SettingsPanel {
            Text(
                stringResource(R.string.title_usage),
                color = Color(0xFF1A1A1A),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.usage_text),
                color = Color(0xFF666666),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SettingsPanel {
            Text(
                stringResource(R.string.title_language),
                color = Color(0xFF1A1A1A),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            ExposedDropdownMenuBox(
                expanded = langExpanded,
                onExpandedChange = { langExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedLang.displayName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF000000),
                        unfocusedTextColor = Color(0xFF000000),
                        focusedLabelColor = accent,
                        unfocusedLabelColor = Color(0xFF666666),
                        focusedBorderColor = accent,
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        cursorColor = accent
                    )
                )
                ExposedDropdownMenu(
                    expanded = langExpanded,
                    onDismissRequest = { langExpanded = false }
                ) {
                    languageOptions.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang.displayName) },
                            onClick = {
                                selectedLang = lang
                                langExpanded = false
                                context.getSharedPreferences("whale_prefs", Context.MODE_PRIVATE)
                                    .edit().putString("app_language", lang.localeCode).apply()
                                (context as? Activity)?.recreate()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF5F7FA))
            .padding(18.dp),
        content = content
    )
}

@Composable
private fun SecretField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    accent: Color = Color(0xFF4D6BFE)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = onToggleVisible) {
                Text(
                    if (visible) stringResource(R.string.btn_hide) else stringResource(R.string.btn_show),
                    color = Color(0xFF333333)
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF000000),
            unfocusedTextColor = Color(0xFF000000),
            focusedLabelColor = accent,
            unfocusedLabelColor = Color(0xFF666666),
            focusedBorderColor = accent,
            unfocusedBorderColor = Color(0xFFCCCCCC),
            focusedPlaceholderColor = Color(0xFF999999),
            unfocusedPlaceholderColor = Color(0xFF999999),
            cursorColor = accent
        )
    )
}
