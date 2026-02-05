# Токены Pixso

**⚠️ ЗАФИКСИРОВАНО: изменения кода по работе с токенами только с подтверждения пользователя! ЗАПРОС В ЧАТЕ ДОЛЖЕН БЫТЬ КАПСЛОКОМ!!!**

## Синхронизация
Команда: **"обнови токены из Pixso"**

## Файлы токенов
- `ui/theme/PixsoColors.kt` — цвета (83 base + 33 aliases)
- `ui/theme/PixsoDimens.kt` — размеры
- `ui/theme/PixsoStrings.kt` — строки/шрифты
- `ui/theme/PixsoTypography.kt` — TextStyle на основе токенов
- `tokens/pixso_tokens_map.json` — маппинг Pixso ID → Kotlin name

## Иерархия токенов
Токены разделены на **базовые** и **алиасы**:
```kotlin
// ===== BASE VALUES =====
val Font_Size_Size_16 = 16.sp

// ===== ALIASES =====
val Body_Body_L_Size = Font_Size_Size_16  // ссылка на базовый
```
При изменении базового токена — все алиасы обновятся автоматически.

## Привязка токенов
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

## Отслеживание изменений
При синхронизации сравниваем ВСЁ:
- **Структура**: добавленные (NEW) / удалённые (REMOVED)
- **Цвета** (type=color): hex значение `0xAARRGGBB`
- **Числа** (type=number): округлённое значение
- **Строки** (type=string): текстовое значение
- **Ссылки** (isAlias=true): `refId` — ID целевого токена

## При синхронизации
1. Получить токены: MCP `getVariableSets`
2. Сравнить с `pixso_tokens_map.json`
3. Показать изменения пользователю
4. После подтверждения — обновить Kotlin файлы и маппинг

## Правила генерации
- **Округление чисел**: `-0.30000001192092896` → `-0.3` (до 1 знака после точки)
- **Отрицательные в скобках**: `(-0.3).sp` для корректного Kotlin синтаксиса
