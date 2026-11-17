package com.focusx.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusx.app.blocking.BlockRuleStore
import com.focusx.app.data.*
import com.focusx.app.timer.FocusTimerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFF0B1017)
private val SurfaceX = Color(0xFF151E29)
private val CardX = Color(0xFF1C2936)
private val Green = Color(0xFF39D9A3)
private val Muted = Color(0xFF9BAABD)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = FocusDatabase.get(this)
        setContent { FocusXTheme { FocusXApp(db) } }
    }
}

@Composable
private fun FocusXTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Green, secondary = Color(0xFF75A9FF),
            background = Bg, surface = SurfaceX,
            onBackground = Color.White, onSurface = Color.White
        ), content = content
    )
}

@Composable
private fun FocusXApp(db: FocusDatabase) {
    var tab by remember { mutableStateOf("Home") }
    var openModule by remember { mutableStateOf<String?>(null) }
    val sessions by db.dao().sessions().collectAsStateWithLifecycle(emptyList())
    val schedules by db.dao().schedules().collectAsStateWithLifecycle(emptyList())
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayMinutes = sessions.filter { it.completedAt >= todayStart }.sumOf { it.minutes }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = SurfaceX) {
                listOf(
                    Triple("Home", Icons.Default.Home, "Home"),
                    Triple("Block", Icons.Default.Shield, "Block"),
                    Triple("Focus", Icons.Default.Timer, "Focus"),
                    Triple("Stats", Icons.Default.BarChart, "Stats"),
                    Triple("More", Icons.Default.Tune, "More")
                ).forEach { (label, icon, value) ->
                    NavigationBarItem(
                        selected = tab == value && openModule == null,
                        onClick = { tab = value; openModule = null },
                        icon = { Icon(icon, null) }, label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (openModule != null) ModuleScreen(openModule!!, db) { openModule = null }
            else when (tab) {
                "Home" -> HomeScreen(todayMinutes, sessions.size) { openModule = it }
                "Block" -> ModuleScreen("App Blocking", db) {}
                "Focus" -> FocusTimerScreen(db)
                "Stats" -> StatsScreen(sessions, todayMinutes)
                else -> MoreScreen(db) { openModule = it }
            }
        }
    }
}

@Composable
private fun HomeScreen(today: Int, count: Int, open: (String) -> Unit) {
    val modules = listOf(
        "App Blocking" to Icons.Default.Shield,
        "Reels & Shorts" to Icons.Default.VideoLibrary,
        "Website Blocking" to Icons.Default.Language,
        "Focus Timer" to Icons.Default.Timer,
        "Music & Themes" to Icons.Default.Headphones,
        "Screen Time Analytics" to Icons.Default.BarChart,
        "Schedule Planner" to Icons.Default.CalendarMonth,
        "Multiplayer" to Icons.Default.Groups,
        "AI Companion" to Icons.Default.SmartToy,
        "Goals & Settings" to Icons.Default.Tune
    )
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item {
            Text("FOCUSX", color = Green, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Text("Stay focused.", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Take control of your attention.", color = Muted)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceX), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("TODAY'S FOCUS", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
                    Text("${today / 60}h ${today % 60}m", fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { (today / 120f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = Green, trackColor = CardX
                    )
                    Text("$count completed sessions • 2h daily goal", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { open("Focus Timer") }) { Icon(Icons.Default.PlayArrow, null); Text(" Start focus") }
                }
            }
        }
        item { Text("YOUR TOOLKIT", color = Muted, fontWeight = FontWeight.Bold) }
        items(modules.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (name, icon) ->
                    Card(
                        Modifier.weight(1f).height(116.dp).clickable { open(name) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceX),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Icon(icon, null, tint = Green)
                            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusTimerScreen(db: FocusDatabase) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("Study session") }
    var minutes by remember { mutableIntStateOf(25) }
    var remaining by remember { mutableIntStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }
    var endAt by remember { mutableLongStateOf(0L) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining = if (endAt > 0) ((endAt - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0) else remaining - 1
        }
        if (running && remaining <= 0 && !saved) {
            running = false
            saved = true
            context.stopService(Intent(context, FocusTimerService::class.java).setAction(FocusTimerService.ACTION_STOP))
            scope.launch { db.dao().addSession(FocusSession(title = title, minutes = minutes)) }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DEEP WORK", color = Green, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        OutlinedTextField(title, { title = it }, label = { Text("Session name") }, singleLine = true)
        Spacer(Modifier.height(28.dp))
        Text("%02d:%02d".format(remaining / 60, remaining % 60), fontSize = 64.sp, fontWeight = FontWeight.Light)
        Text(if (running) "Keep going. One task at a time." else "Choose a duration and begin.", color = Muted)
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(25, 50, 90).forEach { value ->
                FilterChip(
                    selected = minutes == value,
                    onClick = { if (!running) { minutes = value; remaining = value * 60; saved = false } },
                    label = { Text("${value}m") }
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                if (!running) {
                    if (remaining <= 0) remaining = minutes * 60
                    endAt = System.currentTimeMillis() + remaining * 1000L
                    val service = Intent(context, FocusTimerService::class.java)
                        .setAction(FocusTimerService.ACTION_START)
                        .putExtra(FocusTimerService.EXTRA_END_AT, endAt)
                    androidx.core.content.ContextCompat.startForegroundService(context, service)
                    running = true
                    saved = false
                } else {
                    remaining = ((endAt - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
                    context.stopService(Intent(context, FocusTimerService::class.java).setAction(FocusTimerService.ACTION_STOP))
                    running = false
                }
            }) { Text(if (running) "Pause" else "Start") }
            OutlinedButton(onClick = {
                running = false
                context.stopService(Intent(context, FocusTimerService::class.java).setAction(FocusTimerService.ACTION_STOP))
                remaining = minutes * 60; saved = false
            }) { Text("Reset") }
        }
        if (saved) Text("Session saved on this device.", color = Green, modifier = Modifier.padding(top = 14.dp))
        Text("Timer notification continues while the app is in the background.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 18.dp))
    }
}

@Composable
private fun StatsScreen(sessions: List<FocusSession>, today: Int) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("YOUR ANALYTICS", color = Green, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Progress you can see.", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard("Today", "${today}m", Modifier.weight(1f))
                SummaryCard("Sessions", "${sessions.size}", Modifier.weight(1f))
            }
        }
        item { Text("Recent sessions", fontWeight = FontWeight.Bold) }
        if (sessions.isEmpty()) item { Text("Complete a focus session to see your history.", color = Muted) }
        items(sessions.take(40)) { session ->
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceX)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(session.title, fontWeight = FontWeight.SemiBold)
                        Text(SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()).format(Date(session.completedAt)), color = Muted, fontSize = 11.sp)
                    }
                    Text("${session.minutes} min", color = Green)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = SurfaceX)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Muted, fontSize = 12.sp)
            Text(value, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ModuleScreen(name: String, db: FocusDatabase, back: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val blockedSites by db.dao().blockedSites().collectAsStateWithLifecycle(emptyList())
    val schedules by db.dao().schedules().collectAsStateWithLifecycle(emptyList())
    val blockedPackages = remember { mutableStateListOf<String>().apply { addAll(BlockRuleStore.getBlockedPackages(context)) } }
    val installedApps = remember {
        val pm = context.packageManager
        @Suppress("DEPRECATION")
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { pm.getApplicationLabel(it).toString() to it.packageName }
            .sortedBy { it.first.lowercase() }
    }
    var domain by remember { mutableStateOf("") }
    var scheduleTitle by remember { mutableStateOf("Study") }
    var scheduleHour by remember { mutableIntStateOf(19) }
    var scheduleMinute by remember { mutableIntStateOf(0) }
    var scheduleDuration by remember { mutableIntStateOf(60) }
    var goal by remember { mutableIntStateOf(120) }
    var tip by remember { mutableStateOf("Choose one important task. Work on it for 25 minutes without switching apps.") }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, null) }
                Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text("FocusX • local-first tools", color = Green, fontSize = 12.sp)
        }
        when (name) {
            "App Blocking" -> {
                item {
                    Text("Choose apps to block. Then enable FocusX in Android Accessibility settings.", color = Muted)
                    Button(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                        Text("Enable Accessibility service")
                    }
                }
                items(installedApps, key = { it.second }) { app ->
                    val selected = app.second in blockedPackages
                    Card(colors = CardDefaults.cardColors(containerColor = SurfaceX)) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(app.first)
                                Text(app.second, color = Muted, fontSize = 10.sp)
                            }
                            Checkbox(selected, { checked ->
                                BlockRuleStore.setBlocked(context, app.second, checked)
                                if (checked) { if (app.second !in blockedPackages) blockedPackages.add(app.second) }
                                else blockedPackages.remove(app.second)
                            })
                        }
                    }
                }
            }
            "Reels & Shorts" -> {
                item { Text("Choose apps whose short-video feeds you want to avoid. This version can block the whole app; reliable in-app Reels/Shorts detection is not yet implemented.", color = Muted) }
                items(listOf("Instagram", "YouTube", "Facebook", "Snapchat")) { app ->
                    val match = installedApps.firstOrNull { it.first.equals(app, true) }
                    val checked = match?.second in blockedPackages
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(app, Modifier.weight(1f))
                        Switch(checked, { value ->
                            match?.let {
                                BlockRuleStore.setBlocked(context, it.second, value)
                                if (value && it.second !in blockedPackages) blockedPackages.add(it.second)
                                if (!value) blockedPackages.remove(it.second)
                            }
                        }, enabled = match != null)
                    }
                }
            }
            "Website Blocking" -> {
                item { Text("Save domains to your local block list. Network-level filtering is not active in this release.", color = Muted) }
                item {
                    OutlinedTextField(domain, { domain = it }, label = { Text("example.com") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        val host = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").substringBefore("/")
                        if (host.contains(".") && host.length < 254) scope.launch { db.dao().saveSite(BlockedSite(host)) }
                        domain = ""
                    }) { Text("Add website") }
                }
                items(blockedSites) { site ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(site.host, Modifier.weight(1f))
                        IconButton(onClick = { scope.launch { db.dao().removeSite(site.host) } }) { Icon(Icons.Default.Delete, null) }
                    }
                }
            }
            "Schedule Planner" -> {
                item { OutlinedTextField(scheduleTitle, { scheduleTitle = it }, label = { Text("Schedule title") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Text("Start hour: $scheduleHour")
                    Slider(scheduleHour.toFloat(), { scheduleHour = it.toInt() }, valueRange = 0f..23f, steps = 22)
                    Text("Start minute: $scheduleMinute")
                    Slider(scheduleMinute.toFloat(), { scheduleMinute = (it.toInt() / 5) * 5 }, valueRange = 0f..55f, steps = 10)
                    Text("Duration: $scheduleDuration minutes")
                    Slider(scheduleDuration.toFloat(), { scheduleDuration = (it.toInt() / 5) * 5 }, valueRange = 15f..240f, steps = 44)
                    Button(onClick = {
                        scope.launch {
                            db.dao().addSchedule(FocusSchedule(
                                title = scheduleTitle.ifBlank { "Focus" },
                                startHour = scheduleHour, startMinute = scheduleMinute,
                                durationMinutes = scheduleDuration
                            ))
                        }
                    }) { Text("Save schedule") }
                }
                items(schedules) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = SurfaceX)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.title)
                                Text("%02d:%02d • %d min".format(item.startHour, item.startMinute, item.durationMinutes), color = Muted)
                            }
                            IconButton(onClick = { scope.launch { db.dao().removeSchedule(item.id) } }) { Icon(Icons.Default.Delete, null) }
                        }
                    }
                }
            }
            "Music & Themes" -> {
                item { Text("Choose an ambience for a later audio player. No copyrighted audio is bundled in this offline build.", color = Muted) }
                items(listOf("Rain ambience", "Brown noise", "White noise", "Forest", "Ocean")) { sound ->
                    Card(colors = CardDefaults.cardColors(containerColor = SurfaceX)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Headphones, null, tint = Green)
                            Spacer(Modifier.width(12.dp)); Text(sound, Modifier.weight(1f))
                            Text("Planned", color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }
            "Multiplayer" -> item { Text("Multiplayer requires an online service and accounts. It is not available in the offline MVP. The planned design includes private rooms, invite codes, synchronized focus sessions and leaderboards.", color = Muted) }
            "AI Companion" -> {
                item { Text("Offline focus coach", color = Green) }
                item { Text(tip) }
                item { Button(onClick = { tip = listOf(
                    "Write the next physical action for your task, then start a 25-minute session.",
                    "Put your phone out of reach. One task, one timer, one break.",
                    "If you feel stuck, make the task smaller: work on it for just five minutes.",
                    "At the end of the session, note what you completed before taking a break."
                ).random() }) { Text("Give me a focus tip") } }
            }
            "Goals & Settings" -> {
                item { Text("Daily focus goal: $goal minutes", color = Green) }
                item { Slider(goal.toFloat(), { goal = it.toInt() }, valueRange = 30f..480f, steps = 14) }
                item { Text("Your data is stored locally. There is no login, advertising SDK, analytics SDK or cloud sync in this build.", color = Muted) }
            }
            else -> item { Text("This module is in the FocusX roadmap.", color = Muted) }
        }
    }
}

@Composable
private fun MoreScreen(db: FocusDatabase, open: (String) -> Unit) {
    val options = listOf("Reels & Shorts", "Website Blocking", "Music & Themes", "Schedule Planner", "Multiplayer", "AI Companion", "Goals & Settings")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("MORE FEATURES", color = Green, fontWeight = FontWeight.Bold); Text("Configure your focus system.", fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        items(options) { name ->
            Card(Modifier.fillMaxWidth().clickable { open(name) }, colors = CardDefaults.cardColors(containerColor = SurfaceX)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.ChevronRight, null, tint = Muted)
                }
            }
        }
    }
}
