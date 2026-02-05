# Навигация и Экраны

## Экраны

### pages/PageLocation.kt
Начальный экран "Локации" (без кнопки "Назад").

### pages/PageSettings.kt
Экран "Настройки" (с кнопкой "Назад").

### pages/PagePassword.kt
Экран ввода PIN-кода (4 цифры, клавиатура, с кнопкой "Назад").

#### Логика PagePassword
```kotlin
PagePassword(
    correctPassword: String,
    onPasswordCorrect: () -> Unit,
    onBackClick: () -> Unit
)
```
**Поведение:**
- Пользователь вводит 4 цифры через клавиатуру
- При вводе 4-й цифры автоматически проверяется пароль
- **Правильный пароль**: вызывается `onPasswordCorrect()` → закрывает страницу
- **Неправильный пароль**: 
  - PIN индикаторы становятся красными (Error state)
  - Через 1 секунду очищается и сбрасывается для повторного ввода
- Клавиатура: цифры 0-9, backspace (удаление), "Не могу войти" (help)
- Контент центрирован вертикально с учётом AppBar вверху и AIMain внизу (100dp)

## Навигация (activities/MainActivity)
Реализована через `AnimatedContent` с горизонтальной анимацией (slide):
- Переход в настройки: слайд справа налево.
- Возврат: слайд слева направо.
- Все страницы лежат в `MainActivity` слоем **ниже** `AI` (чат всегда поверх).

## MainActivity Setup
Navigation bar настроен:
- Цвет: `Color_Bg_bg_elevated`
- Иконки: белые (`isAppearanceLightNavigationBars = false`)
