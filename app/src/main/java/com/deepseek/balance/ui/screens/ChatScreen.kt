package com.deepseek.balance.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.balance.R
import com.deepseek.balance.data.api.ChatMessage
import com.deepseek.balance.data.repository.ChatRepository
import com.deepseek.balance.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiMessage(
    val id: Long,
    val role: String,
    val content: String
)

data class ChatState(
    val messages: List<UiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val hasApiKey: Boolean = false,
    val streamingMsgId: Long? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val usageRepository: UsageRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var nextId = 0L

    init {
        viewModelScope.launch {
            val key = usageRepository.apiKey.first()
            _state.update { it.copy(hasApiKey = key.isNotBlank()) }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _state.value.isLoading) return

        val userMsg = UiMessage(id = nextId++, role = "user", content = text.trim())
        _state.update { it.copy(messages = it.messages + userMsg, isLoading = true) }

        viewModelScope.launch {
            val apiMessages = _state.value.messages.map { ChatMessage(role = it.role, content = it.content) }
            val result = chatRepository.sendMessage(apiMessages)
            result.onSuccess { reply ->
                val msgId = nextId++
                val aiMsg = UiMessage(id = msgId, role = reply.role, content = reply.content)
                _state.update { it.copy(messages = it.messages + aiMsg, isLoading = false, streamingMsgId = msgId) }
            }.onFailure { e ->
                val errMsg = UiMessage(
                    id = nextId++, role = "assistant",
                    content = "Error: ${e.message ?: "Unknown error"}"
                )
                _state.update { it.copy(messages = it.messages + errMsg, isLoading = false) }
            }
        }
    }

    fun finishStreaming(msgId: Long) {
        _state.update { if (it.streamingMsgId == msgId) it.copy(streamingMsgId = null) else it }
    }
}

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    ChatContent(
        messages = state.messages,
        isLoading = state.isLoading,
        hasApiKey = state.hasApiKey,
        streamingMsgId = state.streamingMsgId,
        onSend = viewModel::sendMessage,
        onFinishStreaming = viewModel::finishStreaming
    )
}

@Composable
private fun ChatContent(
    messages: List<UiMessage>,
    isLoading: Boolean,
    hasApiKey: Boolean,
    streamingMsgId: Long?,
    onSend: (String) -> Unit,
    onFinishStreaming: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    isStreaming = msg.id == streamingMsgId,
                    onFinishStreaming = onFinishStreaming
                )
            }
            if (isLoading) {
                item {
                    ThinkingBubble()
                }
            }
        }

        InputBar(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                if (!hasApiKey) return@InputBar
                onSend(inputText)
                inputText = ""
                focusManager.clearFocus()
            },
            enabled = hasApiKey && !isLoading
        )
    }
}

@Composable
private fun ChatBubble(
    message: UiMessage,
    isStreaming: Boolean,
    onFinishStreaming: (Long) -> Unit
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ))
                .background(if (isUser) Color(0xFFF0F2F5) else Color(0xFF4D6BFE))
                .padding(horizontal = 16.dp, vertical = 11.dp)
        ) {
            if (isStreaming) {
                StreamingText(
                    fullText = message.content,
                    msgId = message.id,
                    onFinish = onFinishStreaming,
                    color = Color.White,
                    lineHeight = 22.sp
                )
            } else {
                Text(
                    text = message.content,
                    color = if (isUser) Color(0xFF1A1A1A) else Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun StreamingText(
    fullText: String,
    msgId: Long,
    onFinish: (Long) -> Unit,
    color: Color,
    lineHeight: androidx.compose.ui.unit.TextUnit
) {
    var visibleCount by remember(fullText) { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing)
        ),
        label = "cursorAlpha"
    )

    LaunchedEffect(fullText) {
        val total = fullText.length
        if (total == 0) {
            onFinish(msgId)
            return@LaunchedEffect
        }
        val steps = minOf(total, 100)
        val chunk = maxOf(1, total / steps)
        for (i in 0..total step chunk) {
            visibleCount = i
            delay(12)
        }
        visibleCount = total
        finished = true
        onFinish(msgId)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = fullText.substring(0, visibleCount.coerceAtMost(fullText.length)),
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = lineHeight
        )
        if (!finished && visibleCount > 0) {
            Box(
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(2.dp, 16.dp)
                    .background(color.copy(alpha = cursorAlpha))
            )
        }
    }
}

@Composable
private fun ThinkingBubble() {
    var dots by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            dots = ""
            delay(350)
            dots = "."
            delay(350)
            dots = ".."
            delay(350)
            dots = "..."
            delay(350)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .background(Color(0xFF4D6BFE).copy(alpha = 0.5f))
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(
                text = dots,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("输入消息...", color = Color(0xFF999999)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1A1A1A),
                unfocusedTextColor = Color(0xFF1A1A1A),
                focusedBorderColor = Color(0xFF4D6BFE),
                unfocusedBorderColor = Color(0xFFDDDDDD),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color(0xFF4D6BFE)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (text.isNotBlank()) onSend() })
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = enabled && text.isNotBlank(),
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(if (text.isNotBlank()) Color(0xFF4D6BFE) else Color(0xFFCCCCCC))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.chat_send),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
