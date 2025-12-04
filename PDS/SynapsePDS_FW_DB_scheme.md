# Схема базы данных прошивки

АПК Синапс v1.0. ПО. Спецификации на разработку

**Последнее изменение:** 04.12.2025

```mermaid
erDiagram
    CONTROLLERS ||--o{ LOCATIONS : "имеет"
    LOCATIONS ||--o{ LUMINAIRES : "содержит"
    LOCATIONS ||--o{ GROUPS : "содержит"
    LOCATIONS ||--o{ PRES_SENSORS : "датчики"
    LOCATIONS ||--o{ BRIGHT_SENSORS : "датчики"
    LOCATIONS ||--o{ BUTTON_PANELS : "панели"
    GROUPS ||--o{ LUMINAIRES : "управляет"
    GROUPS ||--o{ BRIGHT_SENSORS : "используется"
    LUMINAIRES ||--o{ SCENE_LUMINAIRES : "значения в сценах"
    BUTTON_PANELS ||--o{ BUTTONS : "кнопки"
    ACTION_SETS ||--o{ BUTTONS : "перебор"
    ACTIONS ||--o{ BUTTONS : "назначено"
    ACTIONS ||--o{ PRES_SENSORS : "действует"
    ACTIONS ||--o{ EVENTS : "расписание"
    ACTIONS ||--o{ SUBACTIONS : "состоит"

    CONTROLLERS {
        CHAR20 NAME
        CHAR4 PASSWORD
        BOOL IS_SCHEDULE
        UINT8 ICO_NUM
    }

    LOCATIONS {
        UINT8 NUM
        BOOL IS_AUTO
    }

    LUMINAIRES {
        UINT8 DALI_ADDR
        UINT8 LOCATION_NUM
        UINT8 GROUP_NUM
        UINT8 VAL_BRIGHT
        UINT8 VAL_TW
        UINT8 VAL_R
        UINT8 VAL_G
        UINT8 VAL_B
        UINT8 VAL_W
    }

    SCENE_LUMINAIRES {
        UINT8 DALI_ADDR
        UINT8 SCENE_NUM
        UINT8 VAL_BRIGHT
        UINT8 VAL_TW
        UINT8 VAL_R
        UINT8 VAL_G
        UINT8 VAL_B
        UINT8 VAL_W
    }

    GROUPS {
        UINT8 LOCATION_NUM
        UINT8 DALI_NUM
    }

    PRES_SENSORS {
        UINT8 DALI_ADDR
        UINT8 DALI_INST
        UINT8 ACTION_OCCUPANCY_NUM
        UINT8 ACTION_VACANCY_NUM
        UINT8 DELAY
    }

    BRIGHT_SENSORS {
        UINT8 DALI_ADDR
        UINT8 DALI_INST
        UINT8 GROUP_NUM
    }

    BUTTON_PANELS {
        UINT8 DALI_ADDR
    }

    BUTTONS {
        UINT8 DALI_ADDR
        UINT8 DALI_INST
        UINT8 ACTION_SET_SHORT_NUM
        UINT8 ACTION_LONG_NUM
    }

    ACTION_SETS {
        UINT8 NUM
    }

    ACTIONS {
        UINT8 ACTION_SET_NUM
        UINT8 NUM
        UINT8 POS
    }

    SUBACTIONS {
        UINT8 ACTION_NUM
        UINT8 OBJECT_TYPE
        UINT8 OBJECT_NUM
        UINT8 VALUE
    }

    EVENTS {
        CHAR7 DAYS
        CHAR4 TIME
        BOOL SMOOTH
        UINT8 ACTION_NUM
    }
```
