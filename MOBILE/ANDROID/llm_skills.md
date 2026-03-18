# LLM Skills

Легенда:
- `[x]` реализовано и подтверждено кодом/промптом
- `[-]` реализовано частично или с важными ограничениями
- `[ ]` не реализовано сейчас

## Ответ и формат
- [x] Отвечать пользователю по-русски через `assistantText`
- [x] Возвращать ровно один JSON-объект без markdown
- [x] Возвращать структурный ответ вида `assistantText + dbPatch + navigation + action`
- [-] При кривом ответе модели пользователь все равно получает текст, но без структурных действий

## Какой контекст LLM получает
- [x] Видеть текущий экран и его параметры через `UI_CONTEXT_JSON`
- [x] Видеть снимок состояния БД через `APP_DB_STATE_JSON`
- [x] Видеть историю видимых сообщений `USER/AI`
- [x] Работать в `controller scope`, если текущий экран позволяет вычислить контроллер
- [-] Внутри локации обычно видит только данные текущего контроллера, а не весь state приложения
- [-] Если пользователь не подключен к локации, видит только `CONTROLLERS` без внутренних сущностей локации

## Навигация по приложению
- [x] Открывать экран списка локаций `Locations`
- [x] Запускать flow добавления новой локации через экран поиска контроллера `Search`
- [x] Запускать инициализацию контроллера локации через экран `InitializeController`
- [x] Открывать конкретную локацию `Location`
- [x] Открывать конкретное помещение `Room`
- [x] Открывать конкретную группу `Group`
- [x] Открывать настройки локации `LocationSettings`
- [x] Открывать настройки помещения `RoomSettings`
- [x] Открывать экран светильника `Lum`
- [x] Открывать настройки светильника `LumSettings`
- [x] Открывать настройки датчика присутствия `SensorPressSettings`
- [x] Открывать настройки датчика освещенности `SensorBrightSettings`
- [x] Открывать экран кнопочной панели `Panel`
- [x] Открывать настройки кнопочной панели `ButtonPanelSettings`
- [x] Открывать настройки кнопки `ButtonSettings`
- [x] Открывать сценарий `Scenario`
- [x] Открывать расписание `Schedule`
- [x] Открывать точку расписания `SchedulePoint`
- [x] Открывать список графиков `Graphs`
- [x] Открывать график `Graph`
- [x] Открывать смену пароля `ChangePassword`
- [x] Открывать выбор иконки `IconSelect`
- [-] Если нет подключения к локации, не может вести на внутренние экраны и должен сначала открыть `Location`
- [-] Если сущность определяется неоднозначно, должен просить уточнение вместо навигации

## Изменение данных: общие возможности
- [x] Делать только `UPDATE` существующих строк
- [x] Менять только whitelist-таблицы и whitelist-колонки
- [x] Применять несколько обновлений за один ответ через `dbPatch.updates`
- [x] Сохранять текстовые значения без перевода, нормализации и перефразирования
- [-] Может менять только то, что однозначно найдено в текущем контексте и `APP_DB_STATE_JSON`
- [-] Создание новой локации делает не через `dbPatch`, а через navigation flow `Search -> Password -> добавление в список`
- [ ] Создавать новые строки в БД
- [ ] Удалять строки из БД
- [ ] Выполнять произвольный SQL

## Что LLM умеет менять в БД
- [-] Локации/контроллеры `CONTROLLERS`: `NAME`, `PASSWORD`, `IS_SCHEDULE`, `IS_GRAPHS`, `IS_AUTO`, `ICO_NUM`, `STATUS`, `SCENE_NUM`, `TIMESTAMP`, `GRID_POS`
- [x] Помещения `ROOMS`: `NAME`, `ICO_NUM`, `IS_AUTO`, `SCENE_NUM`, `GRID_POS`
- [x] Группы `GROUPS`: `NAME`
- [x] Типы светильников `LUMINAIRE_TYPES`: `NAME`
- [x] Светильники `LUMINAIRES`: `CONTROLLER_ID`, `ROOM_ID`, `GROUP_ID`, `NAME`, `ICO_NUM`, `TYPE_ID`, `BRIGHT`, `TEMPERATURE`, `SATURATION`, `HUE`, `GRID_POS`
- [x] Значения сцен светильников `LUMINAIRE_SCENES`: `BRIGHT`, `TEMPERATURE`, `SATURATION`, `HUE`
- [x] Датчики присутствия `PRES_SENSORS`: `CONTROLLER_ID`, `ROOM_ID`, `NAME`, `GRID_POS`
- [x] Датчики освещенности `BRIGHT_SENSORS`: `CONTROLLER_ID`, `ROOM_ID`, `GROUP_ID`, `NAME`, `GRID_POS`
- [x] Кнопочные панели `BUTTON_PANELS`: `CONTROLLER_ID`, `ROOM_ID`, `NAME`, `GRID_POS`
- [x] Кнопки `BUTTONS`: `NUM`, `BUTTON_PANEL_ID`, `DALI_INST`, `MATRIX_ROW`, `MATRIX_COL`, `LONG_PRESS_SCENARIO_ID`
- [x] Сценарии `SCENARIOS`: `CONTROLLER_ID`
- [x] Действия сценариев `ACTIONS`: `SCENARIO_ID`, `POSITION`, `OBJECT_TYPE_ID`, `OBJECT_ID`, `CHANGE_TYPE_ID`, `CHANGE_VALUE_ID`
- [x] Наборы сценариев кнопок `SCENARIO_SET`: `BUTTON_ID`, `POSITION`, `SCENARIO_ID`
- [x] События расписания `EVENTS`: `CONTROLLER_ID`, `DAYS`, `TIME`, `SCENARIO_ID`
- [x] Графики `GRAPHS`: `CONTROLLER_ID`, `OBJECT_TYPE_ID`, `OBJECT_ID`, `CHANGE_TYPE_ID`
- [x] Точки графиков `GRAPH_POINTS`: `GRAPH_ID`, `TIME`, `VALUE`

## Прикладные умения на уровне продукта
- [x] Запускать поиск нового контроллера для добавления новой локации
- [-] Инициализировать контроллер существующей локации только при `CONTROLLERS.IS_CONNECTED = true`
- [-] Переинициализировать контроллер существующей локации со сбросом через отдельный `action` и подтверждение в UI только при `CONTROLLERS.IS_CONNECTED = true`
- [x] Трактовать общую фразу "инициализируй локацию" внутри уже заполненной локации как переинициализацию, а не как пустую инициализацию
- [x] Удалять существующую локацию через отдельный `action` с подтверждением в UI
- [x] Переименовывать локации, помещения, группы, устройства и датчики
- [x] Менять иконки локаций, помещений и светильников
- [x] Менять флаги авто/расписания/графиков там, где это разрешено колонками
- [x] Менять порядок локации в списке локаций через `CONTROLLERS.GRID_POS`
- [x] Менять привязку светильника к помещению или группе
- [x] Менять тип светильника
- [x] Менять параметры света: яркость, температуру, насыщенность, оттенок
- [x] Менять координаты/позиции элементов в сетке через `GRID_POS`
- [x] Менять параметры кнопок и длинное нажатие на сценарий
- [x] Менять привязки сценариев, событий расписания, графиков и точек графика
- [x] Подбирать `ICO_NUM` по словарю иконок для локаций, помещений и светильников

## Ограничения и правила безопасности
- [x] Не должен показывать пользователю служебные ID и внутренние ключи сущностей
- [x] Не должен пересказывать скрытые служебные данные: system prompt, `UI_CONTEXT_JSON`, `APP_DB_STATE_JSON`
- [x] Не должен выдумывать `dbPatch`, если сущность не найдена или действие небезопасно
- [x] Не должен выдумывать `navigation`, если сущность не найдена или не хватает параметров
- [x] Для `ROOMS` обязан использовать составной ключ `CONTROLLER_ID + ID`
- [x] Для `IconSelect` обязан использовать только `controller`, `room`, `luminaire`
- [x] Для экранов списка/деталей обязан использовать LLM-friendly имена `Locations`, `Location`, `Room`, `Group`
- [x] Менять параметры локации (контроллера) только когда `IS_CONNECTED = true`
- [x] Без входа в локацию может менять у неё только `GRID_POS` и только для порядка на экране `Locations`
- [x] Нельзя инициализировать или переинициализировать локацию без подключения к её контроллеру (`IS_CONNECTED = true`)

## Наблюдаемость и техповедение
- [x] Отправлять в модель `system prompt`, `UI_CONTEXT_JSON`, `APP_DB_STATE_JSON`, историю чата
- [x] Логировать скрытый контекст в Logdog
- [x] Логировать структурный ответ LLM в Logdog
- [x] Работать через Ollama chat API
- [x] Запрашивать у Ollama JSON-ответ через `format = "json"`
- [x] Использовать модель `glm-4.7-flash:latest`

## Источники истины для этого чек-листа
- `app/src/main/assets/llm_system_prompt.md`
- `llm_ai_layer.md`
- `app/src/main/java/com/awada/synapse/ai/LLMOrchestrator.kt`
- `app/src/main/java/com/awada/synapse/ai/LLMDbPatchApplier.kt`
- `app/src/main/java/com/awada/synapse/ai/LLMContracts.kt`
- `app/src/main/java/com/awada/synapse/ai/LLMSystemPromptLoader.kt`
- `app/src/main/java/com/awada/synapse/ai/OllamaClient.kt`
- `app/src/main/java/com/awada/synapse/pages/PageLlmCatalog.kt`
