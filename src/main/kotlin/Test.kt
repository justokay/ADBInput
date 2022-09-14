import org.json.JSONObject

enum class Type(val value: String) {
    DYNAMIC("Dynamic"), RECOMMENDATION("Recommendation"), LIST("List")
}

fun main() {
    println(
        JSONObject().apply {
            put("type", Type.DYNAMIC.value)
        }.toString()
    )
}