package com.ai.assistance.operit.ui.features.chat.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.assistance.operit.R
import com.ai.assistance.operit.data.model.ChatMessageDisplayMode
import com.ai.assistance.operit.ui.features.chat.viewmodel.ChatViewModel

/**
 * 输入框上方的快捷建议条：
 * - 还没发过消息时给一组开聊建议；
 * - 最后一条是 AI 回复、且不在生成中时，给一组追问建议。
 * 点一下直接把这句话发出去。
 */
@Composable
fun ChatSuggestionChips(
    actualViewModel: ChatViewModel,
    modifier: Modifier = Modifier,
) {
    val chatHistory by actualViewModel.chatHistory.collectAsState()
    val isLoading by actualViewModel.isLoading.collectAsState()

    val visibleMessages =
        chatHistory.filter { message ->
            message.displayMode != ChatMessageDisplayMode.HIDDEN_PLACEHOLDER &&
                message.content.isNotBlank()
        }
    val lastMessage = visibleMessages.lastOrNull()

    val starterSuggestions =
        listOf(
            stringResource(R.string.chat_suggestion_starter_capability),
            stringResource(R.string.chat_suggestion_starter_code),
            stringResource(R.string.chat_suggestion_starter_summary),
            stringResource(R.string.chat_suggestion_starter_translate),
        )
    val followUpSuggestions =
        listOf(
            stringResource(R.string.chat_suggestion_followup_detail),
            stringResource(R.string.chat_suggestion_followup_example),
            stringResource(R.string.chat_suggestion_followup_shorter),
        )

    val suggestions: List<String> =
        when {
            isLoading -> emptyList()
            visibleMessages.isEmpty() -> starterSuggestions
            lastMessage?.sender == "ai" -> followUpSuggestions
            else -> emptyList()
        }

    if (suggestions.isEmpty()) {
        return
    }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        suggestions.forEach { suggestion ->
            Surface(
                onClick = { actualViewModel.sendTextMessage(suggestion) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}
