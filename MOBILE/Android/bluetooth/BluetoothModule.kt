package com.synapse.mobile.android.bluetooth

/**
 * Результат подключения к контроллеру
 */
enum class ConnectResult {
    SUCCESS,           // 0 - подключились, всё ОК
    WRONG_PIN,         // 1 - неверный пин-код
    UNKNOWN_ERROR      // 2 - неизвестная ошибка
}

/**
 * Результат отправки телеграммы
 */
enum class SendResult {
    SUCCESS,           // отправлено успешно
    NOT_CONNECTED,     // нет соединения
    ERROR              // ошибка отправки
}

/**
 * Контроллер Synapse
 */
interface SynapseController {
    
    /** Имя контроллера ("SYNAPSE XXXXXXXX" или пользовательское) */
    val name: String
    
    /** Уникальный идентификатор (MAC-адрес) */
    val id: String
    
    /** Номер иконки из библиотеки */
    val iconNumber: Int
    
    /**
     * Подключиться к контроллеру
     * @param pinCode пин-код (4 цифры)
     * @return результат подключения
     */
    fun connect(pinCode: String): ConnectResult
    
    /**
     * Отключиться от контроллера
     */
    fun disconnect()
    
    /**
     * Отправить телеграмму контроллеру
     * @param telegram USML-телеграмма
     * @return результат отправки
     */
    fun sendTelegram(telegram: String): SendResult
    
    /**
     * Установить обработчик входящих телеграмм
     * @param callback функция, вызываемая при получении телеграммы (null — отписаться)
     */
    fun setOnTelegramReceived(callback: ((telegram: String) -> Unit)?)
    
    /**
     * Установить обработчик потери связи
     * @param callback функция, вызываемая при разрыве соединения (null — отписаться)
     */
    fun setOnConnectionLost(callback: (() -> Unit)?)
}

/**
 * Список контроллеров Synapse
 */
interface SynapseControllerList {
    
    /**
     * Сканировать доступные контроллеры
     * Блокирующий вызов, возвращает список после завершения сканирования
     * @param timeoutMs таймаут сканирования в миллисекундах (по умолчанию 5 секунд)
     * @return список найденных контроллеров
     */
    fun scan(timeoutMs: Long = 5000): List<SynapseController>
}

