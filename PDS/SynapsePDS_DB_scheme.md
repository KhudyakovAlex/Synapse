# Схема базы данных

АПК Синапс v1.0. ПО. Спецификации на разработку

**Последнее изменение:** 26.11.2025

```mermaid
erDiagram
    CONTROLLERS ||--o{ LOCATIONS : "содержит"
    LOCATIONS ||--o{ LUMINAIRES : "содержит"
    LOCATIONS ||--o{ GROUPS : "содержит"
    LOCATIONS ||--o{ PRES_SENSORS : "содержит"
    LOCATIONS ||--o{ BRIGHT_SENSORS : "содержит"
    LOCATIONS ||--o{ BUTTON_PANELS : "содержит"
    GROUPS ||--o{ LUMINAIRES : "группирует"
    GROUPS ||--o{ BRIGHT_SENSORS : "управляет"
    LUMINAIRES ||--o{ SCENE_LUMINAIRES : "участвует в"
    BUTTON_PANELS ||--o{ BUTTONS : "содержит"
    BUTTONS ||--o{ ACTION_SETS : "короткое нажатие"
    BUTTONS ||--o{ ACTIONS : "длинное нажатие"
    PRES_SENSORS ||--o{ ACTIONS : "движение есть"
    PRES_SENSORS ||--o{ ACTIONS : "движения нет"
    ACTION_SETS ||--o{ ACTIONS : "содержит"
    ACTIONS ||--o{ SUBACTIONS : "содержит"
    ACTIONS ||--o{ EVENTS : "запускается по"

    CONTROLLERS {
        INTEGER ID PK
        INTEGER SCALE
        TEXT NAME
        TEXT PASSWORD
        INTEGER IS_SCHEDULE
        INTEGER ICO_NUM
    }

    LOCATIONS {
        INTEGER ID PK
        TEXT NAME
        INTEGER ICO_NUM
        INTEGER SCALE
        INTEGER POS_X
        INTEGER POS_Y
        INTEGER CONTROLLER_ID FK
        INTEGER NUM
        INTEGER IS_AUTO
    }

    LUMINAIRES {
        INTEGER ID PK
        TEXT NAME
        INTEGER ICO_NUM
        INTEGER POS_X
        INTEGER POS_Y
        INTEGER LOCATION_ID FK
        INTEGER GROUP_ID FK
        INTEGER DALI_ADDR
        INTEGER LOCATION_NUM
        INTEGER GROUP_NUM
        INTEGER VAL_BRIGHT
        INTEGER VAL_TW
        INTEGER VAL_R
        INTEGER VAL_G
        INTEGER VAL_B
        INTEGER VAL_W
    }

    SCENE_LUMINAIRES {
        INTEGER ID PK
        INTEGER LUMINAIRE_ID FK
        INTEGER DALI_ADDR
        INTEGER SCENE_NUM
        INTEGER VAL_BRIGHT
        INTEGER VAL_TW
        INTEGER VAL_R
        INTEGER VAL_G
        INTEGER VAL_B
        INTEGER VAL_W
    }

    GROUPS {
        INTEGER ID PK
        TEXT NAME
        INTEGER LOCATION_ID FK
        INTEGER LOCATION_NUM
        INTEGER DALI_NUM
        INTEGER SCENE_BRIGHT_0
        INTEGER SCENE_BRIGHT_1
        INTEGER SCENE_BRIGHT_2
        INTEGER SCENE_BRIGHT_3
        INTEGER SCENE_BRIGHT_4
    }

    PRES_SENSORS {
        INTEGER ID PK
        TEXT NAME
        INTEGER LOCATION_ID FK
        INTEGER ACTION_YES_ID FK
        INTEGER ACTION_NO_ID FK
        INTEGER DALI_ADDR
        INTEGER DALI_INST
        INTEGER LOCATION_NUM
        INTEGER ACTION_YES_NUM
        INTEGER ACTION_NO_NUM
        INTEGER DELAY
    }

    BRIGHT_SENSORS {
        INTEGER ID PK
        TEXT NAME
        INTEGER LOCATION_ID FK
        INTEGER GROUP_ID FK
        INTEGER DALI_ADDR
        INTEGER DALI_INST
        INTEGER LOCATION_NUM
        INTEGER GROUP_NUM
    }

    BUTTON_PANELS {
        INTEGER ID PK
        TEXT NAME
        INTEGER LOCATION_ID FK
        INTEGER DALI_ADDR
        INTEGER LOCATION_NUM
    }

    BUTTONS {
        INTEGER ID PK
        TEXT NAME
        INTEGER BUTTON_PANEL_ID FK
        INTEGER ACTION_SET_SHORT_ID FK
        INTEGER ACTION_LONG_ID FK
        INTEGER DALI_ADDR
        INTEGER DALI_INST
        INTEGER ACTION_SET_SHORT_NUM
        INTEGER ACTION_LONG_NUM
    }

    ACTION_SETS {
        INTEGER ID PK
        INTEGER NUM
    }

    ACTIONS {
        INTEGER ID PK
        INTEGER ACTION_SET_ID FK
        INTEGER ACTION_SET_NUM
        INTEGER NUM
        INTEGER POS
    }

    SUBACTIONS {
        INTEGER ID PK
        INTEGER ACTION_ID FK
        INTEGER ACTION_NUM
        INTEGER OBJECT_NUM
        INTEGER VALUE
    }

    EVENTS {
        INTEGER ID PK
        INTEGER ACTION_ID FK
        TEXT DAYS
        TEXT TIME
        INTEGER SMOOTH
        INTEGER ACTION_NUM
    }
```

