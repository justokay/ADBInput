// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.DeviceID
import data.Devices
import data.KeyEvent
import data.events
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader


@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                var currentDeviceID by remember { mutableStateOf("") }

                EventList { event ->
                    ADB.executeCommand(event, currentDeviceID)
                }
                AdbDevices {
                    currentDeviceID = it
                }
            }
        }
    }
}

@Composable
fun AdbDevices(onDeviceSelected: (String) -> Unit) {
    val coroutines = rememberCoroutineScope()
    var devices by remember { mutableStateOf<Devices>(Devices.Empty) }

    DisposableEffect(Unit) {
        coroutines.launch {
            while (true) {
                delay(1000)
                devices = ADB.getDevices()
            }
        }

        onDispose {

        }
    }

    when (devices) {
        is Devices.Connected -> {
            DeviceSelector((devices as Devices.Connected).list, onDeviceSelected)
        }
        Devices.Empty -> Text("There is no any adb connected devices")
        is Devices.Error -> Text((devices as Devices.Error).message)
    }
}
@Composable
fun DeviceSelector(devices: List<String>, onDeviceSelected: (String) -> Unit) {
    var currentDevice by remember { mutableStateOf(devices.firstOrNull() ?: "There is no connected devices") }
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier.clickable {
                expanded = true
            }
        ) {
            Text(currentDevice)
            if (devices.size > 1) {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        }
        if (devices.size > 1) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                devices.forEach { selectionOption ->
                    DropdownMenuItem(
                        onClick = {
                            currentDevice = selectionOption
                            expanded = false
                            onDeviceSelected(currentDevice)
                        }
                    ) {
                        Text(text = selectionOption)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventList(onClick: (KeyEvent) -> Unit) {
    val allEvents by remember { mutableStateOf(events) }
    var filteredList by remember { mutableStateOf(allEvents) }

    var fieldValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.width(240.dp)
    ) {
        TextField(
            fieldValue,
            placeholder = {
                Text("filter")
            },
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
            modifier = Modifier.fillMaxSize(),
        ) {
            items(filteredList, key = { item -> item.code }) { event ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onClick(event)
                        }
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .animateItemPlacement()
                ) {
                    Text(
                        event.name,
                        style = TextStyle(color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.W500),
                    )
                    Text(
                        event.code.toString(),
                        style = TextStyle(
                            color = Color(0xff2c2c2c),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.W400
                        ),
                    )
                }
                Divider(color = Color.Black, thickness = 1.dp)
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
