package dev.nura.app.thozhil.diary.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.data.AttendanceStatus
import dev.nura.app.thozhil.diary.ui.DayAttendance
import dev.nura.app.thozhil.diary.ui.MonthAttendance
import dev.nura.app.thozhil.diary.ui.theme.Amber
import dev.nura.app.thozhil.diary.ui.theme.AmberInk
import dev.nura.app.thozhil.diary.ui.theme.BricolageDisplay
import dev.nura.app.thozhil.diary.ui.theme.Coral
import dev.nura.app.thozhil.diary.ui.theme.Cover
import dev.nura.app.thozhil.diary.ui.theme.CoverHigh
import dev.nura.app.thozhil.diary.ui.theme.CoverMint
import dev.nura.app.thozhil.diary.ui.theme.OnCoverMuted
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import dev.nura.app.thozhil.diary.util.Language
import java.util.Locale

@Composable
private fun dayOfWeekRes(dayOfWeek: DayOfWeek): Int = when (dayOfWeek) {
    DayOfWeek.MONDAY -> R.string.monday
    DayOfWeek.TUESDAY -> R.string.tuesday
    DayOfWeek.WEDNESDAY -> R.string.wednesday
    DayOfWeek.THURSDAY -> R.string.thursday
    DayOfWeek.FRIDAY -> R.string.friday
    DayOfWeek.SATURDAY -> R.string.saturday
    DayOfWeek.SUNDAY -> R.string.sunday
}

@Composable
private fun monthRes(month: Month): Int = when (month) {
    Month.JANUARY -> R.string.january
    Month.FEBRUARY -> R.string.february
    Month.MARCH -> R.string.march
    Month.APRIL -> R.string.april
    Month.MAY -> R.string.may
    Month.JUNE -> R.string.june
    Month.JULY -> R.string.july
    Month.AUGUST -> R.string.august
    Month.SEPTEMBER -> R.string.september
    Month.OCTOBER -> R.string.october
    Month.NOVEMBER -> R.string.november
    Month.DECEMBER -> R.string.december
}

/**
 * The day's attendance on the diary's green cover: a ring split into present, absent and not
 * marked, the three counts beside it, and the month's present rate below. With [date] and
 * [onPickDate] it also carries the day switcher; with [onClick] the whole card opens attendance.
 */
@Composable
fun AttendanceReportCard(
    attendance: List<DayAttendance>,
    month: MonthAttendance,
    modifier: Modifier = Modifier,
    date: LocalDate? = null,
    onPreviousDate: (() -> Unit)? = null,
    onNextDate: (() -> Unit)? = null,
    onPickDate: (() -> Unit)? = null,
    onTodayDate: (() -> Unit)? = null,
    actionLabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    val total = attendance.size
    val present = attendance.count { it.status == AttendanceStatus.PRESENT }
    val absent = attendance.count { it.status == AttendanceStatus.ABSENT }
    val unmarked = total - present - absent
    val monthName = stringResource(monthRes(month.month.month))

    Surface(
        color = Cover,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
                .padding(bottom = 18.dp)
        ) {
            if (date != null && onPickDate != null) {
                DaySwitcher(date, onPreviousDate, onNextDate, onPickDate, onTodayDate)
                HorizontalDivider(color = CoverHigh, modifier = Modifier.padding(horizontal = 20.dp))
            }

            if (total == 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = 18.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.no_employees_added_yet), style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            stringResource(R.string.add_employees_to_see_their_attendance_here),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnCoverMuted
                        )
                    }
                    if (onClick != null) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = OnCoverMuted)
                    }
                }
                return@Column
            }

            val ringContentDescription = stringResource(R.string.format_present_absent_not_marked, present, total, absent, unmarked)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp)
            ) {
                DayRing(
                    present = present,
                    absent = absent,
                    total = total,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = ringContentDescription
                    }
                )
                Spacer(Modifier.width(20.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    CountRow(stringResource(R.string.present), present, CoverMint)
                    CountRow(stringResource(R.string.absent), absent, Coral)
                    CountRow(stringResource(R.string.not_marked), unmarked, OnCoverMuted.copy(alpha = 0.5f))
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = CoverHigh, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.s_so_far, monthName), style = MaterialTheme.typography.labelMedium, color = OnCoverMuted)
                    Text(
                        month.presentPercent?.let { stringResource(R.string.d_percent_present, it) } ?: stringResource(R.string.nothing_marked_yet),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
                if (onClick != null && actionLabel != null) {
                    // The one amber thing on the card: today's next step
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .clip(CircleShape)
                            .background(Amber)
                            .padding(start = 16.dp, end = 10.dp)
                    ) {
                        Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = AmberInk, maxLines = 1)
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = AmberInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Previous and next day around the date, which opens a calendar; "Today" jumps back. */
@Composable
private fun DaySwitcher(
    date: LocalDate,
    onPreviousDate: (() -> Unit)?,
    onNextDate: (() -> Unit)?,
    onPickDate: () -> Unit,
    onTodayDate: (() -> Unit)?
) {
    val today = LocalDate.now()
    val isToday = date == today
    val relative = when (date) {
        today -> stringResource(R.string.today)
        today.minusDays(1) -> stringResource(R.string.yesterday)
        else -> stringResource(dayOfWeekRes(date.dayOfWeek))
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        if (onPreviousDate != null) {
            IconButton(onClick = onPreviousDate) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_day), tint = Color.White)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.choose_a_date), onClick = onPickDate)
        ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = OnCoverMuted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Language.locale)),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
                Text(relative, style = MaterialTheme.typography.bodySmall, color = OnCoverMuted, maxLines = 1)
            }
        }
        if (!isToday && onTodayDate != null) {
            Box(
                modifier = Modifier
                    .heightIn(min = 36.dp)
                    .clip(CircleShape)
                    .background(Amber)
                    .clickable(role = Role.Button, onClick = onTodayDate)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.today), style = MaterialTheme.typography.labelMedium, color = AmberInk, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
        if (onNextDate != null) {
            IconButton(onClick = onNextDate, enabled = date.isBefore(today)) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.next_day),
                    tint = if (date.isBefore(today)) Color.White else OnCoverMuted.copy(alpha = 0.35f)
                )
            }
        }
    }
}

/**
 * The day as a ring: present from the top, then absent, with what's left unmarked as the track.
 * It eases to new values so a mark visibly lands.
 */
@Composable
private fun DayRing(present: Int, absent: Int, total: Int, modifier: Modifier = Modifier) {
    val presentShare by animateFloatAsState(if (total == 0) 0f else present / total.toFloat(), tween(600), label = "present")
    val absentShare by animateFloatAsState(if (total == 0) 0f else absent / total.toFloat(), tween(600), label = "absent")
    Box(modifier = modifier.size(116.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(116.dp)) {
            val stroke = 11.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(CoverHigh, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            // A small gap keeps neighbouring segments apart
            val gap = if (presentShare > 0f && absentShare > 0f) 6f else 0f
            val presentSweep = 360f * presentShare
            val absentSweep = 360f * absentShare
            if (presentSweep > 0f) {
                drawArc(CoverMint, -90f, (presentSweep - gap).coerceAtLeast(0.5f), false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            if (absentSweep > 0f) {
                drawArc(Coral, -90f + presentSweep, (absentSweep - gap).coerceAtLeast(0.5f), false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$present",
                fontFamily = BricolageDisplay,
                fontSize = 34.sp,
                lineHeight = 36.sp,
                color = Color.White
            )
            Text(stringResource(R.string.of_d_in, total), style = MaterialTheme.typography.labelMedium, color = OnCoverMuted, maxLines = 1)
        }
    }
}

@Composable
private fun CountRow(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(width = 14.dp, height = 6.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        // Shrinks a little rather than cutting a longer word ("குறிக்கவில்லை") short
        BasicText(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(color = OnCoverMuted),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = MaterialTheme.typography.bodyMedium.fontSize),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "$count",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}
