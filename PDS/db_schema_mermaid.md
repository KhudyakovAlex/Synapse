# Схема базы данных Synapse (Mermaid ERD)

```mermaid
erDiagram
    CONTROLLERS {
        int ID PK "идентификатор в приложении"
        int SCALE "масштаб сетки локаций"
        string NAME "название контроллера (char20)"
        string PASSWORD "пароль 4 цифры (char4)"
        boolean IS_SCHEDULE "включено расписание"
        int ICO_NUM "номер иконки (uint8_t)"
    }

    LOCATIONS {
        int ID PK "идентификатор в приложении"
        string NAME "название локации"
        int ICO_NUM "номер иконки"
        int SCALE "масштаб сетки светильников"
        int POS_X "позиция по X"
        int POS_Y "позиция по Y"
        int CONTROLLER_ID FK "контроллер"
        int NUM "номер локации (uint8_t)"
        boolean IS_AUTO "автоматический режим"
    }

    LUMINAIRES {
        int ID PK "идентификатор в приложении"
        string NAME "название светильника"
        int ICO_NUM "номер иконки"
        int POS_X "позиция по X"
        int POS_Y "позиция по Y"
        int LOCATION_ID FK "локация"
        int GROUP_ID FK "группа (nullable)"
        int DALI_ADDR "короткий адрес DALI (uint8_t)"
        int LOCATION_NUM "номер локации (uint8_t)"
        int GROUP_NUM "номер группы DALI (uint8_t)"
        int VAL_BRIGHT "яркость (uint8_t)"
        int VAL_TW "температура света (uint8_t)"
        int VAL_R "красный RGB (uint8_t)"
        int VAL_G "зелёный RGB (uint8_t)"
        int VAL_B "синий RGB (uint8_t)"
        int VAL_W "белый RGBW (uint8_t)"
    }

    SCENE_LUMINAIRES {
        int ID PK "идентификатор в приложении"
        int LUMINAIRE_ID FK "светильник"
        int DALI_ADDR "адрес DALI (uint8_t)"
        int SCENE_NUM "номер сцены DALI (uint8_t)"
        int VAL_BRIGHT "яркость (uint8_t)"
        int VAL_TW "температура света (uint8_t)"
        int VAL_R "красный RGB (uint8_t)"
        int VAL_G "зелёный RGB (uint8_t)"
        int VAL_B "синий RGB (uint8_t)"
        int VAL_W "белый RGBW (uint8_t)"
    }

    GROUPS {
        int ID PK "идентификатор в приложении"
        string NAME "название группы"
        int LOCATION_ID FK "локация"
        int LOCATION_NUM "номер локации (uint8_t)"
        int DALI_NUM "номер группы DALI (uint8_t)"
        int SCENE_BRIGHT_0 "освещённость сцена 0 (uint8_t)"
        int SCENE_BRIGHT_1 "освещённость сцена 1 (uint8_t)"
        int SCENE_BRIGHT_2 "освещённость сцена 2 (uint8_t)"
        int SCENE_BRIGHT_3 "освещённость сцена 3 (uint8_t)"
        int SCENE_BRIGHT_4 "освещённость сцена 4 (uint8_t)"
    }

    PRES_SENSORS {
        int ID PK "идентификатор в приложении"
        string NAME "название датчика"
        int LOCATION_ID FK "локация"
        int ACTION_YES_ID FK "действие при движении (nullable)"
        int ACTION_NO_ID FK "действие без движения (nullable)"
        int DALI_ADDR "адрес DALI (uint8_t)"
        int DALI_INST "инстанс DALI (uint8_t)"
        int LOCATION_NUM "номер локации (uint8_t)"
        int ACTION_YES_NUM "номер действия ДА (uint8_t)"
        int ACTION_NO_NUM "номер действия НЕТ (uint8_t)"
        int DELAY "задержка HOLD_TIME (uint8_t)"
    }

    BRIGHT_SENSORS {
        int ID PK "идентификатор в приложении"
        string NAME "название датчика"
        int LOCATION_ID FK "локация"
        int GROUP_ID FK "управляемая группа (nullable)"
        int DALI_ADDR "адрес DALI (uint8_t)"
        int DALI_INST "инстанс DALI (uint8_t)"
        int LOCATION_NUM "номер локации (uint8_t)"
        int GROUP_NUM "номер группы DALI (uint8_t)"
    }

    BUTTON_PANELS {
        int ID PK "идентификатор в приложении"
        string NAME "название панели"
        int LOCATION_ID FK "локация"
        int DALI_ADDR "адрес DALI (uint8_t)"
        int LOCATION_NUM "номер локации (uint8_t)"
    }

    BUTTONS {
        int ID PK "идентификатор в приложении"
        string NAME "название кнопки"
        int BUTTON_PANEL_ID FK "панель"
        int ACTION_SHORT_ID FK "действие короткое (nullable)"
        int ACTION_LONG_ID FK "действие долгое (nullable)"
        int DALI_ADDR "адрес DALI панели (uint8_t)"
        int DALI_INST "инстанс кнопки (uint8_t)"
        int ACTION_SHORT_NUM "номер действия короткое (uint8_t)"
        int ACTION_LONG_NUM "номер действия долгое (uint8_t)"
    }

    ACTIONS {
        int ID PK "идентификатор в приложении"
        int NUM "номер действия (uint8_t)"
    }

    SUBACTIONS {
        int ID PK "идентификатор в приложении"
        int ACTION_ID FK "действие (приложение)"
        int ACTION_NUM "номер действия (uint8_t)"
        int OBJECT_NUM "объект изменений (uint8_t)"
        int VALUE "значение (uint8_t)"
    }

    EVENTS {
        int ID PK "идентификатор в приложении"
        int ACTION_ID FK "действие (приложение)"
        string DAYS "дни недели FFTFFFF (char7)"
        string TIME "время HHMM (char4)"
        boolean SMOOTH "плавное изменение"
        int ACTION_NUM "номер действия (uint8_t)"
    }

    %% Связи контроллера
    CONTROLLERS ||--o{ LOCATIONS : "содержит"

    %% Связи локаций
    LOCATIONS ||--o{ LUMINAIRES : "содержит"
    LOCATIONS ||--o{ GROUPS : "содержит"
    LOCATIONS ||--o{ PRES_SENSORS : "содержит"
    LOCATIONS ||--o{ BRIGHT_SENSORS : "содержит"
    LOCATIONS ||--o{ BUTTON_PANELS : "содержит"

    %% Связи групп
    GROUPS ||--o{ LUMINAIRES : "объединяет"
    GROUPS ||--o{ BRIGHT_SENSORS : "управляется"

    %% Связи светильников
    LUMINAIRES ||--o{ SCENE_LUMINAIRES : "имеет сцены"

    %% Связи кнопочных панелей
    BUTTON_PANELS ||--o{ BUTTONS : "содержит"

    %% Связи действий
    ACTIONS ||--o{ SUBACTIONS : "состоит из"
    ACTIONS ||--o{ PRES_SENSORS : "выполняется YES"
    ACTIONS ||--o{ PRES_SENSORS : "выполняется NO"
    ACTIONS ||--o{ BUTTONS : "выполняется SHORT"
    ACTIONS ||--o{ BUTTONS : "выполняется LONG"
    ACTIONS ||--o{ EVENTS : "выполняется"
```

## Легенда

**Жирным** в спецификации БД помечены рабочие поля (необходимые для работы освещения в прошивке).

Остальные поля — интерфейсные (используются только приложением).

## Типы связей

- `||--o{` — один ко многим
- `(nullable)` — поле может быть NULL

## Основные сущности

1. **CONTROLLERS** — настройки контроллера (одна запись в прошивке)
2. **LOCATIONS** — помещения/локации (до 16)
3. **LUMINAIRES** — отдельные светильники (до 64)
4. **SCENE_LUMINAIRES** — параметры светильников в сценах (до 320)
5. **GROUPS** — группы светильников (до 16)
6. **PRES_SENSORS** — датчики присутствия (до 64)
7. **BRIGHT_SENSORS** — датчики освещённости (до 64)
8. **BUTTON_PANELS** — кнопочные панели управления (до 64)
9. **BUTTONS** — отдельные кнопки на панелях (до 64)
10. **ACTIONS** — действия с устройствами (до 255)
11. **SUBACTIONS** — поддействия (детализация действий) (до 255)
12. **EVENTS** — события расписания (до 255)

## Особенности структуры

### Двойная система идентификации

Каждая таблица имеет два типа идентификаторов:

- **ID** — для приложения (SQLite)
- **NUM/DALI_ADDR** — для прошивки (массивы структур C)

### Действия и поддействия

- **ACTIONS** — контейнер действия с уникальным номером
- **SUBACTIONS** — конкретные команды внутри действия (изменение яркости, цвета, сцены и т.д.)

Одно действие может содержать несколько поддействий для управления разными объектами одновременно.

### Сцены светильников

- **SCENE_LUMINAIRES** — хранит параметры каждого светильника для каждой сцены
- Сцены программируются в DALI-устройствах
- Контроллер хранит копию для синхронизации между приложениями

### Датчики присутствия

Имеют два действия:

- **ACTION_YES** — при обнаружении движения
- **ACTION_NO** — при отсутствии движения (с задержкой DELAY)

### Кнопки

Каждая кнопка может иметь два действия:

- **ACTION_SHORT** — по короткому нажатию
- **ACTION_LONG** — по долгому нажатию

## Память в прошивке

Общий объём памяти для рабочих данных: **~8.5 КБ**

Самые большие таблицы:

- EVENTS — 3.3 КБ (38.3%)
- SCENE_LUMINAIRES — 2.5 КБ (29.5%)
- SUBACTIONS — 765 байт (8.8%)
