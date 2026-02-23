# Конвертация SVG иконок в Android Vector Drawable

## Автоматическая конвертация

### Скрипт: `convert_svg_to_android.py`

Автоматически конвертирует все SVG файлы из папки `IMG/` в Android XML Vector Drawable формат и генерирует JSON каталог для использования в коде.

### Структура исходных файлов

```
IMG/
├── Controller/     (26 файлов) - иконки локаций (контроллеров)
├── Location/       (36 файлов) - иконки помещений (Room) *(исторически папка названа Location)*
├── Luminaire/      (36 файлов) - иконки светильников
└── System/         (1 файлов) - системные иконки
```

### Результат конвертации

**XML файлы** сохраняются в `app/src/main/res/drawable/` с префиксами:
- `controller_*.xml` — из папки Controller
- `location_*.xml` — из папки Location *(иконки помещений / Room)*
- `luminaire_*.xml` — из папки Luminaire
- `system_*.xml` — из папки System

**JSON каталог** сохраняется в `app/src/main/res/raw/icons_catalog.json`:
```json
{
  "icons": [
    {
      "category": "controller",
      "id": 100,
      "description": "",
      "resourceName": "controller_100_default"
    }
  ]
}
```

### Запуск конвертации

```bash
python convert_svg_to_android.py
```

**Вывод:**
```
Начало конвертации SVG -> Android XML

Установка зависимостей...
[OK] Встроенные библиотеки доступны

Обработка папки: Controller
  [OK] 100_default.svg -> controller_100_default.xml
  [OK] 101_ofis.svg -> controller_101_ofis.xml
  ...

============================================================
Результаты конвертации:
   Всего файлов: 127
   Успешно: 127
   Ошибок: 0
   Записей в каталоге: 127
============================================================

Результаты сохранены в: D:\Git\Synapse\MOBILE\ANDROID\app\src\main\res\drawable
Каталог сохранён в: D:\Git\Synapse\MOBILE\ANDROID\app\src\main\res\raw\icons_catalog.json

⚠️  НАПОМИНАНИЕ: Обновите описания иконок в icons_catalog.json!
Готово!
```

---

## Как работает скрипт

### 1. Санитизация имён файлов

Преобразует имена SVG в формат Android (lowercase, underscore):
- `Name=AI, Type on-off=On.svg` → `name_ai_type_on_off_on.xml`
- `Logo Synaps.svg` → `logo_synaps.xml`
- `300_default.svg` → `300_default.xml`

### 2. Парсинг SVG

Извлекает из SVG:
- Размеры (`width`, `height`)
- ViewBox
- Пути (`<path>` элементы)
- Атрибуты заливки и обводки (`fill`, `stroke`, `stroke-width`)

### 3. Генерация Android XML

Создаёт Vector Drawable с корректными атрибутами:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M12 2L2 7v10l10 5 10-5V7z"/>
</vector>
```

---

## Добавление новых иконок

### Шаг 1: Добавить SVG в папку IMG

Положить новые SVG файлы в соответствующую папку:
- `IMG/Controller/` — для локаций (контроллеров)
- `IMG/Location/` — для помещений (Room) *(исторически папка названа Location)*
- `IMG/Luminaire/` — для светильников
- `IMG/System/` — для системных иконок

### Шаг 2: Запустить скрипт

```bash
python convert_svg_to_android.py
```

Скрипт автоматически:
- Найдёт все SVG файлы
- Сконвертирует только новые (существующие перезапишет)
- Покажет статистику

### Шаг 3: Обновить описания

После конвертации обновите поле `description` в `icons_catalog.json` для новых иконок.

### Шаг 4: Использовать в коде

```kotlin
// Прямое использование
Icon(
    painter = painterResource(id = R.drawable.controller_100_default),
    contentDescription = "Default controller",
    tint = Color_State_primary
)

// Через каталог (см. раздел "Использование каталога")
val icon = iconCatalog.findById(100)
```

---

## Создание новой папки с иконками

### 1. Создать папку в IMG/

```
IMG/
├── Controller/
├── Location/
├── Luminaire/
├── System/
└── NewCategory/  ← новая папка
```

### 2. Обновить скрипт

Добавить имя папки в список `FOLDERS`:

```python
# Папки для обработки
FOLDERS = ["Controller", "Location", "Luminaire", "System", "NewCategory"]
```

### 3. Запустить конвертацию

Файлы будут сохранены с префиксом `newcategory_*.xml`

---

## Особенности конвертации

### Поддерживаемые SVG атрибуты

✅ **Поддерживается:**
- `<path>` элементы с `d` атрибутом
- `fill` (цвет заливки)
- `stroke` (цвет обводки)
- `stroke-width` (толщина обводки)
- `width`, `height`, `viewBox`

⚠️ **Ограничения:**
- Градиенты конвертируются как solid цвета
- Сложные эффекты (тени, blur) не поддерживаются
- Текст внутри SVG не обрабатывается
- Вложенные группы (`<g>`) игнорируются

### Рекомендации для SVG

Для лучшего результата SVG должны:
- Использовать простые пути (`<path>`)
- Иметь явные размеры (`width`, `height`)
- Не содержать сложных эффектов
- Быть оптимизированы (SVGO)

---

## Ручная конвертация (если нужно)

### Через Android Studio

1. Правый клик на `res/drawable/` → New → Vector Asset
2. Выбрать "Local file (SVG, PSD)"
3. Указать путь к SVG
4. Настроить размеры и цвета
5. Finish

### Онлайн инструменты

- [svg2android.com](https://svg2android.com/)
- [inloop.github.io/svg2android](https://inloop.github.io/svg2android/)

---

## Troubleshooting

### Проблема: Иконка отображается некорректно

**Решение:**
1. Открыть XML файл в `drawable/`
2. Проверить `viewportWidth` и `viewportHeight`
3. Убедиться что `pathData` не пустой
4. Проверить цвета (`fillColor`, `strokeColor`)

### Проблема: Скрипт выдаёт ошибки

**Решение:**
1. Проверить что SVG файл корректный (открывается в браузере)
2. Убедиться что файл в кодировке UTF-8
3. Проверить что путь к файлу не содержит спецсимволов

### Проблема: Цвета не те

**Решение:**
В коде использовать `tint` для переопределения цвета:
```kotlin
Icon(
    painter = painterResource(id = R.drawable.ic_back),
    contentDescription = "Back",
    tint = Color_State_primary  // переопределяет цвет из XML
)
```

---

## Использование каталога в коде

### 1. Подключение зависимости

Добавить в `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
```

В `build.gradle.kts` (модуль app) добавить плагин:

```kotlin
plugins {
    kotlin("plugin.serialization") version "1.9.0"
}
```

---

### 2. Модели данных

Создать файл `IconCatalog.kt`:

```kotlin
package com.synapse.android.data

import kotlinx.serialization.Serializable

@Serializable
data class IconInfo(
    val category: String,
    val id: Int,
    val description: String,
    val resourceName: String
)

@Serializable
data class IconCatalog(
    val icons: List<IconInfo>
) {
    // Фильтрация по категории
    fun getByCategory(category: String): List<IconInfo> =
        icons.filter { it.category == category }
    
    // Поиск по ID
    fun findById(id: Int): IconInfo? =
        icons.find { it.id == id }
    
    // Все категории
    val categories: List<String>
        get() = icons.map { it.category }.distinct()
}
```

---

### 3. Загрузка каталога

Создать `IconCatalogManager.kt`:

```kotlin
package com.synapse.android.data

import android.content.Context
import kotlinx.serialization.json.Json
import com.synapse.android.R

object IconCatalogManager {
    private var catalog: IconCatalog? = null
    
    fun load(context: Context): IconCatalog {
        if (catalog == null) {
            val json = context.resources
                .openRawResource(R.raw.icons_catalog)
                .bufferedReader()
                .use { it.readText() }
            catalog = Json.decodeFromString<IconCatalog>(json)
        }
        return catalog!!
    }
    
    fun getCatalog(): IconCatalog? = catalog
}
```

---

### 4. Использование в Compose

#### Простой компонент с иконкой по ID

```kotlin
@Composable
fun CatalogIcon(
    iconId: Int,
    modifier: Modifier = Modifier,
    tint: Color = Color_State_primary
) {
    val context = LocalContext.current
    val catalog = remember { IconCatalogManager.load(context) }
    val iconInfo = remember(iconId) { catalog.findById(iconId) }
    
    iconInfo?.let {
        val resourceId = remember(it.resourceName) {
            context.resources.getIdentifier(
                it.resourceName,
                "drawable",
                context.packageName
            )
        }
        
        if (resourceId != 0) {
            Icon(
                painter = painterResource(id = resourceId),
                contentDescription = it.description,
                modifier = modifier,
                tint = tint
            )
        }
    }
}

// Использование
CatalogIcon(
    iconId = 100,
    modifier = Modifier.size(24.dp),
    tint = Color_State_primary
)
```

#### Список иконок по категории

```kotlin
@Composable
fun IconCategoryGrid(category: String) {
    val context = LocalContext.current
    val catalog = remember { IconCatalogManager.load(context) }
    val icons = remember(category) { catalog.getByCategory(category) }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(icons) { iconInfo ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { /* обработка клика */ }
            ) {
                CatalogIcon(
                    iconId = iconInfo.id,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = iconInfo.description,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

// Использование
IconCategoryGrid(category = "controller")
```

#### Селектор иконок с категориями

```kotlin
@Composable
fun IconSelector(
    selectedIconId: Int?,
    onIconSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val catalog = remember { IconCatalogManager.load(context) }
    var selectedCategory by remember { mutableStateOf("controller") }
    
    Column {
        // Табы категорий
        TabRow(selectedTabIndex = catalog.categories.indexOf(selectedCategory)) {
            catalog.categories.forEach { category ->
                Tab(
                    selected = category == selectedCategory,
                    onClick = { selectedCategory = category },
                    text = { Text(category.capitalize()) }
                )
            }
        }
        
        // Сетка иконок
        val icons = remember(selectedCategory) {
            catalog.getByCategory(selectedCategory)
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(icons) { iconInfo ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .border(
                            width = 2.dp,
                            color = if (selectedIconId == iconInfo.id) 
                                Color_State_primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onIconSelected(iconInfo.id) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CatalogIcon(iconId = iconInfo.id)
                }
            }
        }
    }
}
```

---

### 5. Примеры использования

#### Отображение контроллера с иконкой

```kotlin
@Composable
fun ControllerCard(controller: Controller) {
    Card {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CatalogIcon(
                iconId = controller.iconId,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = controller.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = controller.location,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
```

#### Выбор иконки для локации (контроллера)

```kotlin
@Composable
fun LocationIconPicker(
    currentIconId: Int,
    onIconChanged: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    OutlinedButton(onClick = { showDialog = true }) {
        CatalogIcon(
            iconId = currentIconId,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Выбрать иконку")
    }
    
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp)
            ) {
                IconSelector(
                    selectedIconId = currentIconId,
                    onIconSelected = {
                        onIconChanged(it)
                        showDialog = false
                    }
                )
            }
        }
    }
}
```

#### Получение описания иконки

```kotlin
fun getIconDescription(context: Context, iconId: Int): String {
    val catalog = IconCatalogManager.load(context)
    return catalog.findById(iconId)?.description ?: "Неизвестная иконка"
}
```

---

### 6. Инициализация в Application

```kotlin
class SynapseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Предзагрузка каталога
        IconCatalogManager.load(this)
    }
}
```

---

## История конвертации

### 05.02.2026 - Первая конвертация
- Сконвертировано: 127 SVG файлов
- Структура: 4 папки (Controller, Location, Luminaire, System)
- Результат: 127 XML файлов в `drawable/`
- Ошибок: 0
