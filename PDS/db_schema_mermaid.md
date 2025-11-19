# Схема базы данных Synapse (Mermaid ERD)

```mermaid
erDiagram
    CONTROLLER {
        string NAME "название контроллера"
        string PASSWORD "пинкод 4 цифры"
        boolean IS_SCHEDULE "включено расписание"
        int ICO_NUM "номер иконки"
    }

    LOCATIONS {
        int NUM PK "порядковый номер"
        string NAME "название локации"
        int ICO_NUM "номер иконки"
        boolean IS_AUTO "автоматический режим"
        int SCALE "масштаб сетки 1-3"
        int POS "позиция в списке 0-255"
    }

    GROUPS {
        int ID PK
        string NAME "название группы"
        int DALI_NUM "номер группы в DALI"
        int LOCATION_ID FK "идентификатор локации"
    }

    LUMINAIRES {
        int ID PK
        string NAME "название светильника"
        int DALI_ADDR "короткий адрес DALI"
        int LOCATION_ID FK "идентификатор локации"
        int GROUP_ID FK "идентификатор группы (nullable)"
        int ICO_NUM "номер иконки"
        int POS_X "колонка 0-255"
        int POS_Y "ряд 0-255"
    }

    PRES_SENSORS {
        int ID PK
        string NAME "название датчика"
        int DALI_ADDR "короткий адрес DALI"
        int DALI_INST "номер инстанса"
        int LOCATION_ID FK "идентификатор локации"
        int DELAY "задержка выключения сек"
    }

    BRIGHT_SENSORS {
        int ID PK
        string NAME "название датчика"
        int DALI_ADDR "короткий адрес DALI"
        int DALI_INST "номер инстанса"
        int LOCATION_ID FK "идентификатор локации"
        int GROUP_ID FK "управляемая группа"
    }

    BUTTON_PANELS {
        int ID PK
        string NAME "название панели"
        int DALI_ADDR "короткий адрес DALI"
        int LOCATION_ID FK "идентификатор локации"
    }

    BUTTONS {
        int ID PK
        int NUM "номер кнопки на панели"
        int BUTTON_PANEL_ID FK "идентификатор панели"
        int DALI_INST "номер инстанса"
        int ACTION_ID FK "действие по нажатию"
    }

    ACTIONS {
        int ID PK
        int GROUP_ID FK "группа (nullable)"
        int LUMINAIRE_ID FK "светильник (nullable)"
        int SCENE_ID FK "сцена (nullable)"
        int VAL_BRIGHT "яркость"
        int VAL_TW "температура белого"
        int VAL_R "красный RGB"
        int VAL_G "зеленый RGB"
        int VAL_B "синий RGB"
        int VAL_W "белый RGBW"
    }

    EVENTS {
        int ID PK
        boolean DATE_EVERYDAY "ежедневное"
        string DATE_DAYS "по дням недели"
        date DATE_SPECIFIC "конкретная дата"
        time TIME "время события"
        boolean SMOOTH "плавное изменение"
        int ACTION_ID FK "идентификатор действия"
    }

    SCENES {
        int ID PK
        string NAME "название сцены"
    }

    %% Связи локаций
    LOCATIONS ||--o{ LUMINAIRES : "содержит"
    LOCATIONS ||--o{ GROUPS : "содержит"
    LOCATIONS ||--o{ PRES_SENSORS : "содержит"
    LOCATIONS ||--o{ BRIGHT_SENSORS : "содержит"
    LOCATIONS ||--o{ BUTTON_PANELS : "содержит"

    %% Связи групп
    GROUPS ||--o{ LUMINAIRES : "объединяет"
    GROUPS ||--o{ BRIGHT_SENSORS : "управляется"
    GROUPS ||--o{ ACTIONS : "управляет"

    %% Связи светильников
    LUMINAIRES ||--o{ ACTIONS : "управляет"

    %% Связи кнопочных панелей
    BUTTON_PANELS ||--o{ BUTTONS : "содержит"

    %% Связи действий
    ACTIONS ||--o{ BUTTONS : "выполняется"
    ACTIONS ||--o{ EVENTS : "выполняется"
    SCENES ||--o{ ACTIONS : "использует"
```

## Легенда

**Жирным** в описании помечены рабочие поля (необходимые для работы освещения).

Остальные поля — интерфейсные (используются только приложением).

## Типы связей

- `||--o{` — один ко многим
- `(nullable)` — поле может быть NULL

## Основные сущности

1. **CONTROLLER** — настройки контроллера (одна запись)
2. **LOCATIONS** — помещения/локации
3. **GROUPS** — группы светильников
4. **LUMINAIRES** — отдельные светильники
5. **PRES_SENSORS** — датчики присутствия
6. **BRIGHT_SENSORS** — датчики освещённости
7. **BUTTON_PANELS** — кнопочные панели управления
8. **BUTTONS** — отдельные кнопки на панелях
9. **ACTIONS** — действия (изменение яркости, цвета и т.д.)
10. **EVENTS** — события расписания
11. **SCENES** — световые сцены

