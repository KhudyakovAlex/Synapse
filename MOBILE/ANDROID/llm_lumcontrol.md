# LumControl Layer

Новый компонент управления освещением, расположённый между слоем страниц и AI слоем в MainActivity.

## Назначение

Отображение и управление параметрами освещения (слайдеры) и выбором сцен светильников (кнопки сцен).

## Видимость и состояния

### Видимость слоя
- Управляется через параметр `isVisible` в LumControlLayer
- Анимируется с `slideInVertically` / `slideOutVertically` (появление/исчезновение снизу)

### Режимы отображения

**Свёрнутый режим (по умолчанию):**
- Видны: кнопки сцен (D:\Git\Synapse\MOBILE\ANDROID\app\src\main\java\com\awada\synapse\lumcontrol\SceneButtons.kt)
- Ручка торчит выше контейнера

**Развёрнутый режим (если есть слайдеры):**
- Видны: слайдеры + кнопки сцен внизу

## Токены дизайна

### Цвета
- **Фон контейнера и ручки:** `Color_State_tertiary_variant`

### Размеры
- **Скругление углов:** `Radius_Radius_L`
- **Padding контейнера:** 40dp (горизонтальный и вертикальный)

## Поведение ручки (DragHandle)

## Кнопки сцен

### Содержимое
- D:\Git\Synapse\MOBILE\ANDROID\app\src\main\java\com\awada\synapse\lumcontrol\SceneButtons.kt

## Слайдеры

### Расположение
- Выше кнопок сцен
- Видны только в развёрнутом режиме

### Содержимое
- ColorSlider
- SaturationSlider
- TemperatureSlider
- BrightnessSlider

### Динамика
- Набор слайдеров зависит от **типа светильника** на текущей странице

## Позиционирование

### В MainActivity
```kotlin
LumControlLayer(
    isVisible = isLumControlVisible,
    modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
)
```

### Слои приложения (сверху вниз)
1. AI (fillMaxSize)
2. **LumControlLayer** (BottomCenter, fillMaxWidth) ← НОВЫЙ
3. AnimatedContent (PageLocation, PageSettings и т.д.)

### Отступ снизу
- AI компонент всегда занимает минимум 168dp (100dp MAIN_PANEL + 48dp DRAG_HANDLE + 20dp spacing)
- LumControlLayer смещён вверх на этот отступ через `aiBottomPadding`

## File Locations

- **Компонент:** `app/src/main/java/com/awada/synapse/lumcontrol/LumControlLayer.kt`
- **Подкомпоненты:**
  - Кнопки сцен D:\Git\Synapse\MOBILE\ANDROID\app\src\main\java\com\awada\synapse\lumcontrol\SceneButtons.kt
  - Слайдеры D:\Git\Synapse\MOBILE\ANDROID\app\src\main\java\com\awada\synapse\lumcontrol\Sliders.kt
- **Использование:** `activities/MainActivity.kt` (MainContent composable)
