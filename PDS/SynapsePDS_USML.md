# Unit System Model Language

АПК Синапс v1.0. ПО. Спецификации на разработку

**Последнее изменение:** 09.12.2025, 15:56 МСК

## 1. Термины и определения

1.1. **USM** (Unit System Model) — виртуальная модель системы освещения.

1.2. **FW-USM** — USM в прошивке.  В виде массивов структур языка С. Описано в SynapsePDS_FW_DB.

1.3. **APP-USM** — USM в приложении. В виде реляционной базы в SQLite. Описано в SynapsePDS_APP_DB.

1.4. **USML** (Unit System Model Language) — система команд (телеграмм) для обмена данными между приложением и USM в прошивке.

1.5. **JSON/USML-конвертер** - конвртилка телеграмм из формата JSON в USML и взад.

1.6. **Телеграмма**, она же **телега** — название команды в USML, названная так, дабы отличать их от других команд.

1.7. **АПК** — аппаратно-программный комплекс Синапс.

## 2. Общие моменты

2.1. Передача данных в АПК:

```mermaid
flowchart LR
    subgraph Сервер
      LLM((LLM))
    end

    subgraph Телефон
      UI[Кнопочный UI]
      APPUSM[(APP-USM)]

      subgraph Конвертер["USML/JSON-конвертер"]
        C-IN{{USML/JSON}}
        C-OUT{{JSON/USML}}
      end
    end
  
    subgraph Контроллер
      FWUSM[(FW-USM)]
    end

    APPUSM e1@-.JSON.-> UI
    APPUSM e2@-.JSON.-> LLM
    LLM e3@-.JSON.-> C-OUT
    UI e4@-.JSON.-> C-OUT
    C-OUT e5@-.USML.-> FWUSM
    FWUSM e6@-.USML.-> C-IN
    C-IN e7@-.JSON.-> APPUSM

    style Сервер fill:#eeeeee,stroke:#eeeeee,color:#555555
    style Конвертер fill:#dddddd,stroke:#dddddd,color:#555555
    style Телефон fill:#f1f1f1,stroke:#f1f1f1,color:#555555
    style Контроллер fill:#eeeeee,stroke:#eeeeee,color:#555555
    style LLM fill:#ffcc99,stroke:#ff8800,color:#663300
    style UI fill:#b3ffb3,stroke:#00cc00,color:#006600
    style APPUSM fill:#ffcccc,stroke:#cc0000,color:#660000
    style FWUSM fill:#cce5ff,stroke:#0066cc,color:#003366
    style C-IN fill:#ffffff,stroke:#888888,color:#555555
    style C-OUT fill:#ffffff,stroke:#888888,color:#555555

    e1@{ animation: slow }
    e2@{ animation: slow }
    e3@{ animation: slow }
    e4@{ animation: slow }
    e5@{ animation: slow }
    e6@{ animation: slow }
    e7@{ animation: slow }
```

2.2. Экземпляр USM в приложении (APP-USM) — своего рода кэш USM контроллера (FW-USM). Так же как USM контроллера хранит состояние устройств DALI, чтобы за ним каждый раз не лазать в линию, так и USM в приложении нужен для оперативного получения состояния системы LLM'кой и UI.

2.3. Команды на изменение своего состояния FW-USM получает от LLM и UI (после их конвертации в JSON/USML-конвертер из JSON в USML).

2.4. Изменив своё состояние, FW-USM отправляет телеги об этом в APP-USM всех телефонов, которые в данный момент подключены к контроллеру.

## 3. Формат телеграмм

### 3.1. SET

Передача части USM в виде блока байтов. Обработчик этой телеги не только может менять состояние своей БД, но и выполнять другие действия. В частности FW-USM, получив телегу и изменив по данным в ней состояние своей БД, должен отправить соотв телегу SET в APP-USM дабы совершить акт экспликации.

`SET.TABLE[ID].FIELD(Content)`

где:  
  
- **TABLE[ID]** - ограничение Content по таблице:  
    - все таблицы - вся USM без ограничений: TABLE опускается (пример: `SET(..)`)  
    - вся таблица: TABLE[] (пример: `SET.LOCATION[](..)`)  
    - запись таблицы по PK ID: TABLE[ID] (пример: `SET.LOCATION[4](..)`)  

- **FIELD** - ограничение Content по полю записи:
    - вся запись: FIELD опускается (пример: `SET.LOCATION[4](..)`)  
    - поле записи: FIELD (пример: `SET.LOCATION[4].NAME(..)`)  

- **Content** - блок байтов; часть данных USM, ограничиваемая TABLE и FIELD;  
   
Т.о. в телеге может отсутствовать как TABLE так и FIELD, но если есть FIELD должна быть и TABLE. Более того, в последнем случае должна быть конкретизирована запись таблицы, передать значения какого-то поля всех записей таблицы нельзя.

Варианты / примеры:

1. `SET(уокр298498ап3цпп п3к укп кпцпу пкупцкпвам)` - вся USM
2. `SET.LOCATION[](пукп уа ум9п98  п9889 ап)` - вся таблица LOCATION
3. `SET.LOCATION[7](пукп уа ум9)` - запись таблицы LOCATION с ID = 7
4. `SET.LOCATION[7].NAME(Хрень)` - значение поля NAME в записи таблицы LOCATION с ID = 7

### 3.2. Формат блока Content

3.2.1. При формировании Content в него суются последовательно в порядке, указанном ниже, значения полей без разделителей непосредственно в их байтовом представлении.

3.2.2. Важен порядок, в котором в Content добавляются таблицы, записи, поля записей, и размер типов добавляемых значений. Чтобы кто-то правильно прочитал, кто-то должен сначала правильно записать!

3.2.3. Вот единственно верный порядок-структура (с указанием размера в байтах каждого поля):

**CONTROLLERS**
- **ID** (2)
- **NAME** (20)
- **PASSWORD** (4)
- **IS_SCHEDULE** (1)
- **IS_AUTO** (1)
- **ICO_NUM** (1)
- **STATUS** (1)
- **SCENE_NUM** (1)
- **IDATA** (50000)

**LOCATIONS**
- **ID** (2)
- **EXIST** (1)
- **IS_AUTO** (1)
- **SCENE_NUM** (1)

**GROUPS**
- **ID** (2)
- **EXIST** (1)
- **LOCATION_ID** (2)
- **SCENE_NUM** (1)

**LUMINAIRES**
- **ID** (2)
- **EXIST** (1)
- **DALI_ADDR** (1)
- **LOCATION_ID** (2)
- **GROUP_ID** (2)
- **VAL_BRIGHT** (1)
- **VAL_TW** (1)
- **VAL_R** (1)
- **VAL_G** (1)
- **VAL_B** (1)
- **VAL_W** (1)
- **SCENE_NUM** (1)
- **STATUS** (1)

**SCENE_LUMINAIRES**
- **ID** (2)
- **SCENE_NUM** (1)
- **LUMINAIRE_ID** (1)
- **VAL_BRIGHT** (1)
- **VAL_TW** (1)
- **VAL_R** (1)
- **VAL_G** (1)
- **VAL_B** (1)
- **VAL_W** (1)

**PRES_SENSORS**
- **ID** (2)
- **EXIST** (1)
- **DALI_ADDR** (1)
- **DALI_INST** (1)
- **LOCATION_ID** (2)
- **ACTION_OCCUPANCY_ID** (2)
- **ACTION_VACANCY_ID** (2)
- **DELAY** (1)
- **STATUS** (1)

**BRIGHT_SENSORS**
- **ID** (2)
- **EXIST** (1)
- **DALI_ADDR** (1)
- **DALI_INST** (1)
- **LOCATION_ID** (2)
- **GROUP_ID** (2)
- **STATUS** (1)

**BUTTON_PANELS**
- **ID** (2)
- **EXIST** (1)
- **DALI_ADDR** (1)
- **LOCATION_ID** (2)
- **STATUS** (1)

**BUTTONS**
- **ID** (2)
- **BUTTON_PANEL_ID** (2)
- **DALI_INST** (1)
- **ACTION_SET_SHORT_NUM** (2)
- **ACTION_LONG_ID** (2)

**ACTIONS**
- **ID** (2)
- **ACTION_SET_NUM** (2)
- **POS** (1)

**SUBACTIONS**
- **ID** (2)
- **ACTION_ID** (2)
- **OBJECT_TYPE** (1)
- **OBJECT_NUM** (1)
- **VALUE** (1)

**EVENTS**
- **ID** (2)
- **EXIST** (1)
- **DAYS** (7)
- **TIME** (4)
- **SMOOTH** (1)
- **ACTION_ID** (2)

### 3.3. JSON-представление структуры Content

```json
{
  "VERSION": 4,
  "CONTROLLERS": {
    "ID": 2,
    "NAME": 20,
    "PASSWORD": 4,
    "IS_SCHEDULE": 1,
    "IS_AUTO": 1,
    "ICO_NUM": 1,
    "STATUS": 1,
    "SCENE_NUM": 1,
    "IDATA": 50000
  },
  "LOCATIONS": {
    "ID": 2,
    "EXIST": 1,
    "IS_AUTO": 1,
    "SCENE_NUM": 1
  },
  "GROUPS": {
    "ID": 2,
    "EXIST": 1,
    "LOCATION_ID": 2,
    "SCENE_NUM": 1
  },
  "LUMINAIRES": {
    "ID": 2,
    "EXIST": 1,
    "DALI_ADDR": 1,
    "LOCATION_ID": 2,
    "GROUP_ID": 2,
    "VAL_BRIGHT": 1,
    "VAL_TW": 1,
    "VAL_R": 1,
    "VAL_G": 1,
    "VAL_B": 1,
    "VAL_W": 1,
    "SCENE_NUM": 1,
    "STATUS": 1
  },
  "SCENE_LUMINAIRES": {
    "ID": 2,
    "SCENE_NUM": 1,
    "LUMINAIRE_ID": 1,
    "VAL_BRIGHT": 1,
    "VAL_TW": 1,
    "VAL_R": 1,
    "VAL_G": 1,
    "VAL_B": 1,
    "VAL_W": 1
  },
  "PRES_SENSORS": {
    "ID": 2,
    "EXIST": 1,
    "DALI_ADDR": 1,
    "DALI_INST": 1,
    "LOCATION_ID": 2,
    "ACTION_OCCUPANCY_ID": 2,
    "ACTION_VACANCY_ID": 2,
    "DELAY": 1,
    "STATUS": 1
  },
  "BRIGHT_SENSORS": {
    "ID": 2,
    "EXIST": 1,
    "DALI_ADDR": 1,
    "DALI_INST": 1,
    "LOCATION_ID": 2,
    "GROUP_ID": 2,
    "STATUS": 1
  },
  "BUTTON_PANELS": {
    "ID": 2,
    "EXIST": 1,
    "DALI_ADDR": 1,
    "LOCATION_ID": 2,
    "STATUS": 1
  },
  "BUTTONS": {
    "ID": 2,
    "BUTTON_PANEL_ID": 2,
    "DALI_INST": 1,
    "ACTION_SET_SHORT_NUM": 2,
    "ACTION_LONG_ID": 2
  },
  "ACTIONS": {
    "ID": 2,
    "ACTION_SET_NUM": 2,
    "POS": 1
  },
  "SUBACTIONS": {
    "ID": 2,
    "ACTION_ID": 2,
    "OBJECT_TYPE": 1,
    "OBJECT_NUM": 1,
    "VALUE": 1
  },
  "EVENTS": {
    "ID": 2,
    "EXIST": 1,
    "DAYS": 7,
    "TIME": 4,
    "SMOOTH": 1,
    "ACTION_ID": 2
  }
}
```

## 4. Вопросы

## 5. Идеи
