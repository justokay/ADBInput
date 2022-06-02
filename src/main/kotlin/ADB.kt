import data.Devices
import data.KeyEvent
import java.io.InputStreamReader
import kotlin.streams.toList

object ADB {
    fun executeCommand(event: KeyEvent, deviceId: String) {
        val command = if (deviceId.isNotEmpty()) {
            listOf(
                "adb", "-s", deviceId, "shell", "input", "keyevent", event.name
            )
        } else {
            listOf(
                "adb", "shell", "input", "keyevent", event.name
            )
        }

        println(command)

        val process = ProcessBuilder(command).apply {
            redirectErrorStream(true)
        }.start()

        InputStreamReader(process.inputStream).buffered().lines().forEach {
            println(">> $it")
        }
    }

    fun getDevices(): Devices {
        val builder = ProcessBuilder(
            "adb", "devices"
        )
        builder.redirectErrorStream(true)

        val p = builder.start()
        val list = InputStreamReader(p.inputStream).buffered().lines().toList()
        return if (list.firstOrNull() == "List of devices attached") {
            list.subList(1, list.lastIndex).filter { it.trim().isNotEmpty() }.map {
                it.split("\t").first()
            }.let {
                Devices.Connected(it)
            }
        } else Devices.Empty
    }

    fun sendText(text: String, currentDeviceID: String) {
        TODO("Not yet implemented")
    }

}