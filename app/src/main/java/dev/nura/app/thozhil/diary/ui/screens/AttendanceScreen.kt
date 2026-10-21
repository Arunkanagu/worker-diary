package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.data.Attendance
import dev.nura.app.thozhil.diary.data.AttendanceStatus
import dev.nura.app.thozhil.diary.ui.AttendanceViewModel
import dev.nura.app.thozhil.diary.ui.DayAttendance
import dev.nura.app.thozhil.diary.ui.components.AttendanceReportCard
import dev.nura.app.thozhil.diary.ui.components.ClearFocusWhenClosed
import dev.nura.app.thozhil.diary.ui.components.InitialsAvatar
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.Mist
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SageInk
import dev.nura.app.thozhil.diary.ui.theme.Sand
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun AttendanceScreen(
    onBack: () -> Unit,
    onOpenEmployees: () -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val date by viewModel.selectedDate.collectAsStateWithLifecycle()
    val attendance by viewModel.dayAttendance.collectAsStateWithLifecycle()
    val month by viewModel.monthAttendance.collectAsStateWithLifecycle()
    var pickingDate by remember { mutableStateOf(false) }
    var editingReason by remember { mutableStateOf<DayAttendance?>(null) }
    val unmarked = attendance.count { it.status == null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        ScreenHeader(
            title = "Attendance",
            onBack = onBack,
            modifier = Modifier.padding(start = 8.dp, end = 20.dp)
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(4.dp))
            DateBar(
                date = date,
                onPrevious = { viewModel.selectDate(date.minusDays(1)) },
                onNext = { viewModel.selectDate(date.plusDays(1)) },
                onPick = { pickingDate = true }
            )

            Spacer(Modifier.height(12.dp))
            AttendanceReportCard(attendance = attendance, month = month)

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Mark attendance",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                if (unmarked > 0) {
                    TextButton(onClick = viewModel::markRestPresent) {
                        Text(if (unmarked == attendance.size) "All present" else "Rest present", color = SageDeep)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            AttendanceSheet(
                attendance = attendance,
                onToggle = { day, chosen -> viewModel.toggle(day.employee.id, day.status, chosen) },
                onEditReason = { editingReason = it },
                onAddEmployees = onOpenEmployees
            )
        }
    }

    if (pickingDate) {
        AttendanceDatePicker(
            selected = date,
            onPick = {
                viewModel.selectDate(it)
                pickingDate = false
            },
            onDismiss = { pickingDate = false }
        )
    }

    ClearFocusWhenClosed(open = editingReason != null)

    editingReason?.let { day ->
        day.record?.let { record ->
            AbsentReasonDialog(
                name = day.employee.name,
                record = record,
                onDismiss = { editingReason = null },
                onSave = {
                    viewModel.setAbsentReason(record, it)
                    editingReason = null
                }
            )
        }
    }
}

/** Previous / next day arrows around the date; tapping the date opens a calendar. */
@Composable
private fun DateBar(
    date: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPick: () -> Unit
) {
    val today = LocalDate.now()
    val relative = when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE"))
    }
    SoftCard(modifier = Modifier.fillMaxWidth(), shape = CircleShape) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous day", tint = Ink)
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "Choose a date", onClick = onPick),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = SageDeep, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink
                    )
                    Text(relative, style = MaterialTheme.typography.bodySmall, color = InkMuted)
                }
            }
            IconButton(onClick = onNext, enabled = date.isBefore(today)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next day")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceDatePicker(
    selected: LocalDate,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // The picker works in UTC midnight millis
    val todayMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selected.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
            override fun isSelectableYear(year: Int) = year <= LocalDate.now().year
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(containerColor = IvoryCard),
        confirmButton = {
            Button(
                onClick = {
                    state.selectedDateMillis?.let {
                        onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    } ?: onDismiss()
                },
                shape = CircleShape
            ) { Text("Show") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SageDeep) }
        }
    ) {
        DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = IvoryCard))
    }
}

/** Each person with ✓ / ✕; absent people get a reason line under their name. */
@Composable
private fun AttendanceSheet(
    attendance: List<DayAttendance>,
    onToggle: (DayAttendance, AttendanceStatus) -> Unit,
    onEditReason: (DayAttendance) -> Unit,
    onAddEmployees: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        if (attendance.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onAddEmployees)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Mist, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = SageInk)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Add your team", style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text(
                        "Add employees to mark their attendance here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted
                    )
                }
            }
            return@SoftCard
        }

        Spacer(Modifier.height(4.dp))
        attendance.forEachIndexed { index, day ->
            if (index > 0) HorizontalDivider(color = SandLight, modifier = Modifier.padding(start = 66.dp))
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InitialsAvatar(name = day.employee.name, size = 38.dp, showDot = false)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        day.employee.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (day.status == AttendanceStatus.ABSENT) {
                        val reason = day.record?.absentReason.orEmpty()
                        Text(
                            reason.ifEmpty { "Add reason" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (reason.isEmpty()) SageDeep else InkMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Edit reason for ${day.employee.name}"
                                ) { onEditReason(day) }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                MarkChip(
                    label = "Present",
                    icon = Icons.Filled.Check,
                    selected = day.status == AttendanceStatus.PRESENT,
                    selectedColor = SageDeep,
                    personName = day.employee.name,
                    onClick = { onToggle(day, AttendanceStatus.PRESENT) }
                )
                Spacer(Modifier.width(8.dp))
                MarkChip(
                    label = "Absent",
                    icon = Icons.Filled.Close,
                    selected = day.status == AttendanceStatus.ABSENT,
                    selectedColor = ErrorRed,
                    personName = day.employee.name,
                    onClick = { onToggle(day, AttendanceStatus.ABSENT) }
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

/** Round toggle; filled when chosen, outlined otherwise. */
@Composable
private fun MarkChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    selectedColor: Color,
    personName: String,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(if (selected) selectedColor else Color.Transparent, tween(180), label = "markBg")
    val fg by animateColorAsState(if (selected) Color.White else InkMuted, tween(180), label = "markFg")
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, if (selected) selectedColor else Sand, CircleShape)
            .toggleable(value = selected, role = Role.RadioButton, onValueChange = { onClick() })
            .semantics { contentDescription = "$label, $personName" },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun AbsentReasonDialog(
    name: String,
    record: Attendance,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var reason by remember { mutableStateOf(record.absentReason) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IvoryCard,
        title = { Text("Why is $name absent?", color = Ink) },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it.take(120) },
                label = { Text("Reason") },
                supportingText = { Text("Optional, for example sick or family function") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(reason) }, shape = CircleShape) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SageDeep) }
        }
    )
}
