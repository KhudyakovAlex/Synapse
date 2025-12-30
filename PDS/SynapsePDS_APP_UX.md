# Интерфейс пользователя — UX

АПК Синапс v1.0. ПО. Спецификации на разработку

**Последнее изменение:** 30.12.2025, 17:12 МСК

## 1. Термины и определения

1.1. **Graphene** — дизайн-система для разработки пользовательских интерфейсов приложений AWADA.

## 2. Основные принципы

2.1. Кнопка ИИ для перехода в ИИ-чат и записи аудио-запроса без перехода в чат всегда должна быть на экране в самом нажимабельном месте.

2.2. Чем важнее контроль, тем ближе он должен быть к нижнему краю экрана.

## 3. Прототип интерфейса

```UXL
400x700

P\splash\Splash-окно\GOTOAFTER:2\Ну куда же без него... Пока грузится приложуха
  I\SRC:assets/img/splash_logo.png\300x300\FIT

P\controllers\Локации\Список контроллеров (локаций), которые на связи, и те, к которым когда-то подключались\IN:LT

  # Навигация
  F\OUT:T\IN:H\100%x48
    C\Локации\FONT:24\x48\OUT:LT:M10\IN:NW\#333333
    I\ICON:sliders:36\48x48\OUT:M10:RT\#111111\GOTO:settings\Настройки контроллеров

  # Основной контент
  F\100%x100%
    F\IN:H
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж1\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:box:100\GOTO:controller_pass\100x100\OUT:T\Контроллер на связи
        C\Этаж2\OUT:T:M0\IN:M0
    F\IN:H
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж3\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T\Контроллер не на связи
        C\Этаж4\OUT:T:M0\IN:M0\#AAAAAA
  
  # Панель ИИ
  F\OUT:B\100%x48\#333333
    I\ICON:microphone:36\48x48\OUT:M10\GOTO:llm\#FFFFFF\Кнопка вызова ИИ-джина
    S\1\@GOTO:controllers_nollm\AI\80x28\OUT:L:M10\Включение ИИ
    S\1\ICON:volume-2:24\ICON:volume-x:24\80x28\OUT:R:M10\Включение/отключение озвучки ИИ


P\controllers_nollm\Локации (ИИ откл)\IN:LT

  # Навигация
  F\OUT:T\IN:H\100%x48
    C\Локации\FONT:24\x48\OUT:LT:M10\IN:NW\#333333
    I\ICON:sliders:36\48x48\OUT:M10:RT\#111111

  # Основной контент
  F\100%x100%
    F\IN:H
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж1\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:box:100\100x100\OUT:T\
        C\Этаж2\OUT:T:M0\IN:M0
    F\IN:H
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж3\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T\
        C\Этаж4\OUT:T:M0\IN:M0\#AAAAAA
  
  # Панель ИИ
  F\OUT:B\100%x48\#555555
    I\ICON:microphone:24\48x48\OUT:M10\#AAAAAA\
    S\0\\AI@GOTO:controllers\80x28\OUT:L:M10\
    F\80x28\OUT:R:M10


P\settings\Настройки\Настройки приложения

  F\OUT:T\IN:H\100%x48
    I\ICON:back:36\48x48\OUT:L:M10\#111111\GOTO:controllers\Назад к списку
    C\Настройки\FONT:24\x48\OUT:L\IN:NW\#333333

  # Основной контент
  F\OUT:T\100%x
    S\0\ICON:grid3x3:24\ICON:grid4x4:24\140x40\OUT:L\Варианты сетки контроллеров (ниже которая)
    S\0\Светлая\Темная\OUT:R\140x40
  
  F\OUT:T\IN:H\100%x48
    C\Локации\FONT:24\x48\OUT:LT:M10\IN:NW\#333333
    I\ICON:plus:36\48x48\OUT:M10:RT\GOTO:add_controller\#111111  
  
  F\100%x100%
    F\IN:H
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж1\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:box:100\100x100\OUT:T
        C\Этаж2\OUT:T:M0\IN:M0
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA  
    F\IN:H
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж3\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж4\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA

    F\IN:H
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA

  # Панель ИИ
  F\OUT:B\100%x48\#333333
    I\ICON:microphone:36\48x48\OUT:M10\GOTO:llm\#FFFFFF\Кнопка вызова ИИ-джина
    S\1\\AI\80x28\OUT:L:M10\Включение ИИ
    S\1\ICON:volume-2:24\ICON:volume-x:24\80x28\OUT:R:M10\Включение/отключение озвучки ИИ

P\delete_controller\Удаление локации\TYPE:G\По долгому нажатию на иконку локации

  F\OUT:T\IN:H\100%x48
    I\ICON:back:36\48x48\OUT:L:M10\#111111
    C\Настройки\FONT:24\x48\OUT:L\IN:NW\#333333

  # Основной контент
  F\OUT:T\100%x
    S\0\ICON:grid3x3:24\ICON:grid4x4:24\140x40\OUT:L
    S\0\Светлая\Темная\OUT:R\140x40
  
  F\OUT:T\IN:H\100%x48
    C\Локации\FONT:24\x48\OUT:LT:M10\IN:NW\#333333
    I\ICON:plus:36\48x48\OUT:M10:RT\#111111  
  
  F\100%x100%
    F\IN:H\OUT:M20\#FFFFFF\100%x\R8

      C\Этаж4\FONT:24\x48\OUT:LT:M10\IN:NW\#333333
      C\Удалить\FONT:24\x48\OUT:RT:M10\IN:NW\#333333

    F\IN:H
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж3\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:box:100\100x100\#AAAAAA\OUT:T
        C\Этаж4\OUT:T:M0\IN:M0\#AAAAAA
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA

    F\IN:H
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA
      F\100x130\
        I\ICON:grid1x1:100\\100x100\OUT:T\#AAAAAA

  # Панель ИИ
  F\OUT:B\100%x48\#333333
    I\ICON:microphone:36\48x48\OUT:M10\GOTO:llm\#FFFFFF
    S\1\\AI\80x28\OUT:L:M10
    S\1\ICON:volume-2:24\ICON:volume-x:24\80x28\OUT:R:M10

P\add_controller\Добавление локации\Поиск по блютусу новых контроллеров

  I\ICON:grid3x3-anim:200\200x200\#111111\GOTOBACK

P\controller_pass\Пароль локации\Страница ввода пароля контроллера
  F\OUT:LT\IN:H
    I\ICON:back:36\48x48\OUT:L:M10\#111111\GOTO:controllers\
    C\Этаж2\FONT:24\x48\OUT:L\#333333

  # Ввод пароля
  F\100%x100%\IN:V
    C\Введите пароль\OUT:M10
    F\IN:H\OUT:M20
      F\30x30\#FFFFFF\OUT:M5
      F\30x30\#FFFFFF\OUT:M5
      F\30x30\#FFFFFF\OUT:M5
      F\30x30\#FFFFFF\OUT:M5
    
    # Клава
    F\IN:V
      F\IN:H
        B\1\60x40\OUT:M2\GOTO:controller
        B\2\60x40\OUT:M2\GOTO:controller
        B\3\60x40\OUT:M2\GOTO:controller
      F\IN:H
        B\4\60x40\OUT:M2\GOTO:controller
        B\5\60x40\OUT:M2\GOTO:controller
        B\6\60x40\OUT:M2\GOTO:controller
      F\IN:H
        B\7\60x40\OUT:M2\GOTO:controller
        B\8\60x40\OUT:M2\GOTO:controller
        B\9\60x40\OUT:M2\GOTO:controller
      F\IN:H
        B\0\60x40\OUT:M2\GOTO:controller
        B\ICON:delete\60x40\OUT:M2

  # Панель ИИ
  F\OUT:B\100%x48\#333333
    I\ICON:microphone:36\48x48\OUT:M10\GOTO:llm\#FFFFFF\
    S\1\\AI\80x28\OUT:L:M10\
    S\1\ICON:volume-2:24\ICON:volume-x:24\80x28\OUT:R:M10\

P\controller\Локация\Разблокированный контроллер
  F\OUT:LT\IN:H
    I\ICON:back:36\48x48\OUT:L:M10\#111111\GOTO:controller_pass\Назад к списку
    C\Этаж2\FONT:24\x48\OUT:L\#333333

  # Панель ИИ
  F\OUT:B\100%x48\#333333
    I\ICON:microphone:36\48x48\OUT:M10\GOTO:llm\#FFFFFF\Кнопка вызова ИИ-джина
    S\1\\AI\80x28\OUT:L:M10\Включение ИИ
    S\1\ICON:volume-2:24\ICON:volume-x:24\80x28\OUT:R:M10\Включение/отключение озвучки ИИ
P\llm\Чат с LLM\TYPE:G\Страница общения с ИИ-джином. Вернее, не страница, а выдвигающаяся поверх всего остального интерфейса панель с чатом ИИ.
  F\OUT:LT\IN:H
    I\ICON:back:36\48x48\OUT:L:M10\#111111\GOTOBACK
    C\Любая_страница\FONT:24\x48\OUT:L\#333333

  # Основной контент
  F\100%x100%
    C\Контент текущей страницы кнопочного интерфейса\FONT:16\#333333\Страница, поверх которой открывается ИИ-панель. Может прокручиваться в пределах области экрана, которая ей остаётся. Прокручивается вместе с навигационной шапкой. При любом клике вне панели ИИ (кроме прокручивания) панель ИИ сворачивается вниз и мы возвращаемся к исходному виду страницы.

  # Панель чата ИИ
  F\OUT:B\100%x200\#333333

    F\IN:LT:M4\OUT:M8\100%x100%\#999999\Переписка. Вся панель ИИ открывается только на столько, сколько необходимо для вывода в этом поле по одному сообщению ИИ и пользователя. Но можно выдвинуть панель на все окно (вернее не всё: шапка с заголовком страницы кнопочного интерфейса должна остаться)
      C\Вы: Привет
      C\ИИ: Чё надо? Опять сломал всё?

    F\OUT:B:M8\IN:H:L\100%x
      B\ Да \x36
      B\ Нет \x36
      B\ Не знаю \x36\Быстрые ответы/вопросы исходя из контекста

    F\OUT:B:M8\IN:H\100%x
      F\100%x36\#777777
      B\Отпр\72x36\Отправка сообщения LLMке

  # Панель ИИ
  F\OUT:B\100%x48\#333333
    I\ICON:microphone:36\48x48\OUT:M10\#FFFFFF
    S\1\\AI\80x28\OUT:L:M10
    S\1\ICON:volume-2:24\ICON:volume-x:24\80x28\OUT:R:M10

```