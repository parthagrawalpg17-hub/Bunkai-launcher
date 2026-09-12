package com.bunkai.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class AppItem(val label: String, val packageName: String, val icon: Drawable?)
enum class Screen { HOME, DRAWER, BUNKAI_RUNNING, AI_PANEL }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF07090E)) {
                    BunkaiAppCore()
                }
            }
        }
    }
}

@Composable
fun BunkaiAppCore() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var userName by remember { mutableStateOf("Parth") }
    
    val installedApps = remember {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        pm.queryIntentActivities(intent, 0).map {
            AppItem(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm))
        }.sortedBy { it.label.lowercase() }
    }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good Morning, $userName"
        in 12..16 -> "Good Afternoon, $userName"
        in 17..20 -> "Good Evening, $userName"
        else -> "Good Night, $userName"
    }

    val tasks = remember { mutableStateListOf("Mathematics — 20 questions", "Physics — Revision") }
    var taskInput by remember { mutableStateOf("") }
    var bunkaiTimer by remember { mutableIntStateOf(25 * 60) }

    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.BUNKAI_RUNNING) {
            while (bunkaiTimer > 0) {
                delay(1000L)
                bunkaiTimer--
            }
            currentScreen = Screen.HOME
        }
    }

    when (currentScreen) {
        Screen.HOME -> {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Text(greeting.uppercase(), color = Color(0xFF8A99AD), style = MaterialTheme.typography.labelMedium)
                Text(
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    SimpleDateFormat("EEEE • d MMMM", Locale.getDefault()).format(Date()),
                    color = Color(0xFF8A99AD),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { currentScreen = Screen.BUNKAI_RUNNING },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00FFE0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚡ BUNKAI MODE", color = Color.Black, style = MaterialTheme.typography.labelSmall)
                        Text("Start Focus Session (25 Min)", color = Color.Black, style = MaterialTheme.typography.titleLarge)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { currentScreen = Screen.AI_PANEL },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181E2B))
                ) {
                    Text("🤖 AI STUDY HELP — Ask Anything", color = Color.White, modifier = Modifier.padding(14.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("TODAY'S TASKS", color = Color(0xFF8A99AD), style = MaterialTheme.typography.labelSmall)
                
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    OutlinedTextField(
                        value = taskInput,
                        onValueChange = { taskInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Add task for Parth...") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (taskInput.isNotBlank()) {
                            tasks.add(taskInput)
                            taskInput = ""
                        }
                    }) { Text("+") }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(tasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F131C))
                        ) {
                            Text(task, color = Color.White, modifier = Modifier.padding(12.dp))
                        }
                    }
                }

                Button(
                    onClick = { currentScreen = Screen.DRAWER },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181E2B))
                ) {
                    Text("▲ APP DRAWER", color = Color.White)
                }
            }
        }

        Screen.DRAWER -> {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Button(onClick = { currentScreen = Screen.HOME }) { Text("← Back Home") }
                Spacer(modifier = Modifier.height(16.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(4), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(installedApps) { app ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                if (launchIntent != null) context.startActivity(launchIntent)
                            }
                        ) {
                            app.icon?.let {
                                Image(bitmap = it.toBitmap().asImageBitmap(), contentDescription = app.label, modifier = Modifier.size(48.dp))
                            }
                            Text(app.label, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        Screen.BUNKAI_RUNNING -> {
            Column(
                modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("BUNKAI PROTOCOL ACTIVATED", color = Color(0xFF00FFE0), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    String.format("%02d:%02d", bunkaiTimer / 60, bunkaiTimer % 60),
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("$userName — LOCK IN.", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(48.dp))
                TextButton(onClick = { currentScreen = Screen.HOME }) {
                    Text("EMERGENCY ESCAPE", color = Color(0xFFFF2A55))
                }
            }
        }

        Screen.AI_PANEL -> {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text("🤖 AI STUDY HELP", color = Color(0xFF00FFE0), style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Hello Parth! Ready to optimize your tasks today?", color = Color.White)
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { currentScreen = Screen.HOME }, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to Dashboard")
                }
            }
        }
    }
}

