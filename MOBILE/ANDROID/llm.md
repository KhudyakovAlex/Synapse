# Synapse Android — Документация для LLM

## Общие правила
- **НЕ ТРОГАЕМ СЛОЙ AI БЕЗ ЗАПРОСА У ПОЛЬЗОВАТЕЛЯ!!! ЗАПРОС В ЧАТЕ ДОЛЖЕН БЫТЬ КАПСЛОКОМ!!!**
- Растровые изображения брать из `IMG/`
- Векторные изображения (SVG/иконки/логотипы) вытягивать по MCP из Pixso
- Вопросы задавать с пронумерованными вариантами ответов (1, 2, 3...)

## Разделы документации

### [Токены Pixso](llm_tokens.md)
Цвета, размеры, строки, типографика. Синхронизация токенов из Pixso, маппинг ID → Kotlin, иерархия базовых токенов и алиасов.

### [UI Компоненты](llm_components.md)
Готовые компоненты: кнопки (Keyboard, Pin, Primary, Secondary), Tooltip, Toggle, FabButton, AppBar, PageContainer.

### [AI Панель](llm_ai_layer.md)
Нижняя панель с тумблерами и FAB (AIMain), чат bottom sheet (AIChat), контейнер AI, gesture логика.

### [Навигация и Экраны](llm_navigation.md)
Экраны приложения (PageLocation, PageSettings, PagePassword), навигация через AnimatedContent, MainActivity setup.

### [Pixso Workflow](llm_pixso_workflow.md)
Работа с дизайном: получение структуры (getNodeDSL), семантические токены, экспорт иконок.

### [Структура проекта](llm_project_structure.md)
Package структура, список иконок в `res/drawable/`.

### [Конвертация иконок](llm_ico_convert.md)
Экспорт SVG из Pixso и конвертация в Android Vector Drawable.
