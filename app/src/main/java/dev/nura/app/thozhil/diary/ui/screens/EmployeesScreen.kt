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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                title = "Employees",
                subtitle = when (employees.size) {
                    0 -> "Your team list"
                    1 -> "1 employee"
                    else -> "${employees.size} employees"
                },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (employees.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Groups,
                    title = "No employees yet",
                    message = "Add the people who work with you to start marking attendance."
                ) {
                    Button(onClick = { openForm(null) }, shape = CircleShape, modifier = Modifier.height(52.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add employee")
                    }
                }
                return@Column
            }

            CalmSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search by name or mobile",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (visible.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = "No matches",
                    message = "No employee matches \"${query.trim()}\"."
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
                    .height(56.dp)
                    .shadow(10.dp, CircleShape)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add employee", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    ClearFocusWhenClosed(open = showForm || deleting != null)

    if (showForm) {
        EmployeeFormDialog(
            existing = editing,
            onDismiss = { showForm = false },
            onSave = {
                viewModel.save(it)
                showForm = false
            }
        )
    }

    deleting?.let { employee ->
        ConfirmDialog(
            title = "Delete ${employee.name}?",
            message = "Their attendance records will also be deleted. This can't be undone.",
            confirmLabel = "Delete",
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
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = InkMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        employee.mobile.ifEmpty { "No mobile number" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted
                    )
                }
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreHoriz, contentDescription = "Options for ${employee.name}", tint = Ink)
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    containerColor = IvoryCard
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = ErrorRed) },
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
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var mobile by remember { mutableStateOf(existing?.mobile.orEmpty()) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }

    fun save() {
        nameError = if (name.isBlank()) "Please enter a name" else null
        mobileError = if (mobile.isNotEmpty() && mobile.length != 10) "Enter a 10-digit mobile number" else null
        if (nameError == null && mobileError == null) {
            val trimmed = name.trim()
            onSave(existing?.copy(name = trimmed, mobile = mobile) ?: Employee(name = trimmed, mobile = mobile))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IvoryCard,
        title = { Text(if (existing == null) "Add employee" else "Edit employee", color = Ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Name") },
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
                    label = { Text("Mobile number") },
                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                    isError = mobileError != null,
                    supportingText = { Text(mobileError ?: "Optional") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { save() }, shape = CircleShape) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SageDeep) }
        }
    )
}
