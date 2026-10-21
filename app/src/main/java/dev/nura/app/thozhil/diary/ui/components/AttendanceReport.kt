package dev.nura.app.thozhil.diary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nura.app.thozhil.diary.data.AttendanceStatus
import dev.nura.app.thozhil.diary.ui.DayAttendance
import dev.nura.app.thozhil.diary.ui.MonthAttendance
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import java.time.format.TextStyle
import java.util.Locale

/**
 * Compact report for one day: present / absent / not marked, a split bar, and the month's present rate.
 * With [onClick] the whole card is a button ending in [actionLabel].
 */
@Composable
fun AttendanceReportCard(
    attendance: List<DayAttendance>,
    month: MonthAttendance,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    val present = attendance.count { it.status == AttendanceStatus.PRESENT }
    val absent = attendance.count { it.status == AttendanceStatus.ABSENT }
    val unmarked = attendance.size - present - absent
    val monthName = month.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    SoftCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .then(
                    if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
                )
                .padding(start = 18.dp, end = 10.dp, top = 16.dp, bottom = if (onClick != null) 12.dp else 16.dp)
        ) {
            if (attendance.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("No one to mark yet", style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text(
                            "Add employees, then mark them here each day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted
                        )
                    }
                    if (onClick != null) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = InkMuted)
                    }
                }
                return@Column
            }

            Row(modifier = Modifier.padding(end = 8.dp)) {
                ReportFigure(present, "Present", SageDeep, Modifier.weight(1f))
                ReportFigure(absent, "Absent", ErrorRed, Modifier.weight(1f))
                ReportFigure(unmarked, "Not marked", InkMuted, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            SplitBar(present = present, absent = absent, unmarked = unmarked, modifier = Modifier.padding(end = 8.dp))
            Spacer(Modifier.height(if (onClick != null) 6.dp else 12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    month.presentPercent?.let { "$monthName so far: $it% present" } ?: "Nothing marked in $monthName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMuted,
                    modifier = Modifier.weight(1f)
                )
                if (onClick != null && actionLabel != null) {
                    Text(actionLabel, style = MaterialTheme.typography.labelMedium, color = SageDeep)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SageDeep)
                }
            }
        }
    }
}

@Composable
private fun ReportFigure(count: Int, label: String, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Text(
            count.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (count == 0) InkMuted else accent
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(accent, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = InkMuted, maxLines = 1)
        }
    }
}

/** One bar split into present / absent / not marked, in proportion. */
@Composable
private fun SplitBar(present: Int, absent: Int, unmarked: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(SandLight),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (present > 0) Box(Modifier.weight(present.toFloat()).fillMaxHeight().background(SageDeep))
        if (absent > 0) Box(Modifier.weight(absent.toFloat()).fillMaxHeight().background(ErrorRed))
        if (unmarked > 0) Box(Modifier.weight(unmarked.toFloat()).fillMaxHeight())
    }
}
