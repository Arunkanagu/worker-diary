package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.data.Employee
import dev.nura.app.thozhil.diary.ui.EmployeeViewModel
import dev.nura.app.thozhil.diary.ui.components.CalmSearchField
import dev.nura.app.thozhil.diary.ui.components.ClearFocusWhenClosed
import dev.nura.app.thozhil.diary.ui.components.ConfirmDialog
import dev.nura.app.thozhil.diary.ui.components.EmptyState
import dev.nura.app.thozhil.diary.ui.components.InitialsAvatar
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.SageDeep

@Composable
fun EmployeesScreen(viewModel: EmployeeViewModel = hiltViewModel()) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var editing by remember { mutableStateOf<Employee?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Employee?>(null) }

    val visible = employees.filter {
        it.name.contains(query.trim(), ignoreCase = true) || it.mobile.contains(query.trim())
    }

    fun openForm(employee: Employee?) {
        editing = employee
        showForm = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(R.string.employees),
                subtitle = when (employees.size) {
                    0 -> stringResource(R.string.your_team_list)
                    1 -> stringResource(R.string.one_employee)
                    else -> stringResource(R.string.d_employees, employees.size)
                },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (employees.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Groups,
                    title = stringResource(R.string.no_employees_yet),
                    message = stringResource(R.string.add_the_people_who_work_with_you_to_start_marking_attendance)
                ) {
                    Button(onClick = { openForm(null) }, shape = CircleShape, modifier = Modifier.heightIn(min = 52.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.add_employee))
                    }
                }
                return@Column
            }

            CalmSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(R.string.search_by_name_or_mobile),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (visible.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.no_matches),
                    message = stringResource(R.string.no_employee_matches_s, query.trim())
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visible, key = { it.id }) { employee ->
                        EmployeeCard(
                            employee = employee,
                            onEdit = { openForm(employee) },
                            onDelete = { deleting = employee }
                        )
                    }
                }
            }
        }

        if (employees.isNotEmpty()) {
            Button(
                onClick = { openForm(null) },
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .heightIn(min = 56.dp)
                    .shadow(10.dp, CircleShape)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_employee), style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    ClearFocusWhenClosed(open = showForm || deleting != null)

    if (showForm) {
        EmployeeFormDialog(
            existing = editing,
            // Two people can't share a number: attendance and calls would go to the wrong one
            mobileTakenBy = { mobile -> employees.firstOrNull { it.mobile == mobile && it.id != editing?.id }?.name },
            onDismiss = { showForm = false },
            onSave = {
                viewModel.save(it)
                showForm = false
            }
        )
    }

    deleting?.let { employee ->
        ConfirmDialog(
            title = stringResource(R.string.delete_s, employee.name),
            message = stringResource(R.string.their_attendance_records_will_also_be_deleted_this_can_t_be_undone),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                viewModel.delete(employee)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
private fun EmployeeCard(employee: Employee, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialsAvatar(name = employee.name, size = 48.dp, showDot = false)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    employee.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val role = listOf(employee.designation, employee.department).filter { it.isNotBlank() }.joinToString(" · ")
                if (role.isNotEmpty()) {
                    Text(role, style = MaterialTheme.typography.bodyMedium, color = SageDeep, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = InkMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        employee.mobile.ifEmpty { stringResource(R.string.no_mobile_number) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted
                    )
                }
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreHoriz, contentDescription = stringResource(R.string.options_for_s, employee.name), tint = Ink)
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    containerColor = IvoryCard
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete), color = ErrorRed) },
                        leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = ErrorRed) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmployeeFormDialog(
    existing: Employee?,
    mobileTakenBy: (String) -> String?,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var mobile by remember { mutableStateOf(existing?.mobile.orEmpty()) }
    var designation by remember { mutableStateOf(existing?.designation.orEmpty()) }
    var department by remember { mutableStateOf(existing?.department.orEmpty()) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }

    val nameRequiredMsg = stringResource(R.string.please_enter_a_name)
    val invalidMobileMsg = stringResource(R.string.enter_a_10_digit_mobile_number)
    val takenBy = mobile.takeIf { it.length == 10 }?.let(mobileTakenBy)
    val mobileTakenMsg = takenBy?.let { stringResource(R.string.mobile_already_used_by_s, it) }

    fun save() {
        nameError = if (name.isBlank()) nameRequiredMsg else null
        mobileError = when {
            mobile.isNotEmpty() && mobile.length != 10 -> invalidMobileMsg
            else -> mobileTakenMsg
        }
        if (nameError == null && mobileError == null) {
            val trimmed = name.trim()
            val role = designation.trim()
            val team = department.trim()
            onSave(
                existing?.copy(name = trimmed, mobile = mobile, designation = role, department = team)
                    ?: Employee(name = trimmed, mobile = mobile, designation = role, department = team)
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IvoryCard,
        title = { Text(if (existing == null) stringResource(R.string.add_employee) else stringResource(R.string.edit_employee), color = Ink) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text(stringResource(R.string.name)) },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { input ->
                        mobile = input.filter { it.isDigit() }.take(10)
                        mobileError = null
                    },
                    label = { Text(stringResource(R.string.mobile_number)) },
                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                    isError = mobileError != null,
                    supportingText = { Text(mobileError ?: stringResource(R.string.optional)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it.take(40) },
                    label = { Text(stringResource(R.string.designation)) },
                    placeholder = { Text(stringResource(R.string.designation_example)) },
                    leadingIcon = { Icon(Icons.Outlined.Handyman, contentDescription = null) },
                    supportingText = { Text(stringResource(R.string.optional)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it.take(40) },
                    label = { Text(stringResource(R.string.department)) },
                    placeholder = { Text(stringResource(R.string.department_example)) },
                    leadingIcon = { Icon(Icons.Outlined.Workspaces, contentDescription = null) },
                    supportingText = { Text(stringResource(R.string.optional)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { save() }, shape = CircleShape) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = SageDeep) }
        }
    )
}
