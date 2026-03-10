package com.awada.synapse.pages

internal enum class DeviceType {
    Luminaire,
    ButtonPanel,
    PresSensor,
    BrightSensor,
}

internal data class DeviceKey(
    val type: DeviceType,
    val id: Long,
)

