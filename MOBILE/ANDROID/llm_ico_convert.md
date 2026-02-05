# Конвертация SVG иконок в Android Vector Drawable

## Автоматическая конвертация

### Скрипт: `convert_svg_to_android.py`

Автоматически конвертирует все SVG файлы из папки `IMG/` в Android XML Vector Drawable формат.

### Структура исходных файлов

```
IMG/
├── Controller/     (26 файлов) - иконки контроллеров/помещений
├── Location/       (36 файлов) - иконки локаций
├── Luminaire/      (36 файлов) - иконки светильников
└── System/         (29 файлов) - системные иконки
```

### Результат конвертации

Все файлы сохраняются в `app/src/main/res/drawable/` с префиксами:
- `controller_*.xml` — из папки Controller
- `location_*.xml` — из папки Location
- `luminaire_*.xml` — из папки Luminaire
- `system_*.xml` — из папки System

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
============================================================

Результаты сохранены в: D:\Git\Synapse\MOBILE\ANDROID\app\src\main\res\drawable
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
- `IMG/Controller/` — для контроллеров
- `IMG/Location/` — для локаций
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

### Шаг 3: Использовать в коде

```kotlin
Icon(
    painter = painterResource(id = R.drawable.controller_100_default),
    contentDescription = "Default controller",
    tint = Color_State_primary
)
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

## История конвертации

### 05.02.2026 - Первая конвертация
- Сконвертировано: 127 SVG файлов
- Структура: 4 папки (Controller, Location, Luminaire, System)
- Результат: 127 XML файлов в `drawable/`
- Ошибок: 0
