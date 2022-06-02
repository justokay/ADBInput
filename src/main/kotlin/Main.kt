// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import data.Devices
import data.KeyEvent
import data.events
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
                Box(
                    modifier = Modifier.fillMaxHeight().width(1.dp).background(color = Color.Black)
                )
                Content(
                    onDeviceSelected = {
                        currentDeviceID = it
                    },
                    onSendText = {
                        ADB.sendText(it, currentDeviceID)
                    }
                )
            }
        }
    }
}

@Composable
fun Content(onDeviceSelected: (String) -> Unit, onSendText: (String) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(10.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                DeviceSelector(onDeviceSelected)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                horizontalAlignment = Alignment.End
            ) {
                var text by remember { mutableStateOf("") }

                OutlinedTextField(
                    text,
                    onValueChange = { text = it },
                    modifier = Modifier.height(300.dp).fillMaxWidth()
                )
                Row {
                    Button(onClick = {
                        text = ""
                    }) {
                        Text("clear")
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(onClick = {
                        onSendText(text)
                    }) {
                        Text("send")
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceSelector(onDeviceSelected: (String) -> Unit) {
    val coroutines = rememberCoroutineScope()
    var devices by remember { mutableStateOf<Devices>(Devices.Empty) }

    LaunchedEffect(Unit) {
        coroutines.launch {
            while (true) {
                delay(1000)
                devices = ADB.getDevices()
            }
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
        OutlinedTextField(
            fieldValue,
            placeholder = {
                Text("filter code here", style = TextStyle(Color(0xffbbbbbb)))
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
                        .padding(horizontal = 5.dp, vertical = 5.dp)
                        .animateItemPlacement()
                ) {
                    Text(
                        event.name,
                        style = TextStyle(color = Color(0xff2c2c2c), fontSize = 14.sp, fontWeight = FontWeight.W500),
                    )
                    Text(
                        event.code.toString(),
                        style = TextStyle(
                            color = Color(0xffbbbbbb),
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

sealed class AppWindow {
    object Main : AppWindow()
    object Devices : AppWindow()
}

data class AppState(
    val windows: MutableList<AppWindow>
)

@Composable
fun rememberAppState() = remember {
    AppState(
        windows = mutableListOf(
            AppWindow.Main
        )
    )
}

fun main() = application {
    val appState = rememberAppState()

    appState.windows.forEach {
        when (it) {
            AppWindow.Devices -> EmulatorsWindos(appState)
            AppWindow.Main -> AppWindow(appState)
        }
    }
}

@Composable
fun ApplicationScope.AppWindow(appState: AppState) {
    val state = rememberWindowState(
        size = DpSize(600.dp, 600.dp)
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "Android Debug Bridge Input",
        icon = rememberVectorPainter(Icons.Default.Build),
        state = state
    ) {
        MenuBar {
            Menu("File") {
                Item("Emulators") {
                    appState.windows.add(
                        AppWindow.Devices
                    )
                }
            }
        }
        App()
    }
}

@Composable
fun EmulatorsWindos(appState: AppState) {
    val state = rememberWindowState(
        size = DpSize(400.dp, 400.dp)
    )
    Window(title = "Emuliators", state = state, onCloseRequest = {
        appState.windows.removeLast()
    }) {
        Box(modifier = Modifier.fillMaxWidth()) {

        }
    }
}
