# Схема базы данных Synapse (Mermaid ERD)

```mermaid
erDiagram
    CONTROLLER {
        NAME string "название контроллера"
        PASSWORD string "пинкод 4 цифры"
        IS_SCHEDULE boolean "включено расписание"
        ICO_NUM int "номер иконки"
    }

    LOCATIONS {
        NUM PK int "порядковый номер"
        NAME string "название локации"
        ICO_NUM int "номер иконки"
        IS_AUTO boolean "автоматический режим"
        SCALE int "масштаб сетки 1-3"
        POS int "позиция в списке 0-255"
    }

    GROUPS {
        ID PK int
        NAME string "название группы"
        DALI_NUM int "номер группы в DALI"
        LOCATION_ID FK int "идентификатор локации"
    }

    LUMINAIRES {
        ID PK int
        NAME string "название светильника"
        DALI_ADDR int "короткий адрес DALI"
        LOCATION_ID FK int "идентификатор локации"
        GROUP_ID FK int "идентификатор группы (nullable)"
        ICO_NUM int "номер иконки"
        POS_X int "колонка 0-255"
        POS_Y int "ряд 0-255"
    }

    PRES_SENSORS {
        ID PK int
        NAME string "название датчика"
        DALI_ADDR int "короткий адрес DALI"
        DALI_INST int "номер инстанса"
        LOCATION_ID FK int "идентификатор локации"
        DELAY int "задержка выключения сек"
    }

    BRIGHT_SENSORS {
        ID PK int
        NAME string "название датчика"
        DALI_ADDR int "короткий адрес DALI"
        DALI_INST int "номер инстанса"
        LOCATION_ID FK int "идентификатор локации"
        GROUP_ID FK int "управляемая группа"
    }

    BUTTON_PANELS {
        ID PK int
        NAME string "название панели"
        DALI_ADDR int "короткий адрес DALI"
        LOCATION_ID FK int "идентификатор локации"
    }

    BUTTONS {
        ID PK int
        NUM int "номер кнопки на панели"
        BUTTON_PANEL_ID FK int "идентификатор панели"
        DALI_INST int "номер инстанса"
        ACTION_ID FK int "действие по нажатию"
    }

    ACTIONS {
        ID PK int
        GROUP_ID FK int "группа (nullable)"
        LUMINAIRE_ID FK int "светильник (nullable)"
        SCENE_ID FK int "сцена (nullable)"
        VAL_BRIGHT int "яркость"
        VAL_TW int "температура белого"
        VAL_R int "красный RGB"
        VAL_G int "зеленый RGB"
        VAL_B int "синий RGB"
        VAL_W int "белый RGBW"
    }

    EVENTS {
        ID PK int
        DATE_EVERYDAY boolean "ежедневное"
        DATE_DAYS string "по дням недели"
        DATE_SPECIFIC date "конкретная дата"
        TIME time "время события"
        SMOOTH boolean "плавное изменение"
        ACTION_ID FK int "идентификатор действия"
    }

    SCENES {
        ID PK int
        NAME string "название сцены"
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

