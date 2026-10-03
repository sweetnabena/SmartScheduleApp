package com.example.f329723_app.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HighlightedCalendar(
    markedDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val calendarState = rememberCalendarState(
        startMonth = YearMonth.now().minusMonths(12),
        endMonth = YearMonth.now().plusMonths(12),
        firstVisibleMonth = YearMonth.now(),
        firstDayOfWeek = DayOfWeek.MONDAY
    )

    VerticalCalendar(
        state = calendarState,
        monthHeader = {
        },
        dayContent = { day ->
            // Only show days that belong to the current month
            if (day.position == DayPosition.MonthDate) {
                val isMarked = markedDates.contains(day.date)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .background(
                            color = if (isMarked) Color(0xFF81C784) else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        )
                        .clickable { onDateClick(day.date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = day.date.dayOfMonth.toString())
                }
            }
        }

    )
}

