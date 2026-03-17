# AI Панель

**⚠️ НЕ ТРОГАЕМ СЛОЙ AI БЕЗ ЗАПРОСА У ПОЛЬЗОВАТЕЛЯ!!! ЗАПРОС В ЧАТЕ ДОЛЖЕН БЫТЬ КАПСЛОКОМ!!!**

## ai/AIMain.kt
Нижняя панель с тумблерами и FAB-кнопкой.

```kotlin
AIMain(
    modifier: Modifier = Modifier,
    anchoredDraggableState: AnchoredDraggableState<ChatState>
)
```

**Токены и layout:**
- Background: `Color_Bg_bg_elevated`
- Radius top: `Radius_Radius_L`
- Radius bottom: `Radius_Radius_None`
- Height: `100.dp`
- Padding: `start=16, end=16, top=4, bottom=0`

**Состав:**
- Левый тумблер: AI on/off (`ic_ai`, `ic_ai_off`)
- Центр: FAB микрофон (`ic_microphone`, `ic_microphone_off`)
- Правый тумблер: Volume on/off (`ic_volume`, `ic_volume_off`)

**Логика:**
- Когда AI выключен, FAB disabled и правый тумблер disabled
- Сам `AIMain` участвует в vertical drag через `anchoredDraggable`

## ai/AIChat.kt
Чат как `bottom sheet`, привязанный к состоянию drag.

```kotlin
AIChat(
    modifier: Modifier = Modifier,
    currentOffsetPx: Float,
    screenHeightPx: Float,
    expandedTopOffsetPx: Float,
    mainPanelHeightPx: Float,
    anchoredDraggableState: AnchoredDraggableState<ChatState>,
    uiContext: LLMUiContext,
    onNavigationCommand: (LLMNavigationCommand) -> Unit = {}
)
```

**Структура:**
- drag handle: `48.dp`
- handle bar: `40x4.dp`
- контент чата: `Color_Bg_bg_canvas`, top radius = `Radius_L`
- input bar фиксирован над `AIMain`

**Поведение:**
- свёрнуто: видна только ручка над `AIMain`
- развёрнуто: чат поднимается до `EXPANDED_TOP_OFFSET`
- сообщения в UI берутся только из `AI_MESSAGES`
- системные prompt/JSON-контекст в UI не показываются

## ai/AI.kt
Контейнер для `AIChat` и `AIMain`, управляет drag/scrim и прокидывает UI-контекст.

```kotlin
AI(
    modifier: Modifier = Modifier,
    uiContext: LLMUiContext,
    onNavigationCommand: (LLMNavigationCommand) -> Unit = {},
    onMainPanelTopPxChanged: ((Float) -> Unit)? = null
)
```

**Константы:**
- `DRAG_HANDLE_HEIGHT = 48.dp`
- `MAIN_PANEL_HEIGHT = 100.dp`
- `EXPANDED_TOP_OFFSET = 60.dp`

**Координация:**
- `navigationBarsPadding()` на общем контейнере
- `AIChat` расположен под `AIMain`
- при раскрытии рисуется scrim
- `onNavigationCommand(...)` переводит приложение на нужный экран после ответа LLM

---

## Актуальная схема общения с LLM

### Транспорт
- Клиент: `ai/OllamaClient.kt`
- Endpoint: `...:11434/api/chat`
- Формат запроса: `messages[]`, `stream=false`
- Для строгого структурного ответа используется `format="json"`

### Откуда берётся system prompt
- System prompt хранится в `app/src/main/assets/llm_system_prompt.md`
- Загружается через `ai/LLMSystemPromptLoader.kt`
- Если asset недоступен, используется fallback-текст в коде

### Кто собирает запрос
- Основная orchestration-логика находится в `ai/LLMOrchestrator.kt`
- В `AIChat` при Send:
  - сообщение пользователя сразу пишется в `AI_MESSAGES`
  - затем вызывается `LLMOrchestrator.processUserMessage(...)`
  - по завершении в `AI_MESSAGES` сохраняется только пользовательский видимый ответ `assistantText`

### Что уходит в LLM
В модель уходят `messages`:
- `system`: markdown system prompt
- `system`: `UI_CONTEXT_JSON`
- `system`: `APP_DB_STATE_JSON`
- история видимых сообщений `USER/AI`

### Что НЕ уходит в UI-чат
- system prompt
- `UI_CONTEXT_JSON`
- `APP_DB_STATE_JSON`
- внутренний структурный JSON ответа модели

---

## Scope данных для LLM

### Общий принцип
- Если пользователь находится внутри конкретной локации/контроллера, в LLM отправляется state только этого контроллера
- Если controller scope определить нельзя, отправляется полный state приложения

### Как определяется controller scope
`LLMOrchestrator` пытается вычислить `controllerId` по текущему `LLMUiContext`:
- `selectedLocationControllerId`
- `selectedRoomControllerId`
- `selectedGroupControllerId`
- либо через выбранные сущности (`luminaire`, `buttonPanel`, `presSensor`, `brightSensor`, `scenario`)

### Что попадает в controller-scoped `APP_DB_STATE_JSON`
- `CONTROLLERS`: ровно 1 запись
- controller-scoped:
  - `ROOMS`
  - `LUMINAIRES`
  - `PRES_SENSORS`
  - `BRIGHT_SENSORS`
  - `BUTTON_PANELS`
  - `BUTTONS`
  - `SCENARIOS`
  - `ACTIONS`
  - `SCENARIO_SET`
  - `EVENTS`
  - `GRAPHS`
  - `GRAPH_POINTS`
  - `LUMINAIRE_SCENES`
- глобальные, без фильтра:
  - `GROUPS`
  - `LUMINAIRE_TYPES`

### Важное изменение схемы БД
- В `SCENARIOS` добавлен `CONTROLLER_ID`
- Новые сценарии создаются уже с привязкой к контроллеру
- Миграция пытается восстановить `CONTROLLER_ID` старых сценариев по:
  - `EVENTS`
  - `SCENARIO_SET -> BUTTONS -> BUTTON_PANELS`
  - `BUTTONS.LONG_PRESS_SCENARIO_ID -> BUTTON_PANELS`

---

## Формат ответа LLM

Модель должна вернуть один JSON-объект:

```json
{
  "assistantText": "Короткий текст для пользователя по-русски",
  "dbPatch": {
    "updates": [
      {
        "table": "TABLE_NAME",
        "where": { "PRIMARY_KEY": 1 },
        "values": { "COLUMN": 123 }
      }
    ]
  },
  "navigation": {
    "screen": "ScreenName",
    "controllerId": 1,
    "roomId": 2,
    "groupId": 3,
    "luminaireId": 4,
    "presSensorId": 5,
    "brightSensorId": 6,
    "buttonPanelId": 7,
    "buttonNumber": 2,
    "scenarioId": 8
  }
}
```

### Как это применяется
- `assistantText`:
  - показывается пользователю в чате
  - сохраняется в `AI_MESSAGES`
- `dbPatch`:
  - применяется через `ai/LLMDbPatchApplier.kt`
  - разрешены только whitelist-таблицы/колонки
  - только `UPDATE` существующих строк, без `INSERT/DELETE`
- `navigation`:
  - маппится в `AppScreen` и `selected...` state в `MainActivity`

### Fallback
- Если JSON не распарсился, raw-ответ модели превращается в `assistantText`
- В этом случае пользователь всё равно увидит текстовый ответ, но структурные действия не будут извлечены

---

## Логирование в Logdog

### При старте приложения
На событии `process_start` отправляется:
- обычный log event
- fields с версией приложения
- markdown attachment `llm-system-prompt.md` с текущим system prompt

### При запросе к LLM
Отправляется событие `LLM hidden context`:
- `fields`:
  - `controllerScopeId`
  - `uiContextChars`
  - `appStateChars`
- attachments:
  - `ui-context.json`
  - `app-db-state.json`

### При ответе LLM
Отправляется событие `LLM structured response`:
- `fields`:
  - `assistantTextChars`
  - `updateCount`
  - `navigationScreen`
- attachment:
  - `llm-structured-response.json`

### Что логируется отдельно текстом
- пользовательский запрос логируется как `USER: ...`
- итоговый видимый ответ логируется как `LLM: ...`
- отдельный markdown attachment с `assistantText` больше не используется

---

## Разрешение HTTP
- Для доступа по HTTP (без TLS) хост Ollama должен быть разрешён в `res/xml/network_security_config.xml`
- Для Logdog аналогично нужен доступ по сети к `LOGDOG_HOST:LOGDOG_PORT`
