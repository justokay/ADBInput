// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.KeyEvent
import data.events
import java.io.BufferedReader
import java.io.InputStreamReader


@Composable
@Preview
fun App() {
    MaterialTheme {
        EventList { event ->
            executeCommand(event)
        }
    }
}

fun executeCommand(event: KeyEvent) {
    val builder = ProcessBuilder(
        "adb", "devices"
    )
    builder.redirectErrorStream(true)
    val p = builder.start()
    val r = BufferedReader(InputStreamReader(p.inputStream))
    var line: String?
    while (true) {
        line = r.readLine()
        if (line == null) {
            break
        }
        println(line)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventList(onClick: (KeyEvent) -> Unit) {
    val allEvents by remember { mutableStateOf(events) }
    var filteredList by remember { mutableStateOf(allEvents) }

    var fieldValue by remember { mutableStateOf(filteredList.firstOrNull()?.name ?: "") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.width(200.dp)
        ) {
            TextField(
                fieldValue,
                onValueChange = { value ->
                    filteredList = if (value.isEmpty()) {
                        allEvents
                    } else {
                        allEvents.filter {
                            val lowercase = value.lowercase()
                            lowercase in it.name.lowercase() || lowercase in it.code.toString()
                        }
                    }
                    fieldValue = value
                },
                maxLines = 1
            )

            LazyColumn(
                modifier = Modifier.padding(top = 20.dp).fillMaxSize(),
            ) {
                items(filteredList, key = { item -> item.code }) { event ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                onClick(event)
                            }
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                            .animateItemPlacement()
                    ) {
                        Text(
                            "${event.name} (${event.code})",
                            style = TextStyle(color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.W500),
                        )
                        Divider(color = Color.Black, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
