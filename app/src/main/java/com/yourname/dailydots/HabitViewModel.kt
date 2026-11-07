package com.yourname.dailydots

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitUi(
    val habit: Habit,
    val doneToday: Boolean,
    val streak: Int,
    val best: Int,
    val last30: List<Boolean> // oldest -> today
)

class HabitViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDb.get(app).dao()
    private val today = MutableStateFlow(LocalDate.now().toEpochDay())

    val items: StateFlow<List<HabitUi>> =
        combine(dao.habits(), dao.logs(), today) { habits, logs, t ->
            val byHabit = logs.groupBy({ it.habitId }, { it.epochDay })
            habits.map { h ->
                val days = byHabit[h.id].orEmpty().toSet()
                HabitUi(
                    habit = h,
                    doneToday = t in days,
                    streak = currentStreak(days, t),
                    best = bestStreak(days),
                    last30 = (29 downTo 0).map { (t - it) in days }
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Gamification: 10 XP for every completed check-in, 100 XP per level. */
    val xp: StateFlow<Int> = dao.logs()
        .map { it.size * 10 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Call when the app returns to the foreground (handles midnight rollover). */
    fun refreshToday() {
        today.value = LocalDate.now().toEpochDay()
    }

    fun toggle(id: Long) = viewModelScope.launch {
        dao.toggle(id, today.value)
        HabitWidget().updateAll(getApplication())
    }

    fun add(name: String, emoji: String) = viewModelScope.launch {
        dao.insert(Habit(name = name.trim().take(30), emoji = emoji))
        HabitWidget().updateAll(getApplication())
    }

    fun delete(id: Long) = viewModelScope.launch {
        dao.archive(id)
        HabitWidget().updateAll(getApplication())
    }

    private fun currentStreak(days: Set<Long>, today: Long): Int {
        var d = if (today in days) today else today - 1
        var count = 0
        while (d in days) { count++; d-- }
        return count
    }

    private fun bestStreak(days: Set<Long>): Int {
        var best = 0
        var run = 0
        var prev = Long.MIN_VALUE
        for (d in days.sorted()) {
            run = if (d == prev + 1) run + 1 else 1
            if (run > best) best = run
            prev = d
        }
        return best
    }
}
