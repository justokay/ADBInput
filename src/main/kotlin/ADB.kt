import data.Devices
import data.KeyEvent
import java.io.InputStreamReader
import kotlin.streams.toList

object ADB {
    fun executeCommand(event: KeyEvent) {
        val builder = ProcessBuilder(
            "adb", "devices"
        )
        builder.redirectErrorStream(true)

        val p = builder.start()
        InputStreamReader(p.inputStream).buffered().lines().forEach {
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
                println(it)
                Devices.Connected(it)
            }
        } else Devices.Empty
    }

}