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
- **Скругление углов плашки:** `Radius_Radius_L` (40dp) - всегда одинаковое, не меняется при разворачивании
- **Padding контейнера:** 12dp (внутренний)
- **Отступы от краёв экрана:** 13dp (горизонтальный)
- **Высота ручки:** 48dp
- **Размер полоски ручки:** 40dp × 4dp
- **Скругление полоски:** `Radius_Radius_Full` (полное)

## Поведение ручки (DragHandle)

### Взаимодействие
- **Свайп вверх:** разворачивает слой (показывает слайдеры)
- **Свайп вниз:** сворачивает слой (скрывает слайдеры, остаются только кнопки сцен)
- **Порог свайпа:** 30f пикселей
- **Реализация:** `detectVerticalDragGestures` с `pointerInput(isExpanded)`

### Позиционирование ручки
- Ручка накладывается на плашку сверху
- **Середина блока ручки (24dp от верха)** совпадает с **верхним краем плашки**
- Плашка имеет `padding(top = 24.dp)` для компенсации
- Полоска внутри ручки расположена на 12dp от верха ручки
- Центр полоски (12dp + 2dp) = 14dp от верха ручки, что на 10dp выше верхнего края плашки

## Кнопки сцен

### Содержимое
- Используется компонент `QuickButtonsRow` из SceneButtons.kt
- 5 кнопок: "Выкл", "1", "2", "3", "Вкл" (`defaultQuickButtons`)
- Данные сцен (список, названия, иконки) берутся из SceneButtons.kt
- Обработка кликов также в SceneButtons.kt
- Всегда видны (и в свёрнутом, и в развёрнутом состоянии)

## Слайдеры

### Расположение
- Выше кнопок сцен
- Видны только в развёрнутом режиме

### Содержимое
- **ColorSlider** - спектр цветов (0-100)
- **SaturationSlider** - насыщенность (0-100), требует параметр `dynamicColor`
- **TemperatureSlider** - температура (3000K-5000K)
- **BrightnessSlider** - яркость (0-100%)

### Динамика
- Набор слайдеров зависит от **типа светильника** на текущей странице (будет реализовано позже по контексту)
- Слайдеры передаются через параметр `sliders: List<String>` в LumControlLayer
- Spacing между слайдерами: 12dp

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
- LumControlLayer смещён вверх на: **178dp** (100dp AIMain + 48dp AI handle + 30dp spacing)
- Параметр `bottomPadding` в LumControlLayer = 178dp по умолчанию
- Расчёт высоты для `aiBottomPadding` будет реализован позже

## Структура компонента

```
AnimatedVisibility (slideIn/slideOut снизу)
└── Box (padding bottom = 178dp)
    ├── Column (padding top = 24dp) - плашка
    │   └── Column (padding horizontal = 13dp, clip + background)
    │       ├── Column (sliders) - только если isExpanded
    │       │   ├── ColorSlider
    │       │   ├── SaturationSlider
    │       │   ├── TemperatureSlider
    │       │   └── BrightnessSlider
    │       └── QuickButtonsRow - всегда видна
    └── DragHandle (align TopCenter) - накладывается на плашку
```

## Важные детали реализации

### Скругление
- Используется `clip(RoundedCornerShape(Radius_L))` ПЕРЕД `background`
- Без `shape` параметра в `background` - только цвет
- Скругление фиксированное, не зависит от контента

### Анимация
- Появление/исчезновение всего слоя: `slideInVertically` / `slideOutVertically`
- Разворачивание слайдеров: простое `if (isExpanded)` без дополнительной анимации
- State управляется через `remember { mutableStateOf(false) }`

### Нижний край
- Нижний край плашки остаётся на месте при разворачивании
- Плашка растёт вверх, добавляя слайдеры сверху кнопок

## File Locations

- **Компонент:** `app/src/main/java/com/awada/synapse/lumcontrol/LumControlLayer.kt`
- **Подкомпоненты:**
  - Кнопки сцен: `app/src/main/java/com/awada/synapse/lumcontrol/SceneButtons.kt`
  - Слайдеры: `app/src/main/java/com/awada/synapse/lumcontrol/Sliders.kt`
- **Использование:** `activities/MainActivity.kt` (MainContent composable)
