Правило для LLM:
- Растровые изображения брать из IMG.
- Векторные изображения (SVG/иконки/логотипы) вытягивать по MCP из Pixso.
- Вопросы задавать с пронумерованными вариантами ответов (1, 2, 3...).

## Токены Pixso

**⚠️ ЗАФИКСИРОВАНО: изменения кода по работе с токенами только с подтверждения пользователя!**

### Синхронизация
Команда: **"обнови токены из Pixso"**

### Файлы токенов
- `ui/theme/PixsoColors.kt` — цвета (83 base + 33 aliases)
- `ui/theme/PixsoDimens.kt` — размеры
- `ui/theme/PixsoStrings.kt` — строки/шрифты
- `ui/theme/PixsoTypography.kt` — TextStyle на основе токенов
- `tokens/pixso_tokens_map.json` — маппинг Pixso ID → Kotlin name

### Иерархия токенов
Токены разделены на **базовые** и **алиасы**:
```kotlin
// ===== BASE VALUES =====
val Font_Size_Size_16 = 16.sp

// ===== ALIASES =====
val Body_Body_L_Size = Font_Size_Size_16  // ссылка на базовый
```
При изменении базового токена — все алиасы обновятся автоматически.

### Привязка токенов
Связь по **Pixso ID** (не по имени):
```json
{
  "2:14": {
    "pixsoName": "Color/Primary/Primary_40",
    "kotlinName": "Color_Primary_Primary_40",
    "type": "color",
    "isAlias": false,
    "value": "0xFFRRGGBB"
  }
}
```

### Отслеживание изменений
При синхронизации сравниваем ВСЁ:
- **Структура**: добавленные (NEW) / удалённые (REMOVED)
- **Цвета** (type=color): hex значение `0xAARRGGBB`
- **Числа** (type=number): округлённое значение
- **Строки** (type=string): текстовое значение
- **Ссылки** (isAlias=true): `refId` — ID целевого токена

### При синхронизации
1. Получить токены: MCP `getVariableSets`
2. Сравнить с `pixso_tokens_map.json`
3. Показать изменения пользователю
4. После подтверждения — обновить Kotlin файлы и маппинг

### Правила генерации
- **Округление чисел**: `-0.30000001192092896` → `-0.3` (до 1 знака после точки)
- **Отрицательные в скобках**: `(-0.3).sp` для корректного Kotlin синтаксиса

---

## Компоненты UI

### Созданные компоненты

#### UIToggle.kt
Тумблер с иконкой внутри.
```kotlin
UIToggle(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconOn: Int,    // @DrawableRes
    iconOff: Int,   // @DrawableRes
    enabled: Boolean = true
)
```
**Токены:**
- Track ON: `Color_Bg_bg_shade_primary`
- Track OFF/Disabled: `Color_Bg_bg_shade_disabled`
- Track border: `Color_State_pressed_shade_4`
- Thumb ON: `Color_State_secondary`
- Thumb OFF: `Color_State_tertiary`
- Thumb Disabled: `Color_State_disabled`
- Icon ON: `Color_State_on_secondary`
- Icon OFF: `Color_State_on_tertiary`
- Icon Disabled: `Color_State_on_disabled`
- Radius: `Radius_Radius_Full` (200dp, pill shape)

#### UIFabButton.kt
Круглая FAB кнопка.
```kotlin
UIFabButton(
    state: FabState,  // Default, ActiveStroke1, ActiveStroke12, Disabled
    onClick: () -> Unit,
    icon: Int,         // @DrawableRes
    iconDisabled: Int  // @DrawableRes
)
```
**Токены:**
- Background Default: `Color_Bg_bg_surface`
- Background Active: `Color_Bg_bg_primary_light`
- Background Disabled: `Color_State_disabled`
- Border Active: `Color_Border_border_focus` (1dp или 12dp)
- Icon Default/Active: `Color_State_primary`
- Icon Disabled: `Color_State_on_disabled`

#### UIAIMain.kt
Нижняя панель с тумблерами и FAB кнопкой.
```kotlin
UIAIMain(
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

#### UIAIChat.kt
Чат (bottom sheet) с drag gesture.
```kotlin
UIAIChat(
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
- Свёрнуто: видна только черточка над UIAIMain
- Развёрнуто: чат выезжает вверх, отступ 100dp от верха экрана
- Drag gesture с анимацией (300ms)
- Порог переключения: 50dp

#### UIAI.kt
Контейнер для UIAIChat и UIAIMain. Управляет состоянием чата.
```kotlin
UIAI(modifier: Modifier = Modifier)
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
- UIAIChat под UIAIMain (z-order)
- Drag может начинаться с UIAIChat или UIAIMain
- Tap на элементах UIAIMain работает нормально

---

## Workflow: Компоненты из Pixso

### Шаг 1: Получить структуру
1. Пользователь выделяет компонент в Pixso
2. Вызвать MCP `getNodeDSL` — получить JSON структуры
3. Проанализировать: размеры, layout, вложенность

### Шаг 2: Получить токены
**MCP НЕ даёт привязки токенов напрямую!** Только raw цвета.

Workflow:
1. Спросить пользователя какие семантические токены назначены
2. Пользователь смотрит в Pixso и называет токены
3. Использовать эти токены в коде

### Шаг 3: Экспорт иконок
1. Пользователь выделяет иконку в Pixso
2. MCP `getExportImage` с `imageType: 3` (SVG)
3. Конвертировать в Android Vector Drawable
4. Сохранить в `res/drawable/`

### Важно: Семантические токены
Дизайнер привязывает к компонентам **семантические** токены (например `bg_elevated`).
Мы используем их в коде. Если дизайнер потом изменит базовый токен — 
наш код автоматически получит новый цвет после синхронизации токенов.

---

## MainActivity

Navigation bar настроен:
- Цвет: `Color_Bg_bg_elevated`
- Иконки: белые (`isAppearanceLightNavigationBars = false`)

#### UIAppBar.kt
Типовая шапка страницы.
```kotlin
UIAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null
)
```
**Особенности:**
- Заголовок выровнен по левому краю (`TextAlign.Start`).
- Если кнопки "Назад" нет, текст прижат влево (отступ 24dp).
- Если кнопка есть, текст идет сразу за ней (отступ 60dp).
- **Токены:** Фон `bg_subtle`, текст `text_1_level`, стиль `Headline S`, обводка снизу `border_shade_8` (1dp).

#### PageContainer.kt
Базовая обертка для страниц.
```kotlin
PageContainer(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    isScrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
)
```
**Логика:** Фиксированный `UIAppBar` сверху, под ним область контента (опционально скроллируемая).

---

## Навигация и Экраны

### Экраны
- `PageLocation.kt` — начальный экран "Локации" (без кнопки "Назад").
- `PageSettings.kt` — экран "Настройки" (с кнопкой "Назад").

### Навигация (MainActivity)
Реализована через `AnimatedContent` с горизонтальной анимацией (slide):
- Переход в настройки: слайд справа налево.
- Возврат: слайд слева направо.
- Все страницы лежат в `MainActivity` слоем **ниже** `UIAI` (чат всегда поверх).

---

## Иконки (res/drawable/)

- `ic_ai.xml` — AI on
- `ic_ai_off.xml` — AI off
- `ic_microphone.xml` — микрофон on
- `ic_microphone_off.xml` — микрофон off (disabled)
- `ic_volume.xml` — звук on
- `ic_volume_off.xml` — звук off
