# UI Компоненты

## components/Buttons.kt
Кнопки различных типов для клавиатуры, PIN-кода и интерфейса.

### KeyboardButton
Кнопка клавиатуры:
- Size: 88×68dp, Radius: 16dp
- Стили: Default (текст), Icon (иконка), Help (длинный текст)
- State: Default (bg_surface + border), Pressed (secondary_pressed)

### PinButton
Индикатор PIN-кода:
- Size: 49×56dp, Radius: 8dp
- States: Default (пусто), Input (символ), Error (красный)

### PrimaryButton
Основная кнопка:
- Height: 44dp, адаптивная ширина, Radius: 40dp (pill)
- States: Default (primary), Pressed (primary_pressed), Disabled
- Text: Button M style, текст смещён вверх на 2dp

### SecondaryButton
Вторичная кнопка с обводкой:
- Height: 44dp, min width 80dp, Radius: 24dp
- Border: border_primary (только enabled)
- States: Default (secondary), Pressed (secondary_pressed), Disabled
- Text: Button M style, текст смещён вверх на 2dp

## components/Tooltips.kt
Модальное окно с текстом и кнопками.
```kotlin
Tooltip(
    text: String,
    primaryButtonText: String,
    onResult: (TooltipResult) -> Unit,
    secondaryButtonText: String? = null
)
```
**Особенности:**
- Окно 328dp ширина, Radius_M (24dp)
- Затемнение фона (scrim)
- AI слой остаётся сверху (zIndex: Tooltip=999, AI=1000)
- Клик вне окна → TooltipResult.Dismissed
- Клик на кнопку → TooltipResult.Primary / Secondary

**Токены:**
- Background: `bg_surface`
- Scrim: `bg_scrim`
- Text: `text_1_level` + Body L style

## components/Toggle.kt
Тумблер с иконкой внутри.
```kotlin
Toggle(
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

## components/FabButton.kt
Круглая FAB кнопка.
```kotlin
FabButton(
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

## components/AppBar.kt
Типовая шапка страницы.
```kotlin
AppBar(
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

## pages/PageContainer.kt
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
**Логика:** Фиксированный `AppBar` сверху, под ним область контента (опционально скроллируемая).
