package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.awada.synapse.components.DropdownItem
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.components.ScenarioPoint
import com.awada.synapse.components.ScenarioPointField
import com.awada.synapse.db.ActionEntity
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun PageScenario(
    scenarioId: Long?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val actions by remember(db, scenarioId) {
        if (scenarioId == null) {
            flowOf(emptyList())
        } else {
            db.actionDao().observeAllForScenario(scenarioId)
        }
    }.collectAsState(initial = emptyList())
    val expandedStates = remember { mutableStateMapOf<Long, Boolean>() }

    val whereItems = remember {
        listOf(
            DropdownItem(1, "Гостиная"),
            DropdownItem(2, "Кухня"),
            DropdownItem(3, "Спальня"),
        )
    }
    val whatItems = remember {
        listOf(
            DropdownItem(1, "Выключить"),
            DropdownItem(2, "Включить"),
            DropdownItem(3, "Яркость"),
            DropdownItem(4, "Сцена"),
        )
    }
    val valueItems = remember {
        listOf(
            DropdownItem(0, "0"),
            DropdownItem(1, "1"),
            DropdownItem(25, "25"),
            DropdownItem(50, "50"),
            DropdownItem(75, "75"),
            DropdownItem(100, "100"),
        )
    }

    PageContainer(
        title = if (scenarioId == null) "Сценарий" else "Сценарий $scenarioId",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16),
            verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_12),
        ) {
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_12))

            actions.forEach { action ->
                val expanded = expandedStates[action.id] ?: false
                ScenarioPoint(
                    title = "Действие ${action.position + 1}",
                    expanded = expanded,
                    onExpandedChange = { expandedStates[action.id] = it },
                    whereField = ScenarioPointField(
                        value = action.whereId,
                        onValueChange = { value ->
                            scope.launch {
                                db.actionDao().update(action.copy(whereId = value))
                            }
                        },
                        placeholder = "Не выбрано",
                        dropdownItems = whereItems,
                    ),
                    whatField = ScenarioPointField(
                        value = action.whatId,
                        onValueChange = { value ->
                            scope.launch {
                                db.actionDao().update(action.copy(whatId = value))
                            }
                        },
                        placeholder = "Не выбрано",
                        dropdownItems = whatItems,
                    ),
                    valueField = ScenarioPointField(
                        value = action.valueId,
                        onValueChange = { value ->
                            scope.launch {
                                db.actionDao().update(action.copy(valueId = value))
                            }
                        },
                        placeholder = "Не выбрано",
                        dropdownItems = valueItems,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_12))
            PrimaryIconButtonLarge(
                text = "Добавить",
                onClick = {
                    val resolvedScenarioId = scenarioId ?: return@PrimaryIconButtonLarge
                    scope.launch {
                        val nextPosition = db.actionDao().getNextPositionForScenario(resolvedScenarioId)
                        val newActionId = db.actionDao().insert(
                            ActionEntity(
                                scenarioId = resolvedScenarioId,
                                position = nextPosition,
                            )
                        )
                        expandedStates[newActionId] = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

