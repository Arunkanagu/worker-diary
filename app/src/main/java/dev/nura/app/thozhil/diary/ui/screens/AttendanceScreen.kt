package dev.nura.app.thozhil.diary.ui.screens

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.R
import androidx.compose.material.icons.outlined.Assessment
import dev.nura.app.thozhil.diary.data.AttendanceStatus
import dev.nura.app.thozhil.diary.ui.AttendanceViewModel
import dev.nura.app.thozhil.diary.ui.DayAttendance
import dev.nura.app.thozhil.diary.ui.components.AttendanceReportCard
import dev.nura.app.thozhil.diary.ui.components.CalmSearchField
import dev.nura.app.thozhil.diary.ui.components.CalmSegmented
import dev.nura.app.thozhil.diary.ui.components.ClearFocusWhenClosed
import dev.nura.app.thozhil.diary.ui.components.EmptyState
import dev.nura.app.thozhil.diary.ui.components.InitialsAvatar
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.components.StatusPill
import dev.nura.app.thozhil.diary.ui.theme.Amber
import dev.nura.app.thozhil.diary.ui.theme.AmberInk
import dev.nura.app.thozhil.diary.ui.theme.AmberSoft
import dev.nura.app.thozhil.diary.ui.theme.BricolageDisplay
import dev.nura.app.thozhil.diary.ui.theme.Cover
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.Mist
import dev.nura.app.thozhil.diary.ui.theme.OnCoverMuted
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SageInk
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import dev.nura.app.thozhil.diary.util.Language
import java.time.format.DateTimeFormatter

/** "08:30 AM" for display and database. */
private fun LocalTime.label(): String = format(DateTimeFormatter.ofPattern("hh:mm a"))

/** Converts "08:30 AM" back to LocalTime, or null if unparseable. */
private fun parseTime(text: String?): LocalTime? {
    if (text.isNullOrBlank()) return null
    return runCatching { LocalTime.parse(text, DateTimeFormatter.ofPattern("hh:mm a")) }.getOrNull()
}

/** LocalTime rounded to the minute for now. */
private fun nowToMinute(): LocalTime = LocalTime.now().withSecond(0).withNano(0)

/** Presets for the absence reason dropdown. */
private val absenceReasons = listOf(
    R.string.sick_leave,
    R.string.personal_leave,
    R.string.family_function,
    R.string.emergency,
    R.string.medical_appointment,
    R.string.injury,
    R.string.festival_holiday,
    R.string.rain_weather,
    R.string.no_work_at_site,
    R.string.material_not_ready,
    R.string.transport_issue,
    R.string.out_of_town,
    R.string.unapproved_absence,
    R.string.other
)

/** Which time a sheet is setting. */
private enum class ShiftTime(@param:StringRes val labelRes: Int, @param:StringRes val titleRes: Int) {
    CHECK_IN(R.string.check_in_lower, R.string.check_in_title),
    CHECK_OUT(R.string.check_out_lower, R.string.check_out_title)
}

private val ShiftTime.accent get() = if (this == ShiftTime.CHECK_IN) SageDeep else AmberInk
private val ShiftTime.tint get() = if (this == ShiftTime.CHECK_IN) Mist else AmberSoft
private val ShiftTime.icon get() = if (this == ShiftTime.CHECK_IN) Icons.AutoMirrored.Outlined.Login else Icons.AutoMirrored.Outlined.Logout
/** Times most crews start and finish at, one tap away in the clock. */
private val ShiftTime.presets
    get() = if (this == ShiftTime.CHECK_IN) {
        listOf(LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0), LocalTime.of(9, 30))
    } else {
        listOf(LocalTime.of(17, 0), LocalTime.of(17, 30), LocalTime.of(18, 0), LocalTime.of(18, 30))
    }

/** "8 h 30 min" between check-in and check-out, or null when out isn't after in. */
private fun shiftLength(inTime: LocalTime?, outTime: LocalTime?): String? {
    if (inTime == null || outTime == null || !outTime.isAfter(inTime)) return null
    val minutes = Duration.between(inTime, outTime).toMinutes()
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0L -> "$m min"
        m == 0L -> "$h h"
        else -> "$h h $m min"
    }
}

/** What an employee already has for the day, in a few words: "In 09:00 AM, out 06:00 PM". */
@Composable
private fun DayAttendance.markedAs(): String = when (status) {
    AttendanceStatus.PRESENT -> {
        val inTime = record?.inTime?.takeIf { it.isNotBlank() }
        val outTime = record?.outTime?.takeIf { it.isNotBlank() }
        val ot = record?.overtimeHours?.toInt() ?: 0
        val otText = if (ot > 0 && outTime != null) " " + pluralStringResource(R.plurals.ot_hours_short, ot, ot) else ""
        when {
            inTime != null && outTime != null -> stringResource(R.string.in_s_out_s, inTime, outTime) + otText
            inTime != null -> stringResource(R.string.in_s, inTime)
            outTime != null -> stringResource(R.string.out_s, outTime) + otText
            else -> stringResource(R.string.present_no_times)
        }
    }
    AttendanceStatus.ABSENT -> record?.absentReason?.takeIf { it.isNotBlank() }?.let { stringResource(R.string.absent_s, it) } ?: stringResource(R.string.absent)
    null -> stringResource(R.string.not_marked)
}

/**
 * A bottom sheet on phones; on tablets a panel that slides in from the end edge, so the list stays
 * in view beside it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendancePanel(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val wide = LocalConfiguration.current.screenWidthDp >= 600
    if (!wide) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = IvoryCard
        ) { content() }
        return
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shown = remember { MutableTransitionState(false).apply { targetState = true } }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.CenterEnd
        ) {
            AnimatedVisibility(visibleState = shown, enter = slideInHorizontally { it } + fadeIn()) {
                Surface(
                    color = IvoryCard,
                    shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(440.dp)
                        // Taps inside the panel shouldn't fall through to the dismissing backdrop
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                ) {
                    Column(modifier = Modifier.systemBarsPadding()) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, end = 8.dp)
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = InkMuted)
                            }
                        }
                        content()
                    }
                }
            }
        }
    }
}

/**
 * One time, large and easy to hit: tap the card to change it on the clock. An empty card says what
 * a tap does; [onClear] adds a way to take the time off again.
 */
@Composable
private fun TimeCard(
    which: ShiftTime,
    time: LocalTime?,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
    note: String? = null,
    onClear: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(20.dp)
    val set = time != null
    val whichLabel = stringResource(which.labelRes)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(shape)
            .background(if (set) which.tint else IvoryCard)
            .border(1.dp, if (set) which.tint else SandLight, shape)
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.change_s_time, whichLabel), onClick = onPick)
            .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(if (set) IvoryCard else which.tint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(which.icon, contentDescription = null, tint = which.accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(which.titleRes), style = MaterialTheme.typography.labelLarge, color = InkMuted, maxLines = 1)
            if (time != null) {
                Text(
                    time.label().uppercase(),
                    fontFamily = BricolageDisplay,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    color = Ink,
                    maxLines = 1
                )
            } else {
                Text(stringResource(R.string.tap_to_add), style = MaterialTheme.typography.titleMedium, color = which.accent, maxLines = 1)
            }
            if (note != null) {
                Text(note, style = MaterialTheme.typography.bodySmall, color = InkMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (onClear != null && set) {
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.remove_s, whichLabel), tint = InkMuted)
            }
        } else {
            Icon(
                Icons.Outlined.Schedule,
                contentDescription = null,
                tint = which.accent,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

/**
 * Overtime selection card: shown only when check-out time is added.
 * Allows picking OT in hours.
 */
@Composable
private fun OvertimeCard(
    hours: Int,
    onHoursChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    SoftCard(
        modifier = modifier.fillMaxWidth(),
        shape = shape
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Mist, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.MoreTime, contentDescription = null, tint = SageInk, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.overtime), style = MaterialTheme.typography.labelLarge, color = InkMuted, maxLines = 1)
                    Text(
                        if (hours > 0) pluralStringResource(R.plurals.ot_hours, hours, hours) else stringResource(R.string.no_ot),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (hours > 0) SageDeep else Ink,
                        maxLines = 1
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { if (hours > 0) onHoursChange(hours - 1) },
                        enabled = hours > 0
                    ) {
                        Icon(Icons.Outlined.RemoveCircleOutline, contentDescription = null, tint = if (hours > 0) SageDeep else SandLight)
                    }
                    Text("$hours h", style = MaterialTheme.typography.titleMedium, color = Ink)
                    IconButton(
                        onClick = { if (hours < 24) onHoursChange(hours + 1) }
                    ) {
                        Icon(Icons.Outlined.AddCircleOutline, contentDescription = null, tint = SageDeep)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                listOf(0, 1, 2, 3, 4, 5, 6, 8, 10, 12).forEach { option ->
                    val selected = hours == option
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(if (selected) SageDeep else SandLight)
                            .clickable { onHoursChange(option) }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (option == 0) "0 h" else "+$option h",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else Ink
                        )
                    }
                }
            }
        }
    }
}

/** Time picker dialog with presets for quick entry. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClockDialog(
    which: ShiftTime,
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute)
    var typing by remember { mutableStateOf(false) }
    val colors = TimePickerDefaults.colors(
        selectorColor = SageDeep,
        containerColor = IvoryCard,
        periodSelectorBorderColor = SandLight,
        periodSelectorSelectedContainerColor = Mist,
        periodSelectorSelectedContentColor = SageInk,
        timeSelectorSelectedContainerColor = Mist,
        timeSelectorSelectedContentColor = SageInk,
        timeSelectorUnselectedContainerColor = SandLight
    )
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        // The dial needs more width than a standard dialog allows on a narrow phone
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp)
    ) {
        Surface(color = IvoryCard, shape = RoundedCornerShape(28.dp)) {
            Column(modifier = Modifier.padding(vertical = 24.dp)) {
                Text(
                    stringResource(R.string.s_time, stringResource(which.titleRes)),
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    TimeChip(stringResource(R.string.now), selected = false) { onConfirm(nowToMinute()) }
                    which.presets.forEach { preset ->
                        TimeChip(preset.label(), selected = false) { onConfirm(preset) }
                    }
                }
                Spacer(Modifier.height(18.dp))
                // The app's display face is taller than the picker's hour and minute boxes allow
                val base = MaterialTheme.typography
                MaterialTheme(typography = base.copy(displayLarge = base.headlineLarge.copy(fontSize = 44.sp, lineHeight = 52.sp))) {
                    Box(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 24.dp)
                    ) {
                        if (typing) TimeInput(state = state, colors = colors) else TimePicker(state = state, colors = colors)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    IconButton(onClick = { typing = !typing }) {
                        Icon(
                            if (typing) Icons.Outlined.Schedule else Icons.Outlined.Keyboard,
                            contentDescription = if (typing) stringResource(R.string.use_the_clock) else stringResource(R.string.type_the_time),
                            tint = InkMuted
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = SageDeep, maxLines = 1) }
                    Spacer(Modifier.width(4.dp))
                    Button(
                        onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SageDeep)
                    ) { Text(stringResource(R.string.set_time), maxLines = 1) }
                }
            }
        }
    }
}

/** The reason for an absence, picked from a list. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AbsenceReasonDropdown(selectedRes: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(selectedRes),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.reason)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = IvoryCard
        ) {
            absenceReasons.forEach { reasonRes ->
                val label = stringResource(reasonRes)
                DropdownMenuItem(
                    text = {
                        Text(
                            label,
                            color = if (reasonRes == selectedRes) ErrorRed else Ink,
                            fontWeight = if (reasonRes == selectedRes) FontWeight.SemiBold else null
                        )
                    },
                    trailingIcon = if (reasonRes == selectedRes) {
                        { Icon(Icons.Filled.Check, contentDescription = null, tint = ErrorRed) }
                    } else null,
                    onClick = {
                        onSelect(reasonRes)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

/** The absence part of a sheet: the reason, a custom reason for "Other", and remarks. */
@Composable
private fun AbsenceFields(
    reasonRes: Int,
    onReason: (Int) -> Unit,
    customReason: String,
    onCustomReason: (String) -> Unit,
    remarks: String,
    onRemarks: (String) -> Unit
) {
    AbsenceReasonDropdown(selectedRes = reasonRes, onSelect = onReason)
    if (reasonRes == R.string.other) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = customReason,
            onValueChange = onCustomReason,
            label = { Text(stringResource(R.string.reason_for_absence)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
    }
    Spacer(Modifier.height(8.dp))
    OptionalRemarks(remarks, onRemarks, stringResource(R.string.eg_approved_leave))
}

/** Remarks stay out of the way behind "Add remarks" until someone wants to write one. */
@Composable
private fun OptionalRemarks(value: String, onValueChange: (String) -> Unit, example: String) {
    var open by remember { mutableStateOf(value.isNotBlank()) }
    if (open) {
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.remarks)) },
            placeholder = { Text(example) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        TextButton(onClick = { open = true }, modifier = Modifier.heightIn(min = 48.dp)) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = SageDeep, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.add_remarks), color = SageDeep, maxLines = 1)
        }
    }
}

/** Cancel at its own width and the main action filling the rest, so neither label is cut off. */
@Composable
private fun SheetActions(
    saveLabel: String,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    saveColor: Color = SageDeep
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(onClick = onCancel, modifier = Modifier.heightIn(min = 52.dp)) {
            Text(stringResource(R.string.cancel), color = SageDeep, maxLines = 1)
        }
        Button(
            onClick = onSave,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = saveColor),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp)
        ) {
            // Shrinks a little rather than cutting a longer label short
            androidx.compose.foundation.text.BasicText(
                saveLabel,
                style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
                maxLines = 1,
                autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(
                    minFontSize = 11.sp,
                    maxFontSize = MaterialTheme.typography.labelLarge.fontSize
                )
            )
        }
    }
}

/** Name, date and status at the top of a sheet. */
@Composable
private fun SheetHeader(title: String, date: LocalDate, leading: @Composable () -> Unit, trailing: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        leading()
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Language.locale)),
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted
            )
        }
        trailing()
    }
}

@Composable
private fun TimeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(if (selected) SageDeep else SandLight)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else Ink,
            maxLines = 1
        )
    }
}

@Composable
fun AttendanceScreen(
    onBack: () -> Unit,
    onOpenEmployees: () -> Unit,
    onOpenReport: () -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val date by viewModel.selectedDate.collectAsStateWithLifecycle()
    val attendance by viewModel.dayAttendance.collectAsStateWithLifecycle()
    val month by viewModel.monthAttendance.collectAsStateWithLifecycle()
    val totalCount = attendance.size

    var filterIndex by rememberSaveable { mutableStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedEmployeeIds by rememberSaveable { mutableStateOf(setOf<String>()) }

    var activeIndividualSheetDay by remember { mutableStateOf<DayAttendance?>(null) }
    var bulkSheetMode by remember { mutableStateOf<AttendanceStatus?>(null) }
    var pickingDate by remember { mutableStateOf(false) }

    val presentCount = attendance.count { it.status == AttendanceStatus.PRESENT }
    val absentCount = attendance.count { it.status == AttendanceStatus.ABSENT }
    val unmarkedCount = attendance.count { it.status == null }

    val isMultiSelectMode = selectedEmployeeIds.isNotEmpty()

    fun clearSelection() {
        selectedEmployeeIds = emptySet()
    }

    val visibleAttendance = attendance.filter { day ->
        val matchesStatus = when (filterIndex) {
            1 -> day.status == AttendanceStatus.PRESENT
            2 -> day.status == AttendanceStatus.ABSENT
            3 -> day.status == null
            else -> true
        }
        val matchesQuery = day.employee.name.contains(query.trim(), ignoreCase = true) ||
            day.employee.mobile.contains(query.trim())
        matchesStatus && matchesQuery
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(R.string.attendance),
                onBack = onBack,
                modifier = Modifier.padding(start = 8.dp, end = 12.dp)
            ) {
                IconButton(onClick = onOpenReport) {
                    Icon(Icons.Outlined.Assessment, contentDescription = stringResource(R.string.attendance_report), tint = Ink)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    AttendanceReportCard(
                        attendance = attendance,
                        month = month,
                        date = date,
                        onPreviousDate = {
                            clearSelection()
                            viewModel.selectDate(date.minusDays(1))
                        },
                        onNextDate = {
                            clearSelection()
                            viewModel.selectDate(date.plusDays(1))
                        },
                        onPickDate = { pickingDate = true },
                        onTodayDate = {
                            clearSelection()
                            viewModel.selectDate(LocalDate.now())
                        },
                        // Once everyone is marked there's no one left to mark present
                        actionLabel = when (unmarkedCount) {
                            0 -> null
                            attendance.size -> stringResource(R.string.all_present)
                            else -> stringResource(R.string.rest_present)
                        },
                        onClick = if (unmarkedCount == 0) null else {
                            {
                                clearSelection()
                                viewModel.markRestPresent()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                if (totalCount > 0) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            CalmSearchField(
                                value = query,
                                onValueChange = { query = it },
                                placeholder = stringResource(R.string.search_employees)
                            )
                            Spacer(Modifier.height(12.dp))
                            StatusFilterChips(
                                options = listOf(
                                    stringResource(R.string.all_d, attendance.size),
                                    stringResource(R.string.present_d, presentCount),
                                    stringResource(R.string.absent_d, absentCount),
                                    stringResource(R.string.unmarked_d, unmarkedCount)
                                ),
                                selectedIndex = filterIndex,
                                onSelect = { filterIndex = it }
                            )
                        }
                    }
                }

                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        AttendanceSheet(
                            attendance = visibleAttendance,
                            totalCount = totalCount,
                            query = query,
                            isMultiSelectMode = isMultiSelectMode,
                            selectedIds = selectedEmployeeIds,
                            onItemClick = { day ->
                                if (isMultiSelectMode) {
                                    val id = day.employee.id
                                    selectedEmployeeIds = if (selectedEmployeeIds.contains(id)) {
                                        selectedEmployeeIds - id
                                    } else {
                                        selectedEmployeeIds + id
                                    }
                                } else {
                                    activeIndividualSheetDay = day
                                }
                            },
                            onItemLongClick = { day ->
                                val id = day.employee.id
                                selectedEmployeeIds = if (selectedEmployeeIds.contains(id)) {
                                    selectedEmployeeIds - id
                                } else {
                                    selectedEmployeeIds + id
                                }
                            },
                            onSelectAllToggle = {
                                val visibleIds = visibleAttendance.map { it.employee.id }.toSet()
                                val allVisibleSelected = visibleAttendance.all { selectedEmployeeIds.contains(it.employee.id) }
                                selectedEmployeeIds = if (allVisibleSelected) {
                                    selectedEmployeeIds - visibleIds
                                } else {
                                    selectedEmployeeIds + visibleIds
                                }
                            },
                            onAddEmployees = onOpenEmployees
                        )
                    }
                }
            }
        }

        // Selection bar for multi-select mode
        if (isMultiSelectMode) {
            SelectionBar(
                selectedCount = selectedEmployeeIds.size,
                onPresent = { bulkSheetMode = AttendanceStatus.PRESENT },
                onAbsent = { bulkSheetMode = AttendanceStatus.ABSENT },
                onClear = ::clearSelection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(20.dp)
            )
        }
    }

    if (pickingDate) {
        AttendanceDatePicker(
            selected = date,
            onPick = {
                clearSelection()
                viewModel.selectDate(it)
                pickingDate = false
            },
            onDismiss = { pickingDate = false }
        )
    }

    ClearFocusWhenClosed(
        open = activeIndividualSheetDay != null || bulkSheetMode != null
    )

    val checkInLabel = stringResource(R.string.check_in_title)
    val checkOutLabel = stringResource(R.string.check_out_title)
    val attendanceLabel = stringResource(R.string.attendance)

    // Individual Attendance Bottom Sheet
    activeIndividualSheetDay?.let { day ->
        IndividualAttendanceBottomSheet(
            day = day,
            date = date,
            onDismiss = { activeIndividualSheetDay = null },
            onSavePresent = { inTime, outTime, overtimeHours, remarks, what ->
                viewModel.saveIndividualPresent(day.employee.id, date, inTime, outTime, overtimeHours, remarks)
                val whatText = when (what) {
                    "Check-in" -> checkInLabel
                    "Check-out" -> checkOutLabel
                    else -> attendanceLabel
                }
                Toast.makeText(context, context.getString(R.string.s_saved_for_s, whatText, day.employee.name), Toast.LENGTH_SHORT).show()
                activeIndividualSheetDay = null
            },
            onSaveAbsent = { reason, remarks ->
                viewModel.saveIndividualAbsent(day.employee.id, date, reason, remarks)
                Toast.makeText(context, context.getString(R.string.absence_saved_for_s, day.employee.name), Toast.LENGTH_SHORT).show()
                activeIndividualSheetDay = null
            }
        )
    }

    val oneEmpStr = stringResource(R.string.one_employee)

    // Bulk sheet: the same Present / Absent steps as one employee, for everyone selected
    val bulkMode = bulkSheetMode
    if (bulkMode != null && selectedEmployeeIds.isNotEmpty()) {
        fun done(what: String, saved: Int, skipped: Int) {
            val people = if (saved == 1) oneEmpStr else context.getString(R.string.d_employees, saved)
            val whatText = when (what) {
                "Check-in" -> checkInLabel
                "Check-out" -> checkOutLabel
                else -> attendanceLabel
            }
            val message = if (skipped > 0) context.getString(R.string.s_saved_for_s_skipped_d_already_marked, whatText, people, skipped) else context.getString(R.string.s_saved_for_s, whatText, people)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            clearSelection()
            bulkSheetMode = null
        }
        BulkAttendanceBottomSheet(
            days = attendance.filter { selectedEmployeeIds.contains(it.employee.id) },
            date = date,
            initialMode = bulkMode,
            onDismiss = { bulkSheetMode = null },
            onSkip = { skipped -> selectedEmployeeIds = selectedEmployeeIds - skipped },
            onSaveCheckIn = { ids, time, remarks, skipped ->
                viewModel.saveBulkCheckIn(ids, date, time, remarks)
                done("Check-in", ids.size, skipped)
            },
            onSaveCheckOut = { ids, time, remarks, replaceIds, skipped ->
                viewModel.saveBulkCheckOut(ids, date, time, remarks, replaceIds)
                done("Check-out", ids.size, skipped)
            },
            onSaveAbsent = { ids, reason, remarks, skipped ->
                viewModel.saveBulkAbsent(ids, date, reason, remarks)
                done("Absence", ids.size, skipped)
            }
        )
    }
}

/**
 * Status filters as chips, each as wide as its label and count, in a row that scrolls sideways when
 * they don't all fit, so "Unmarked (11)" is never cut to "Unmark".
 */
@Composable
private fun StatusFilterChips(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .clip(CircleShape)
                    .background(if (selected) SageDeep else IvoryCard)
                    .border(1.dp, if (selected) SageDeep else SandLight, CircleShape)
                    .selectable(selected = selected, role = Role.Tab) { onSelect(index) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) Color.White else Ink,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/** The count and the bulk actions, floating over the list while choosing several people. */
@Composable
private fun SelectionBar(
    selectedCount: Int,
    onPresent: () -> Unit,
    onAbsent: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Cover,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        // A tablet keeps it a compact bar rather than one stretched across the screen
        modifier = modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
        ) {
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.clear_selection), tint = Color.White)
            }
            // The count over a small "selected", so both buttons keep their full labels beside it
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$selectedCount",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1
                )
                Text(stringResource(R.string.selected_count), style = MaterialTheme.typography.labelSmall, color = OnCoverMuted, maxLines = 1)
            }
            OutlinedButton(
                onClick = onAbsent,
                shape = CircleShape,
                border = BorderStroke(1.dp, OnCoverMuted),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 14.dp),
                modifier = Modifier.heightIn(min = 44.dp)
            ) { Text(stringResource(R.string.absent), maxLines = 1) }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onPresent,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Mist, contentColor = SageInk),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.heightIn(min = 44.dp)
            ) { Text(stringResource(R.string.present), maxLines = 1, fontWeight = FontWeight.SemiBold) }
        }
    }
}

/** One employee's day: Present with a check-in and check-out, or Absent with a reason. */
@Composable
private fun IndividualAttendanceBottomSheet(
    day: DayAttendance,
    date: LocalDate,
    onDismiss: () -> Unit,
    /** [what] names what's being saved, for the confirmation: "Check-in", "Check-out", "Attendance". */
    onSavePresent: (inTime: String, outTime: String?, overtimeHours: Double, remarks: String, what: String) -> Unit,
    onSaveAbsent: (reason: String, remarks: String) -> Unit
) {
    val record = day.record?.takeIf { day.status == AttendanceStatus.PRESENT }
    val savedIn = parseTime(record?.inTime)
    val savedOut = parseTime(record?.outTime)

    var activeMode by remember { mutableStateOf(day.status ?: AttendanceStatus.PRESENT) }
    var inTime by remember { mutableStateOf(savedIn ?: nowToMinute()) }
    // Someone already in is most likely being checked out, so their check-out starts at now
    var outTime by remember { mutableStateOf(savedOut ?: if (savedIn != null) nowToMinute() else null) }
    var overtimeHours by remember { mutableStateOf(record?.overtimeHours?.toInt() ?: 0) }
    var presentRemarks by remember { mutableStateOf(record?.remarks.orEmpty()) }
    var clockFor by remember { mutableStateOf<ShiftTime?>(null) }

    val absentRecord = day.record?.takeIf { day.status == AttendanceStatus.ABSENT }
    val context = LocalContext.current
    val savedReasonRes = absentRecord?.absentReason?.let { reasonText ->
        absenceReasons.firstOrNull { reasonRes ->
            reasonText == context.getString(reasonRes)
        }
    } ?: absenceReasons.first()
    var reasonRes by remember { mutableStateOf(savedReasonRes) }
    var customReason by remember { mutableStateOf(if (savedReasonRes == R.string.other) absentRecord?.absentReason.orEmpty() else "") }
    var absentRemarks by remember { mutableStateOf(absentRecord?.remarks.orEmpty()) }

    val otherReasonStr = stringResource(R.string.other)
    val chosenReasonText = if (reasonRes == R.string.other) customReason.ifBlank { otherReasonStr } else stringResource(reasonRes)

    AttendancePanel(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            SheetHeader(
                title = day.employee.name,
                date = date,
                leading = { InitialsAvatar(name = day.employee.name, size = 48.dp, showDot = false) },
                trailing = { AttendanceStatusPill(day) }
            )

            Spacer(Modifier.height(20.dp))
            CalmSegmented(
                options = listOf(stringResource(R.string.present), stringResource(R.string.absent)),
                selectedIndex = if (activeMode == AttendanceStatus.PRESENT) 0 else 1,
                onSelect = { activeMode = if (it == 0) AttendanceStatus.PRESENT else AttendanceStatus.ABSENT }
            )
            Spacer(Modifier.height(20.dp))

            if (activeMode == AttendanceStatus.PRESENT) {
                val out = outTime
                TimeCard(
                    which = ShiftTime.CHECK_IN,
                    time = inTime,
                    onPick = { clockFor = ShiftTime.CHECK_IN }
                )
                Spacer(Modifier.height(10.dp))
                TimeCard(
                    which = ShiftTime.CHECK_OUT,
                    time = out,
                    onPick = { clockFor = ShiftTime.CHECK_OUT },
                    note = shiftLength(inTime, out)?.let { stringResource(R.string.s_on_site, it) },
                    onClear = {
                        outTime = null
                        overtimeHours = 0
                    }
                )
                if (out != null) {
                    Spacer(Modifier.height(10.dp))
                    OvertimeCard(
                        hours = overtimeHours,
                        onHoursChange = { overtimeHours = it }
                    )
                }
                if (out != null && !out.isAfter(inTime)) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.checkout_before_checkin_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed
                    )
                }
                Spacer(Modifier.height(8.dp))
                OptionalRemarks(presentRemarks, { presentRemarks = it }, stringResource(R.string.eg_on_time_site_duty))

                // Name the button for what changes, so "Save check-out" means only that
                val what = when {
                    savedIn == null -> if (out == null) "Check-in" else "Attendance"
                    savedOut == null -> if (out != null && inTime == savedIn) "Check-out" else if (out == null) "Check-in" else "Attendance"
                    else -> "Attendance"
                }
                val updateCheckInStr = stringResource(R.string.update_check_in)
                val updateAttendanceStr = stringResource(R.string.update_attendance)
                val saveCheckInStr = stringResource(R.string.save_check_in)
                val saveCheckOutStr = stringResource(R.string.save_check_out)
                val saveAttendanceStr = stringResource(R.string.save_attendance)
                val saveLabelText = if (savedIn != null && what != "Check-out") {
                    if (what == "Check-in") updateCheckInStr else updateAttendanceStr
                } else {
                    when (what) {
                        "Check-in" -> saveCheckInStr
                        "Check-out" -> saveCheckOutStr
                        else -> saveAttendanceStr
                    }
                }
                Spacer(Modifier.height(16.dp))
                SheetActions(
                    saveLabel = saveLabelText,
                    onCancel = onDismiss,
                    onSave = { onSavePresent(inTime.label(), out?.label(), if (out != null) overtimeHours.toDouble() else 0.0, presentRemarks, what) }
                )
            } else {
                AbsenceFields(
                    reasonRes = reasonRes,
                    onReason = { reasonRes = it },
                    customReason = customReason,
                    onCustomReason = { customReason = it },
                    remarks = absentRemarks,
                    onRemarks = { absentRemarks = it }
                )
                Spacer(Modifier.height(16.dp))
                SheetActions(
                    saveLabel = stringResource(R.string.save_absence),
                    onCancel = onDismiss,
                    saveColor = ErrorRed,
                    onSave = {
                        onSaveAbsent(chosenReasonText, absentRemarks)
                    }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    clockFor?.let { which ->
        ClockDialog(
            which = which,
            initial = if (which == ShiftTime.CHECK_IN) inTime else outTime ?: nowToMinute(),
            onConfirm = {
                if (which == ShiftTime.CHECK_IN) inTime = it else outTime = it
                clockFor = null
            },
            onDismiss = { clockFor = null }
        )
    }
}

/** What the bulk sheet is about to save, so the overwrite question can name it. */
private enum class BulkAction(@param:StringRes val titleRes: Int) {
    CHECK_IN(R.string.check_in_title),
    CHECK_OUT(R.string.check_out_title),
    ABSENT(R.string.absence_title)
}

/**
 * Who already has something [action] would replace: any mark at all for a check-in or an absence;
 * for a check-out, an earlier check-out or an absence (a check-in alone is the normal case).
 */
private fun DayAttendance.clashesWith(action: BulkAction): Boolean = when (action) {
    BulkAction.CHECK_IN, BulkAction.ABSENT -> status != null
    BulkAction.CHECK_OUT -> status == AttendanceStatus.ABSENT || !record?.outTime.isNullOrBlank()
}

/**
 * Marks several employees at once with one choice (check-in, check-out or absent) and one time or
 * reason. If anyone selected is already marked, a dialog asks whether to overwrite their record or
 * skip them, which takes them out of the selection.
 */
@Composable
private fun BulkAttendanceBottomSheet(
    days: List<DayAttendance>,
    date: LocalDate,
    initialMode: AttendanceStatus,
    onDismiss: () -> Unit,
    onSkip: (ids: Set<String>) -> Unit,
    onSaveCheckIn: (ids: Set<String>, time: String, remarks: String, skipped: Int) -> Unit,
    onSaveCheckOut: (ids: Set<String>, time: String, remarks: String, replaceIds: Set<String>, skipped: Int) -> Unit,
    onSaveAbsent: (ids: Set<String>, reason: String, remarks: String, skipped: Int) -> Unit
) {
    val checkedIn = days.count { it.status == AttendanceStatus.PRESENT && !it.record?.inTime.isNullOrBlank() }
    val checkedOut = days.count { it.status == AttendanceStatus.PRESENT && !it.record?.outTime.isNullOrBlank() }
    val absent = days.count { it.status == AttendanceStatus.ABSENT }
    // Once everyone has checked in, the next step is checking out
    var action by remember {
        mutableStateOf(
            when {
                initialMode == AttendanceStatus.ABSENT -> BulkAction.ABSENT
                checkedIn == days.size -> BulkAction.CHECK_OUT
                else -> BulkAction.CHECK_IN
            }
        )
    }
    var time by remember { mutableStateOf(nowToMinute()) }
    var clockOpen by remember { mutableStateOf(false) }
    var presentRemarks by remember { mutableStateOf("") }
    var reasonRes by remember { mutableStateOf(absenceReasons.first()) }
    var customReason by remember { mutableStateOf("") }
    var absentRemarks by remember { mutableStateOf("") }
    // The action waiting on the overwrite question
    var pending by remember { mutableStateOf<BulkAction?>(null) }

    val otherReasonStr = stringResource(R.string.other)
    val finalReason = if (reasonRes == R.string.other) customReason.ifBlank { otherReasonStr } else stringResource(reasonRes)

    fun save(action: BulkAction, ids: Set<String>, replaceIds: Set<String>, skipped: Int) {
        if (ids.isEmpty()) {
            onDismiss()
            return
        }
        when (action) {
            BulkAction.CHECK_IN -> onSaveCheckIn(ids, time.label(), presentRemarks, skipped)
            BulkAction.CHECK_OUT -> onSaveCheckOut(ids, time.label(), presentRemarks, replaceIds, skipped)
            BulkAction.ABSENT -> onSaveAbsent(ids, finalReason, absentRemarks, skipped)
        }
    }

    fun attempt(action: BulkAction) {
        if (days.none { it.clashesWith(action) }) save(action, days.map { it.employee.id }.toSet(), emptySet(), 0)
        else pending = action
    }

    AttendancePanel(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            SheetHeader(
                title = if (days.size == 1) stringResource(R.string.one_employee) else stringResource(R.string.d_employees, days.size),
                date = date,
                leading = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Mist, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${days.size}", style = MaterialTheme.typography.titleMedium, color = SageInk)
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            val names = days.map { it.employee.name }
            val namesText = names.take(3).joinToString(", ") + if (names.size > 3) stringResource(R.string.and_d_more, names.size - 3) else ""
            Text(
                namesText,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val standing = listOfNotNull(
                if (checkedIn > 0) stringResource(R.string.d_checked_in, checkedIn) else null,
                if (checkedOut > 0) stringResource(R.string.d_checked_out, checkedOut) else null,
                if (absent > 0) stringResource(R.string.d_absent, absent) else null
            )
            if (standing.isNotEmpty()) {
                Text(
                    stringResource(R.string.already_prefix) + standing.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted
                )
            }

            Spacer(Modifier.height(20.dp))
            CalmSegmented(
                options = BulkAction.entries.map { stringResource(it.titleRes) },
                selectedIndex = action.ordinal,
                onSelect = { action = BulkAction.entries[it] }
            )
            Spacer(Modifier.height(20.dp))

            if (action != BulkAction.ABSENT) {
                val which = if (action == BulkAction.CHECK_IN) ShiftTime.CHECK_IN else ShiftTime.CHECK_OUT
                TimeCard(which = which, time = time, onPick = { clockOpen = true })
                Spacer(Modifier.height(8.dp))
                OptionalRemarks(presentRemarks, { presentRemarks = it }, stringResource(R.string.eg_on_time_site_duty))
                Spacer(Modifier.height(16.dp))
                SheetActions(
                    saveLabel = if (action == BulkAction.CHECK_IN) stringResource(R.string.check_in_d, days.size) else stringResource(R.string.check_out_d, days.size),
                    onCancel = onDismiss,
                    onSave = { attempt(action) }
                )
            } else {
                AbsenceFields(
                    reasonRes = reasonRes,
                    onReason = { reasonRes = it },
                    customReason = customReason,
                    onCustomReason = { customReason = it },
                    remarks = absentRemarks,
                    onRemarks = { absentRemarks = it }
                )
                Spacer(Modifier.height(16.dp))
                SheetActions(
                    saveLabel = stringResource(R.string.mark_d_absent, days.size),
                    onCancel = onDismiss,
                    saveColor = ErrorRed,
                    onSave = { attempt(BulkAction.ABSENT) }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (clockOpen) {
        ClockDialog(
            which = if (action == BulkAction.CHECK_OUT) ShiftTime.CHECK_OUT else ShiftTime.CHECK_IN,
            initial = time,
            onConfirm = {
                time = it
                clockOpen = false
            },
            onDismiss = { clockOpen = false }
        )
    }

    pending?.let { pendingAction ->
        val clashing = days.filter { it.clashesWith(pendingAction) }
        val clashIds = clashing.map { it.employee.id }.toSet()
        val allIds = days.map { it.employee.id }.toSet()
        val alreadyText = if (pendingAction == BulkAction.CHECK_OUT) stringResource(R.string.already_checked_out_or_absent) else stringResource(R.string.already_marked)
        val replacesText = when (pendingAction) {
            BulkAction.CHECK_IN -> stringResource(R.string.overwrite_checkin_explanation, time.label())
            BulkAction.CHECK_OUT -> stringResource(R.string.overwrite_checkout_explanation, time.label())
            BulkAction.ABSENT -> stringResource(R.string.overwrite_absent_explanation, finalReason)
        }
        OverwriteDialog(
            date = date,
            already = alreadyText,
            clashing = clashing,
            othersCount = days.size - clashing.size,
            replaces = replacesText,
            onOverwrite = {
                pending = null
                save(pendingAction, allIds, clashIds, 0)
            },
            onSkip = {
                pending = null
                onSkip(clashIds)
                save(pendingAction, allIds - clashIds, emptySet(), clashIds.size)
            },
            onCancel = { pending = null }
        )
    }
}

/**
 * Some of the selected employees are already marked: list who and what they have, then overwrite
 * their records, skip them (unselect them and save for the rest), or go back to the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverwriteDialog(
    date: LocalDate,
    /** What they already are, for the title: "already marked". */
    already: String,
    clashing: List<DayAttendance>,
    othersCount: Int,
    replaces: String,
    onOverwrite: () -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit
) {
    val count = clashing.size
    val day = if (date == LocalDate.now()) stringResource(R.string.today_lowercase) else stringResource(R.string.on_s, date.format(DateTimeFormatter.ofPattern("d MMM", Language.locale)))
    BasicAlertDialog(onDismissRequest = onCancel) {
        Surface(color = IvoryCard, shape = RoundedCornerShape(28.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    if (count == 1) stringResource(R.string.one_employee_is_s_s, already, day) else stringResource(R.string.d_employees_are_s_s, count, already, day),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .heightIn(max = 220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SandLight.copy(alpha = 0.5f))
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    clashing.forEach { d ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                            InitialsAvatar(name = d.employee.name, size = 32.dp, showDot = false)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    d.employee.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    d.markedAs(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (d.status == AttendanceStatus.ABSENT) ErrorRed else SageDeep,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(replaces, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
                Spacer(Modifier.height(20.dp))
                // Stacked full-width buttons, so longer labels never get cut off
                Button(
                    onClick = onOverwrite,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SageDeep),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) { Text(if (count == 1) stringResource(R.string.overwrite) else stringResource(R.string.overwrite_all_d, count), maxLines = 1) }
                if (othersCount > 0) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onSkip,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, SageDeep),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SageDeep),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            if (count == 1) stringResource(R.string.skip_s_save_for_d, clashing.first().employee.name, othersCount) else stringResource(R.string.skip_these_d_save_for_d, count, othersCount),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) { Text(stringResource(R.string.cancel), color = SageDeep, maxLines = 1) }
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
            ) { Text(stringResource(R.string.show)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = SageDeep) }
        }
    ) {
        DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = IvoryCard))
    }
}

/** Multi-select employee list with checkboxes and status pills. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AttendanceSheet(
    attendance: List<DayAttendance>,
    totalCount: Int,
    query: String,
    isMultiSelectMode: Boolean,
    selectedIds: Set<String>,
    onItemClick: (DayAttendance) -> Unit,
    onItemLongClick: (DayAttendance) -> Unit,
    onSelectAllToggle: () -> Unit,
    onAddEmployees: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        if (totalCount == 0) {
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
                    Text(stringResource(R.string.add_your_team), style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text(
                        stringResource(R.string.add_employees_to_mark_their_attendance_here),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted
                    )
                }
            }
            return@SoftCard
        }

        if (attendance.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = stringResource(R.string.no_employees_found),
                message = if (query.isNotEmpty()) stringResource(R.string.no_employee_matches_s, query.trim()) else stringResource(R.string.no_employees_in_this_status_filter),
                modifier = Modifier.padding(vertical = 24.dp)
            )
            return@SoftCard
        }

        val allVisibleSelected = attendance.all { selectedIds.contains(it.employee.id) }

        Column {
            // Select All header row - permanently visible
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectAllToggle() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (isMultiSelectMode) {
                    Checkbox(
                        checked = allVisibleSelected,
                        onCheckedChange = { onSelectAllToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SageDeep,
                            uncheckedColor = InkMuted
                        )
                    )
                } else {
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (allVisibleSelected) stringResource(R.string.deselect_all_d, attendance.size) else stringResource(R.string.select_all_d, attendance.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = SageDeep,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(color = SandLight)

            attendance.forEachIndexed { index, day ->
                if (index > 0) HorizontalDivider(color = SandLight, modifier = Modifier.padding(start = 72.dp))
                val isSelected = selectedIds.contains(day.employee.id)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) Mist.copy(alpha = 0.6f) else Color.Transparent)
                        .combinedClickable(
                            onClick = { onItemClick(day) },
                            onLongClick = { onItemLongClick(day) }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // In select mode the checkbox takes the avatar's place, so the row keeps its width
                    Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        if (isMultiSelectMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onItemClick(day) },
                                colors = CheckboxDefaults.colors(checkedColor = SageDeep, uncheckedColor = InkMuted)
                            )
                        } else {
                            InitialsAvatar(name = day.employee.name, size = 42.dp, showDot = false)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            day.employee.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // One line on where they stand: times, the absence reason, or their trade
                        val (detail, color) = when (day.status) {
                            AttendanceStatus.PRESENT -> day.markedAs() to SageDeep
                            AttendanceStatus.ABSENT -> day.record?.absentReason.orEmpty().ifEmpty { stringResource(R.string.absent) } to ErrorRed
                            null -> day.employee.designation.ifBlank { day.employee.department }.ifBlank { stringResource(R.string.tap_to_mark) } to InkMuted
                        }
                        Text(
                            detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    AttendanceStatusPill(day)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

/** Completed, Present, Absent or Not Marked for one employee's day. */
@Composable
private fun AttendanceStatusPill(day: DayAttendance) {
    when (day.status) {
        AttendanceStatus.PRESENT -> StatusPill(
            text = if (!day.record?.outTime.isNullOrBlank()) stringResource(R.string.completed) else stringResource(R.string.present),
            container = Mist,
            content = SageDeep
        )
        AttendanceStatus.ABSENT -> StatusPill(
            text = stringResource(R.string.absent),
            container = ErrorRed.copy(alpha = 0.12f),
            content = ErrorRed
        )
        null -> StatusPill(
            text = stringResource(R.string.not_marked_capital),
            container = SandLight,
            content = InkMuted
        )
    }
}
