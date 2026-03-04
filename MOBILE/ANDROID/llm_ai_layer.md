# AI Панель

**⚠️ НЕ ТРОГАЕМ СЛОЙ AI БЕЗ ЗАПРОСА У ПОЛЬЗОВАТЕЛЯ!!! ЗАПРОС В ЧАТЕ ДОЛЖЕН БЫТЬ КАПСЛОКОМ!!!**

## ai/AIMain.kt
Нижняя панель с тумблерами и FAB кнопкой.
```kotlin
AIMain(
    modifier: Modifier = Modifier,
    onVerticalDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {}
)
```
**Токены:**
- Background: `Color_Bg_bg_elevated`
- Radius top: `Radius_Radius_L` (40dp)
- Radius bottom: `Radius_Radius_None` (0dp)
- Height: 100dp
- Padding: start=16, end=16, top=4, bottom=0

**Состав:**
- Левый тумблер: AI on/off (ic_ai, ic_ai_off)
- Центр: FAB микрофон (ic_microphone, ic_microphone_off)
- Правый тумблер: Volume on/off (ic_volume, ic_volume_off)

**Логика:** Когда AI выключен → FAB disabled, правый тумблер disabled

**Gesture:** Vertical drag на панели открывает чат, tap на элементах работает нормально (touchSlop=18px)

## ai/AIChat.kt
Чат (bottom sheet) с drag gesture.
```kotlin
AIChat(
    modifier: Modifier = Modifier,
    currentOffsetPx: Float,
    screenHeightPx: Float,
    dragHandleHeightPx: Float,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
)
```

**Структура:**
- **Drag handle область**: 48dp высота, прозрачный фон
- **Drag handle черточка**: 40×4dp, pill (`Radius_Full`), `Color_Bg_bg_elevated`, padding bottom 10dp
- **Серая плашка**: `Color_Bg_bg_canvas`, скругление top=`Radius_L`, bottom=`Radius_None`

**Поведение:**
- Свёрнуто: видна только черточка над AIMain
- Развёрнуто: чат выезжает вверх, отступ 100dp от верха экрана
- Drag gesture с анимацией (300ms)
- Порог переключения: 50dp

## ai/AI.kt
Контейнер для AIChat и AIMain. Управляет состоянием чата.
```kotlin
AI(modifier: Modifier = Modifier)
```
**Состояние (lifted up):**
- `isExpanded` — развёрнут ли чат
- `dragOffsetPx` — текущее смещение при drag

**Константы:**
- `DRAG_HANDLE_HEIGHT` = 48dp
- `MAIN_PANEL_HEIGHT` = 100dp
- `EXPANDED_TOP_OFFSET` = 100dp
- `DRAG_THRESHOLD` = 50dp

**Координация:**
- `navigationBarsPadding()` применяется к общему контейнеру
- AIChat под AIMain (z-order)
- Drag может начинаться с AIChat или AIMain
- Tap на элементах AIMain работает нормально

---

## Как AIChat работает с удалённой LLM (интерфейсные моменты)

### Сеть / сервер модели
- Клиент: `ai/OllamaClient.kt` (OkHttp).
- Сервер: Ollama HTTP API (endpoint `...:11434/api/generate`).
- Запрос: JSON `{ model, prompt, stream:false }`.
- Ответ: ожидаем поле `response` (если парсинг не удался — используем raw body).
- Модель задаётся константой в `OllamaClient` (например `gpt-oss:20b`).

### Формирование prompt (контекст диалога)
- Prompt собирается в `ai/AIChat.kt` функцией `buildPrompt(...)`.
- В prompt добавляется системная фраза про стиль ответа (русский, кратко) + история сообщений.
- История берётся из БД: последние `N` сообщений (сейчас используется `limit = 24`), с ролями `USER` и `AI`.

### Поведение UI при запросе
- При нажатии Send:
  - пользовательское сообщение сохраняется в БД сразу (с timestamp),
  - отправка блокируется флагом `isSending` (второй запрос параллельно не стартуем),
  - сетевой вызов выполняется в `Dispatchers.IO`,
  - ответ сохраняется в БД как роль `AI`.
- Стриминга в UI нет (используем `stream=false`), ответ приходит целиком.
- Ошибки отображаются текстом в чате (например `"Ошибка Ollama HTTP ..."` / `"Ошибка запроса к Ollama: ..."`).

### Разрешение HTTP (важно для Android)
- Для доступа по HTTP (без TLS) домен/IP должен быть разрешён в `res/xml/network_security_config.xml`
  (в проекте уже добавлен домен `10.10.1.184`).
