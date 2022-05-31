package data

sealed class Devices {
    object Empty : Devices()

    data class Connected(val list: List<String>) : Devices()

    data class Error(val message: String) : Devices()
}

data class DeviceID(val id: String)