package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.nura.app.thozhil.diary.ui.components.InitialsAvatar
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.SearchOff
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.data.Site
import dev.nura.app.thozhil.diary.data.SiteContact
import dev.nura.app.thozhil.diary.data.SiteWithContacts
import dev.nura.app.thozhil.diary.ui.SiteViewModel
import dev.nura.app.thozhil.diary.ui.components.CalmSearchField
import dev.nura.app.thozhil.diary.ui.components.ClearFocusWhenClosed
import dev.nura.app.thozhil.diary.ui.components.ConfirmDialog
import dev.nura.app.thozhil.diary.ui.components.EmptyState
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.Mist
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SageInk

@Composable
fun SitesScreen(
    onOpenSite: (Long) -> Unit,
    viewModel: SiteViewModel = hiltViewModel()
) {
    val sites by viewModel.sites.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var editing by remember { mutableStateOf<SiteWithContacts?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Site?>(null) }

    val visible = sites.filter { item ->
        val q = query.trim()
        item.site.name.contains(q, ignoreCase = true) ||
            item.contacts.any { it.name.contains(q, ignoreCase = true) || it.mobile.contains(q) }
    }

    fun openForm(site: SiteWithContacts?) {
        editing = site
        showForm = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Sites",
                subtitle = when (sites.size) {
                    0 -> "Your work sites"
                    1 -> "1 site"
                    else -> "${sites.size} sites"
                },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (sites.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Apartment,
                    title = "No sites yet",
                    message = "Add a site or building with its contact people to keep notes."
                ) {
                    Button(onClick = { openForm(null) }, shape = CircleShape, modifier = Modifier.height(52.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add site")
                    }
                }
                return@Column
            }

            CalmSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search site or contact",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (visible.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = "No matches",
                    message = "No site matches \"${query.trim()}\"."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visible, key = { it.site.id }) { item ->
                        SiteCard(
                            item = item,
                            onOpen = { onOpenSite(item.site.id) },
                            onEdit = { openForm(item) },
                            onDelete = { deleting = item.site }
                        )
                    }
                }
            }
        }

        if (sites.isNotEmpty()) {
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
                Text("Add site", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    ClearFocusWhenClosed(open = showForm || deleting != null)

    if (showForm) {
        SiteFormSheet(
            existing = editing,
            onDismiss = { showForm = false },
            onSave = { site, contacts ->
                viewModel.save(site, contacts)
                showForm = false
            }
        )
    }

    deleting?.let { site ->
        ConfirmDialog(
            title = "Delete ${site.name}?",
            message = "Its contacts and notes will also be deleted. This can't be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.delete(site)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }
}

/** Mist tile with a building icon, used as the site's avatar. */
@Composable
fun SiteBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(Mist, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Outlined.Apartment, contentDescription = null, tint = SageInk, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun SiteCard(
    item: SiteWithContacts,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(role = Role.Button, onClickLabel = "Open ${item.site.name}", onClick = onOpen)
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SiteBadge()
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.site.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = InkMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        contactSummary(item.contacts),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreHoriz, contentDescription = "Options for ${item.site.name}", tint = Ink)
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

private fun contactSummary(contacts: List<SiteContact>): String = when (contacts.size) {
    0 -> "No contacts"
    1 -> contacts[0].name
    else -> "${contacts[0].name} +${contacts.size - 1} more"
}

/** A contact added in the form, before the site is saved. */
private data class ContactDraft(val name: String, val mobile: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SiteFormSheet(
    existing: SiteWithContacts?,
    onDismiss: () -> Unit,
    onSave: (Site, List<SiteContact>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(existing?.site?.name.orEmpty()) }
    var nameError by remember { mutableStateOf<String?>(null) }
    val contacts = remember {
        mutableStateListOf<ContactDraft>().apply {
            existing?.contacts?.forEach { add(ContactDraft(it.name, it.mobile)) }
        }
    }
    // The contact being typed, not yet in the list
    var contactName by remember { mutableStateOf("") }
    var contactMobile by remember { mutableStateOf("") }
    var contactNameError by remember { mutableStateOf<String?>(null) }
    var contactMobileError by remember { mutableStateOf<String?>(null) }

    // Animate the sheet away before removing it
    fun close(then: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
    }

    /** Validates the typed contact and moves it into the list; false when it has errors. */
    fun addTypedContact(): Boolean {
        contactNameError = if (contactName.isBlank()) "Please enter a name" else null
        contactMobileError =
            if (contactMobile.isNotEmpty() && contactMobile.length != 10) "Enter a 10-digit mobile number" else null
        if (contactNameError != null || contactMobileError != null) return false
        contacts.add(ContactDraft(contactName.trim(), contactMobile))
        contactName = ""
        contactMobile = ""
        return true
    }

    fun save() {
        nameError = if (name.isBlank()) "Please enter a site name" else null
        // A contact still in the fields is saved too, so typing it is never lost
        val typedOk = contactName.isBlank() && contactMobile.isEmpty() || addTypedContact()
        if (nameError == null && typedOk) {
            val trimmed = name.trim()
            close {
                onSave(
                    existing?.site?.copy(name = trimmed) ?: Site(name = trimmed),
                    contacts.map { SiteContact(name = it.name, mobile = it.mobile) }
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IvoryCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
        ) {
            Text(
                if (existing == null) "New site" else "Edit site",
                style = MaterialTheme.typography.titleLarge,
                color = Ink
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Site / building name") },
                leadingIcon = { Icon(Icons.Outlined.Apartment, contentDescription = null) },
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

            Spacer(Modifier.height(12.dp))
            Text(
                if (contacts.isEmpty()) "Contact members" else "Contact members (${contacts.size})",
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(8.dp))

            contacts.forEachIndexed { index, contact ->
                ContactChipCard(
                    contact = contact,
                    onEdit = {
                        // Pull it back into the fields; anything already typed goes into the list first
                        if (contactName.isNotBlank() && !addTypedContact()) return@ContactChipCard
                        contacts.removeAt(index)
                        contactName = contact.name
                        contactMobile = contact.mobile
                    },
                    onRemove = { contacts.removeAt(index) }
                )
                Spacer(Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = contactName,
                onValueChange = {
                    contactName = it
                    contactNameError = null
                },
                label = { Text("Contact name") },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                isError = contactNameError != null,
                supportingText = contactNameError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = contactMobile,
                onValueChange = { input ->
                    contactMobile = input.filter { it.isDigit() }.take(10)
                    contactMobileError = null
                },
                label = { Text("Mobile number") },
                leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                isError = contactMobileError != null,
                supportingText = { Text(contactMobileError ?: "Optional") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { addTypedContact() }),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                onClick = { addTypedContact() },
                shape = CircleShape,
                border = BorderStroke(1.dp, SageDeep),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Outlined.PersonAddAlt, contentDescription = null, tint = SageDeep)
                Spacer(Modifier.width(8.dp))
                Text("Add contact", color = SageDeep)
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { close(onDismiss) }) { Text("Cancel", color = SageDeep) }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { save() }, shape = CircleShape, modifier = Modifier.height(48.dp)) {
                    Text(if (existing == null) "Add site" else "Save")
                }
            }
        }
    }
}

/** Small card for a contact already added to the form: tap to change it, × to remove. */
@Composable
private fun ContactChipCard(contact: ContactDraft, onEdit: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(Mist.copy(alpha = 0.6f))
            .clickable(role = Role.Button, onClickLabel = "Change ${contact.name}", onClick = onEdit)
            .padding(start = 10.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InitialsAvatar(name = contact.name, size = 34.dp, showDot = false)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                contact.name,
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                contact.mobile.ifEmpty { "No mobile number" },
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Outlined.Close, contentDescription = "Remove ${contact.name}", tint = InkMuted)
        }
    }
}
