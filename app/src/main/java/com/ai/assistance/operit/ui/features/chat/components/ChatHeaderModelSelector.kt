package com.ai.assistance.operit.ui.features.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.assistance.operit.R
import com.ai.assistance.operit.api.chat.EnhancedAIService
import com.ai.assistance.operit.data.model.FunctionType
import com.ai.assistance.operit.data.model.getModelByIndex
import com.ai.assistance.operit.data.model.getModelList
import com.ai.assistance.operit.data.model.getValidModelIndex
import com.ai.assistance.operit.data.preferences.FunctionConfigMapping
import com.ai.assistance.operit.data.preferences.FunctionalConfigManager
import com.ai.assistance.operit.data.preferences.ModelConfigManager
import kotlinx.coroutines.launch

/**
 * 顶部标题栏里的模型选择器：显示当前聊天模型，点开直接切换。
 */
@Composable
fun ChatHeaderModelSelector(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val functionalConfigManager = remember { FunctionalConfigManager(context) }
    val modelConfigManager = remember { ModelConfigManager(context) }
    val mappings by
        functionalConfigManager.functionConfigMappingWithIndexFlow.collectAsState(initial = emptyMap())
    val summaries by modelConfigManager.configSummariesFlow.collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }

    val mapping =
        mappings[FunctionType.CHAT]
            ?: FunctionConfigMapping(FunctionalConfigManager.DEFAULT_CONFIG_ID, 0)
    val currentConfig = summaries.firstOrNull { it.id == mapping.configId }
    val notSelected = stringResource(R.string.not_selected)
    val currentModelName =
        currentConfig?.let { config ->
            val index = getValidModelIndex(config.modelName, mapping.modelIndex)
            getModelByIndex(config.modelName, index).ifBlank { notSelected }
        } ?: notSelected

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = currentModelName,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 96.dp),
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (summaries.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(text = notSelected) },
                    onClick = { expanded = false },
                )
            }
            summaries.forEach { summary ->
                val models = getModelList(summary.modelName)
                val entries = if (models.isEmpty()) listOf("") else models
                entries.forEachIndexed { modelIndex, modelName ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text =
                                    if (entries.size > 1) {
                                        "${summary.name} · $modelName"
                                    } else {
                                        summary.name
                                    },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = {
                            expanded = false
                            scope.launch {
                                functionalConfigManager.setConfigForFunction(
                                    FunctionType.CHAT,
                                    summary.id,
                                    modelIndex,
                                )
                                EnhancedAIService.refreshServiceForFunction(context, FunctionType.CHAT)
                            }
                        },
                    )
                }
            }
        }
    }
}
