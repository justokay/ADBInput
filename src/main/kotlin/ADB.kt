import data.Devices
import data.KeyEvent
import org.json.JSONObject
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

        executeCommand(command)
    }

    fun sendDeeplink(deeplink: String, appId: String, deviceId: String) {
        if (deeplink.isEmpty() || appId.isEmpty()) return

        val command = if (deviceId.isNotEmpty()) {
            listOf(
                "adb", "-s", deviceId, "shell", "am", "start", "-W", "-a", "android.intent.action.VIEW", "-d", "\"$deeplink\"", appId
            )
        } else {
            listOf(
                "adb", "shell", "am", "start", "-W", "-a", "android.intent.action.VIEW", "-d", "\"$deeplink\"", appId
            )
        }

        executeCommand(command)
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

    fun sendText(text: String, deviceId: String) {
        val command = if (deviceId.isNotEmpty()) {
            listOf(
                "adb", "-s", deviceId, "shell", "input", "text", text
            )
        } else {
            listOf(
                "adb", "shell", "input", "text", text
            )
        }

        executeCommand(command)
    }

    private fun executeCommand(command: List<String>) {
        println(command)

        val process = ProcessBuilder(command).apply {
            redirectErrorStream(true)
        }.start()

        InputStreamReader(process.inputStream).buffered().lines().forEach {
            println(">> $it")
        }
    }

    enum class Type(val value: String) {
        DYNAMIC("Dynamic"), RECOMMENDATION("Recommendation"), LIST("List")
    }

//    fun test() {
//        JSONObject().apply {
//
//        }
//    }
//
//    enum class Type(val value: String) {
//        DYNAMIC("Dynamic"), RECOMMENDATION("Recommendation"), LIST("List")
//    }
//
//    fun test() {
//        JSONObject().apply {
//
//        }
//    }

}