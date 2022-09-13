package utils

import data.DPadInput
import data.KeyEvent
import data.events

fun DPadInput.toCommand(): KeyEvent {
    return when (this) {
        DPadInput.UP -> "KEYCODE_DPAD_UP"
        DPadInput.DOWN -> "KEYCODE_DPAD_DOWN"
        DPadInput.LEFT -> "KEYCODE_DPAD_LEFT"
        DPadInput.RIGHT -> "KEYCODE_DPAD_RIGHT"
        DPadInput.CENTER -> "KEYCODE_DPAD_CENTER"
        DPadInput.BACK -> "KEYCODE_BACK"
    }.let { name -> events.first { it.name == name } }
}