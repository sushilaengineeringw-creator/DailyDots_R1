package com.yourname.dailydots

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.LocalDate

val HabitIdKey = ActionParameters.Key<Long>("habitId")

class HabitWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = AppDb.get(context).dao()
        val today = LocalDate.now().toEpochDay()
        val all = dao.habitsOnce()
        val done = dao.doneOn(today).toSet()
        val limit = if (Products.cachedPremium(context)) 6 else Products.FREE_LIMIT
        val shown = all.take(limit)
        val doneCount = all.count { it.id in done }

        provideContent { WidgetContent(shown, done, doneCount, all.size) }
    }
}

@Composable
private fun WidgetContent(habits: List<Habit>, done: Set<Long>, doneCount: Int, total: Int) {
    val context = LocalContext.current
    val openApp = actionStartActivity(Intent(context, MainActivity::class.java))
    val white = ColorProvider(Color.White)
    val muted = ColorProvider(Color(0xFFB8B5D0))

    Column(
        GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF1E1B2E))
            .cornerRadius(20.dp)
            .padding(12.dp)
    ) {
        Text(
            text = "Today  $doneCount/$total",
            style = TextStyle(color = white, fontWeight = FontWeight.Bold, fontSize = 15.sp),
            modifier = GlanceModifier.fillMaxWidth().clickable(openApp)
        )
        Spacer(GlanceModifier.padding(2.dp))

        if (habits.isEmpty()) {
            Text(
                text = "Tap to add your first habit",
                style = TextStyle(color = muted, fontSize = 13.sp),
                modifier = GlanceModifier.clickable(openApp)
            )
        }

        habits.forEach { h ->
            val isDone = h.id in done
            Row(
                GlanceModifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable(actionRunCallback<ToggleAction>(actionParametersOf(HabitIdKey to h.id))),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDone) "✅" else "⬜",
                    style = TextStyle(fontSize = 16.sp)
                )
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = "${h.emoji} ${h.name}",
                    style = TextStyle(color = if (isDone) muted else white, fontSize = 14.sp),
                    maxLines = 1
                )
            }
        } 
    }
}

/** Tapping a row on the home screen ticks the habit without opening the app. */
class ToggleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[HabitIdKey] ?: return
        AppDb.get(context).dao().toggle(id, LocalDate.now().toEpochDay())
        HabitWidget().updateAll(context)
    }
}

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()
}
