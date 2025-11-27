package com.awada.synapse.bluetooth

/**
 * Интерфейс для работы с Bluetooth-соединением контроллера Synapse.
 * 
 * Реализация должна обеспечивать:
 * - Поиск контроллеров Synapse по имени "SYNAPSE XXXXXXXX"
 * - Подключение/отключение от контроллера
 * - Передачу телеграмм USML между приложением и контроллером
 * - Обработку потери связи и автоматическое переподключение
 */
interface BluetoothModule {

    /**
     * Состояние Bluetooth-соединения
     */
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING
    }

    /**
     * Информация о найденном контроллере
     */
    data class ControllerInfo(
        val id: String,              // Уникальный идентификатор Bluetooth-устройства
        val name: String,            // Имя контроллера (например, "SYNAPSE 12345678" или "Этаж 2")
        val iconNumber: Int,         // Номер иконки (0 = дефолтная)
        val isConnected: Boolean     // Подключён ли сейчас
    )

    /**
     * Callback для событий Bluetooth
     */
    interface BluetoothCallback {
        fun onControllerFound(controller: ControllerInfo)
        fun onConnectionStateChanged(state: ConnectionState)
        fun onMessageReceived(message: String)
        fun onError(error: BluetoothError)
    }

    /**
     * Типы ошибок Bluetooth
     */
    enum class BluetoothError {
        BLUETOOTH_DISABLED,
        PERMISSION_DENIED,
        CONNECTION_FAILED,
        CONNECTION_LOST,
        SEND_FAILED
    }

    /**
     * Начать поиск контроллеров Synapse
     */
    fun startScan()

    /**
     * Остановить поиск контроллеров
     */
    fun stopScan()

    /**
     * Подключиться к контроллеру
     * @param controllerId Уникальный идентификатор контроллера
     * @param password Пароль (4 цифры)
     */
    fun connect(controllerId: String, password: String)

    /**
     * Отключиться от контроллера
     */
    fun disconnect()

    /**
     * Отправить телеграмму USML контроллеру
     * @param message Телеграмма в формате USML
     */
    fun sendMessage(message: String)

    /**
     * Получить текущее состояние соединения
     */
    fun getConnectionState(): ConnectionState

    /**
     * Получить информацию о подключённом контроллере
     * @return null если не подключён
     */
    fun getConnectedController(): ControllerInfo?

    /**
     * Получить список найденных контроллеров
     */
    fun getDiscoveredControllers(): List<ControllerInfo>

    /**
     * Зарегистрировать callback для событий
     */
    fun registerCallback(callback: BluetoothCallback)

    /**
     * Удалить callback
     */
    fun unregisterCallback(callback: BluetoothCallback)
}

