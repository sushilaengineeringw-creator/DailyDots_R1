package com.yourname.dailydots

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class App : Application() {
    val billing: BillingManager by lazy { BillingManager(this) }

    override fun onCreate() {
        super.onCreate()
        billing.connect()
    }
}

class MainActivity : ComponentActivity() {
    private val vm: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val billing = (application as App).billing
        setContent {
            DailyDotsTheme {
                HabitApp(vm, billing, this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshToday()
        (application as App).billing.refresh()
    }
}

@Composable
fun DailyDotsTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> darkColorScheme(primary = Color(0xFF9D8CFF))
        else -> lightColorScheme(primary = Color(0xFF6C5CE7))
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitApp(vm: HabitViewModel, billing: BillingManager, activity: Activity) {
    val list by vm.items.collectAsState()
    val xp by vm.xp.collectAsState()
    val premium by billing.isPremium.collectAsState()
    val prices by billing.prices.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<HabitUi?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DailyDots", fontWeight = FontWeight.Bold) },
                actions = {
                    if (premium) {
                        Text(
                            "PRO ⭐",
                            modifier = Modifier.padding(end = 16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        TextButton(onClick = { showPaywall = true }) { Text("Go Pro") }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (!premium && list.size >= Products.FREE_LIMIT) showPaywall = true else showAdd = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New habit") }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { LevelCard(xp) }
            item { Summary(done = list.count { it.doneToday }, total = list.size) }
            if (list.isEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Start in one tap. Pick a starter habit:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "💧" to "Drink water",
                                "🚶" to "Walk 10 minutes",
                                "📖" to "Read 10 pages",
                                "🧘" to "Breathe 2 minutes"
                            ).forEach { (e, n) ->
                                AssistChip(onClick = { vm.add(n, e) }, label = { Text("$e $n") })
                            }
                        }
                        Text(
                            "Then long-press your home screen → Widgets → DailyDots to tick habits without opening the app.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            items(list, key = { it.habit.id }) { ui ->
                HabitCard(
                    ui = ui,
                    premium = premium,
                    onToggle = { vm.toggle(ui.habit.id) },
                    onDelete = { toDelete = ui }
                )
            }
            if (!premium) {
                item {
                    Text(
                        "Free plan: up to ${Products.FREE_LIMIT} habits.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAdd) {
        AddHabitDialog(
            onDismiss = { showAdd = false },
            onAdd = { name, emoji -> vm.add(name, emoji); showAdd = false }
        )
    }
    if (showPaywall) {
        PaywallDialog(
            prices = prices,
            onBuy = { id -> billing.purchase(activity, id) },
            onRestore = { billing.refresh() },
            onDismiss = { showPaywall = false }
        )
    }
    toDelete?.let { ui ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete habit?") },
            text = { Text("“${ui.habit.name}” will be removed from your list and widget.") },
            confirmButton = {
                TextButton(onClick = { vm.delete(ui.habit.id); toDelete = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }
}

/** Gamification hook: every check-in earns XP; levels give a reason to come back daily. */
@Composable
fun LevelCard(xp: Int) {
    val level = xp / 100 + 1
    val titles = listOf("Seedling", "Sprout", "Builder", "Streaker", "Champion", "Legend")
    val title = titles[minOf(level - 1, titles.lastIndex)]
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Level $level · $title",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$xp XP · ${100 - xp % 100} XP to next level",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (xp % 100) / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

fun milestone(streak: Int): String = when {
    streak >= 30 -> " 🏆"
    streak >= 7 -> " 🏅"
    streak >= 3 -> " 🌱"
    else -> ""
}

@Composable
fun Summary(done: Int, total: Int) {
    Column {
        Text("Today: $done of $total done", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else done.toFloat() / total },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HabitCard(ui: HabitUi, premium: Boolean, onToggle: () -> Unit, onDelete: () -> Unit) {
    val days = if (premium) 30 else 7
    val on = MaterialTheme.colorScheme.primary
    val off = MaterialTheme.colorScheme.surfaceVariant
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ui.habit.emoji, fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        ui.habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "🔥 ${ui.streak} day streak" + milestone(ui.streak) +
                            if (premium) "  •  best ${ui.best}" else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete habit")
                }
                FilledIconToggleButton(checked = ui.doneToday, onCheckedChange = { onToggle() }) {
                    Icon(Icons.Default.Check, contentDescription = "Mark done today")
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                ui.last30.takeLast(days).forEach { d ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (d) on else off)
                    )
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("💧") }
    val emojis = listOf("💧", "🏃", "📚", "🧘", "💪", "😴", "🥗", "✍️", "🚭", "🎯")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New habit") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 30) name = it },
                    label = { Text("Habit name") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    emojis.forEach { e ->
                        Box(
                            Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    if (e == emoji) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable { emoji = e }
                                .padding(8.dp)
                        ) { Text(e, fontSize = 24.sp) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name, emoji) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun PaywallDialog(
    prices: Map<String, String>,
    onBuy: (String) -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("DailyDots Pro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("✔ Unlimited habits\n✔ 30-day history and best streak\n✔ Bigger home-screen widget\n✔ Pay once, no account, no ads")
                Button(
                    onClick = { onBuy(Products.LIFETIME) },
                    enabled = prices[Products.LIFETIME] != null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Lifetime unlock — ${prices[Products.LIFETIME] ?: "…"}") }
                OutlinedButton(
                    onClick = { onBuy(Products.ANNUAL) },
                    enabled = prices[Products.ANNUAL] != null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Yearly — ${prices[Products.ANNUAL] ?: "…"} / year") }
                TextButton(onClick = onRestore) { Text("Restore purchase") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Not now") } }
    )
}
