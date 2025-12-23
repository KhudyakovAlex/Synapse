# Интерфейс пользователя — UX

АПК Синапс v1.0. ПО. Спецификации на разработку

**Последнее изменение:** 23.12.2025, 19:05 МСК

## 1. Термины и определения

1.1. **Graphene** — дизайн-система для разработки пользовательских интерфейсов приложений AWADA.

## 2. Основные принципы

2.1. Кнопка ИИ для перехода в ИИ-чат и записи аудио-запроса без перехода в чат всегда должна быть на экране в самом нажимабельном месте.

2.2. Чем важнее контроль, тем ближе он должен быть к нижнему краю экрана.

## 3. Прототип интерфейса

```UXL
400x700

P\splash\Splash-окно\GOTOAFTER:2\Ну куда же без него... Пока грузится приложуха
  I\SRC:"D:\Git\Synapse\INDEX\assets\img\splash_logo.png"\300x300\FIT

P\controllers\Контроллеры\Список контроллеров, которые на связи, и те, к которым когда-то подключались\IN:LT

  # Навигация
  F\OUT:T\IN:RT\100%x48
    I\ICON:sliders:36\48x48\OUT:M10\#111111\GOTO:settings\Настройки контроллеров

  # Основной контент
  F\100%x100%\IN:V
    F\
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж1\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:box:100\GOTO:controller\100x100\OUT:T\Контроллер на связи
        C\Этаж2\OUT:T:M0\IN:M0
    F\
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж3\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T\Контроллер не на связи
        C\Этаж4\OUT:T:M0\IN:M0\#AAAAAA
  
  # Панель ИИ
  F\OUT:B\100%x48\#333333
    I\ICON:ai:36\48x48\OUT:M10\GOTO:llm\#FFFFFF\Кнопка вызова ИИ-джина
    S\1\\\80x28\OUT:L:M10\Включение ИИ
    S\1\ICON:volume-2:24\ICON:volume-x:24\80x28\OUT:R:M10\Включение/отключение озвучки ИИ


P\settings\Настройки\Настройки приложения
  F\OUT:LT
    I\ICON:back:36\48x48\OUT:L:M10\#111111\GOTO:controllers\Назад к списку
    C\Настройки\FONT:24\x48\OUT:L\#333333

  # Основной контент
  F\OUT:TL\IN:V
    F
      C\Тема
      S\0\Светлая\Темная\140x40
    F
      C\Сетка
      S\0\ICON:grid3x3:24\ICON:grid4x4:24\140x40\Варианты сетки контроллеров (ниже которая)
  
  F\OUT:TL\IN:H
    I\ICON:search:36\48x48\OUT:M10\#111111\Кнопка поиска новых контроллеров на связи
    I\ICON:trash:36\48x48\OUT:M10\#111111\Удаление контроллера (перетаскиваем на нее)

  F\100%x100%\IN:V
    F\
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж1\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:box:100\100x100\OUT:T
        C\Этаж2\OUT:T:M0\IN:M0
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA  
    F\
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж3\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж4\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA

    F\
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA

  # Панель ИИ
  F\OUT:B\100%x48\#333333
    I\ICON:ai:36\48x48\OUT:M10\GOTO:llm\#FFFFFF\Кнопка вызова ИИ-джина
    S\1\\\80x28\OUT:L:M10\Включение ИИ
    S\1\ICON:volume-2:24\ICON:volume-x:24\80x28\OUT:R:M10\Включение/отключение озвучки ИИ

P\controller\Контроллер\Страница конкретного контроллера
  F\OUT:LT
    I\ICON:back:36\48x48\OUT:L:M10\#111111\GOTO:controllers\Назад к списку
    C\Этаж2\FONT:24\x48\OUT:L\#333333

  # Панель ИИ
  F\OUT:B\100%x48\#333333
    I\ICON:ai:36\48x48\OUT:M10\GOTO:llm\#FFFFFF\Кнопка вызова ИИ-джина
    S\1\\\80x28\OUT:L:M10\Включение ИИ
    S\1\ICON:volume-2:24\ICON:volume-x:24\80x28\OUT:R:M10\Включение/отключение озвучки ИИ
P\llm\Чат с LLM\Страница общения с ИИ-джином
  F\OUT:LT
    I\ICON:back:36\48x48\OUT:L:M10\#111111\GOTOBACK\Назад отсюда

  # Основной контент
  F\OUT:TL\IN:V
    F\
      C\Чат с ИИ-джином\FONT:24\x48\OUT:L\#333333

  # Панель ИИ
  F\OUT:B\100%x48\#333333
    I\ICON:ai:36\48x48\OUT:M10\#FFFFFF\Кнопка вызова ИИ-джина
    S\1\\\80x28\OUT:L:M10\Включение ИИ
    S\1\ICON:volume-2:24\ICON:volume-x:24\80x28\OUT:R:M10\Включение/отключение озвучки ИИ

```


