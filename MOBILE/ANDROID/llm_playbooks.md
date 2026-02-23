# Playbooks (рецепты) для частых задач

Формат: **куда смотреть → что править → как проверить**. Если задача не похожа ни на один рецепт — сначала заполни [`llm_current_task.md`](llm_current_task.md).

## 1) Добавить/изменить экран
- **Куда смотреть**:
  - Экраны: `app/src/main/java/com/awada/synapse/pages/`
  - Навигация/анимации: `app/src/main/java/com/awada/synapse/activities/MainActivity.kt`
  - Док: [`llm_navigation.md`](llm_navigation.md)
- **Что править**:
  - Создать `Page*.kt` и подключить в `AnimatedContent` (состояние + переходы).
  - Использовать общие компоненты из `components/` и токены из `ui/theme/`.
- **Как проверить**:
  - Прогнать сценарий перехода туда/обратно, убедиться что back работает, а AI‑слой остаётся поверх.

## 2) Поменять токены (цвета/радиусы/типографика/строки)
- **Куда смотреть**:
  - Док: [`llm_tokens.md`](llm_tokens.md), [`llm_pixso_workflow.md`](llm_pixso_workflow.md)
  - Код: `app/src/main/java/com/awada/synapse/ui/theme/` (например `PixsoColors.kt`, `PixsoDimens.kt`, `PixsoTypography.kt`, `PixsoStrings.kt`)
- **Что править**:
  - Обновлять токены через согласованный workflow (из Pixso), сохранять имена стабильными.
- **Как проверить**:
  - Открыть 2–3 ключевые страницы (локации/настройки/PIN), сравнить базовые цвета/типографику/отступы.

## 3) Добавить/обновить иконку
- **Куда смотреть**:
  - Док: [`llm_ico_convert.md`](llm_ico_convert.md)
  - Маппинг/каталог: `app/src/main/java/com/awada/synapse/data/IconCatalog*.kt`, `.../components/IconResolver.kt`, `.../components/ControllerIconMapper.kt`
  - Ресурсы: `app/src/main/res/drawable/`
- **Что править**:
  - Добавить Vector Drawable и обновить каталог/маппинг, чтобы иконка была доступна в UI (например в выборе иконок).
- **Как проверить**:
  - Открыть экран выбора иконки, убедиться что новая иконка видна, корректно масштабируется и не “прыгает” по baseline.

## 4) Правки LumControl (слайдеры/сцены/поведение)
- **Куда смотреть**:
  - Док: [`llm_lumcontrol.md`](llm_lumcontrol.md)
  - Код: `app/src/main/java/com/awada/synapse/lumcontrol/` (`LumControlLayer.kt`, `Sliders.kt`, `SceneButtons.kt`)
- **Что править**:
  - Держать слои: Pages (низ) → LumControl → AI (верх).
  - Не ломать drag‑handle/паддинги относительно AI.
- **Как проверить**:
  - Свернуть/развернуть слой, проверить клики по сценам, проверить что ничего не перекрывает AI.

## 5) Данные/настройки/Помещение (Room)
- **Куда смотреть**:
  - DB: `app/src/main/java/com/awada/synapse/db/`
  - Репозитории/настройки: `app/src/main/java/com/awada/synapse/data/`
- **Что править**:
  - Любые изменения схемы помещения (Room) делать осознанно: миграции/версия/совместимость.
- **Как проверить**:
  - Холодный старт приложения, повторный старт, сценарий “сохранить → перезапустить → убедиться что сохранилось”.

## 6) AI‑слой (ЗАПРЕТ ПО УМОЛЧАНИЮ)
- **Куда смотреть**: [`llm_ai_layer.md`](llm_ai_layer.md), `app/src/main/java/com/awada/synapse/ai/`
- **Правило**: **не менять без явной команды пользователя капсом** (см. `llm.md`).

