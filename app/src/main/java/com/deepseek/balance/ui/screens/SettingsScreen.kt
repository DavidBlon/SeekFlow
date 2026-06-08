package com.deepseek.balance.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.balance.data.repository.UsageRepository
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

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isWhaleBlue = remember {
        context.getSharedPreferences("whale_prefs", Context.MODE_PRIVATE)
            .getBoolean("is_whale_blue", false)
    }

    var showKey by remember { mutableStateOf(false) }
    var showToken by remember { mutableStateOf(false) }
    var threshold by remember {
        mutableStateOf(
            context.getSharedPreferences("whale_prefs", Context.MODE_PRIVATE)
                .getString("balance_threshold", "") ?: ""
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\u8fd4\u56de", tint = Color(0xFF333333))
            }
            Text(
                "\u8bbe\u7f6e",
                color = Color(0xFF1A1A1A),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        SettingsPanel {
            Text(
                "DeepSeek API \u8bbe\u7f6e",
                color = Color(0xFF1A1A1A),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(14.dp))

            SecretField(
                value = viewModel.apiKey,
                onValueChange = viewModel::updateApiKey,
                label = "API Key\uff08\u67e5\u8be2\u4f59\u989d\uff09",
                placeholder = "sk-...",
                visible = showKey,
                onToggleVisible = { showKey = !showKey },
                isWhaleBlue = isWhaleBlue
            )

            Spacer(Modifier.height(14.dp))

            SecretField(
                value = viewModel.userToken,
                onValueChange = viewModel::updateUserToken,
                label = "User Token\uff08\u67e5\u8be2\u7528\u91cf\uff09",
                placeholder = "eyJ...",
                visible = showToken,
                onToggleVisible = { showToken = !showToken },
                isWhaleBlue = isWhaleBlue
            )

            Spacer(Modifier.height(14.dp))

            val accent = if (isWhaleBlue) Color(0xFF4D6BFE) else Color(0xFF424242)
            OutlinedTextField(
                value = threshold,
                onValueChange = { threshold = it },
                label = { Text("\u9608\u503c (\u00a5)") },
                placeholder = { Text("\u4f8b\u5982 10") },
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
                "\u5f53\u4f59\u989d\u4f4e\u4e8e\u8bbe\u5b9a\u9608\u503c\u65f6\uff0c\u901a\u8fc7\u901a\u77e5\u63d0\u9192\u4f60\u3002\u7559\u7a7a\u5219\u4e0d\u63d0\u9192\u3002",
                color = Color(0xFF999999),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    context.getSharedPreferences("whale_prefs", Context.MODE_PRIVATE)
                        .edit().putString("balance_threshold", threshold).apply()
                    viewModel.save(onBack)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWhaleBlue) Color(0xFF4D6BFE) else Color(0xFF424242),
                    contentColor = Color.White
                )
            ) {
                Text("\u4fdd\u5b58", fontWeight = FontWeight.SemiBold)
            }
        }

        SettingsPanel {
            Text(
                "\u4f7f\u7528\u8bf4\u660e",
                color = Color(0xFF1A1A1A),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "API Key \u7528\u4e8e\u67e5\u8be2\u8d26\u6237\u4f59\u989d\uff0c\u53ef\u5728 platform.deepseek.com/api_keys \u521b\u5efa\u6216\u590d\u5236\u3002\n\n" +
                    "User Token \u7528\u4e8e\u67e5\u8be2\u6bcf\u65e5\u7528\u91cf\u660e\u7ec6\u3002\u767b\u5f55 platform.deepseek.com \u540e\u6253\u5f00\u6d4f\u89c8\u5668\u5f00\u53d1\u8005\u5de5\u5177\uff0c\u5728 Console \u4e2d\u6267\u884c\uff1a\n\n" +
                    "localStorage.getItem('userToken')\n\n" +
                    "\u590d\u5236\u8fd4\u56de\u503c\u5e76\u7c98\u8d34\u5230\u4e0a\u65b9\u8f93\u5165\u6846\u3002",
                color = Color(0xFF666666),
                style = MaterialTheme.typography.bodyMedium
            )
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
    isWhaleBlue: Boolean
) {
    val accent = if (isWhaleBlue) Color(0xFF4D6BFE) else Color(0xFF424242)

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
                    if (visible) "\u9690\u85cf" else "\u663e\u793a",
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
