package dev.nura.app.thozhil.diary.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.nura.app.thozhil.diary.R
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import dev.nura.app.thozhil.diary.ui.components.AuthButton
import dev.nura.app.thozhil.diary.ui.components.AuthScaffold
import dev.nura.app.thozhil.diary.ui.components.AuthTextField
import dev.nura.app.thozhil.diary.ui.components.PasswordField
import dev.nura.app.thozhil.diary.ui.components.PasswordRule
import dev.nura.app.thozhil.diary.ui.components.PasswordRules

/**
 * Two steps: find the account by its mobile number or email, then set a new password.
 * The account lives only on this phone, so there is no code sent out to confirm it.
 */
@Composable
fun ForgotPasswordScreen(
    findAccount: suspend (identifier: String) -> Boolean,
    /** Called once the new password is saved; returns to log in. */
    onReset: (identifier: String, newPassword: String) -> Unit,
    onBack: () -> Unit
) {
    var identifier by rememberSaveable { mutableStateOf("") }
    var found by rememberSaveable { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    var identifierError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmError by rememberSaveable { mutableStateOf<String?>(null) }

    val identifierRequired = stringResource(R.string.enter_mobile_or_email)
    val noAccount = stringResource(R.string.no_account_found)
    val passwordWeak = stringResource(R.string.password_does_not_meet_rules)
    val passwordMismatch = stringResource(R.string.passwords_do_not_match)

    val scope = rememberCoroutineScope()

    fun findIt() {
        if (identifier.isBlank()) {
            identifierError = identifierRequired
            return
        }
        scope.launch {
            identifierError = if (findAccount(identifier.trim())) null else noAccount
            found = identifierError == null
        }
    }

    fun reset() {
        passwordError = if (!PasswordRule.allPass(password)) passwordWeak else null
        confirmError = if (confirm != password) passwordMismatch else null
        if (passwordError == null && confirmError == null) onReset(identifier.trim(), password)
    }

    BackHandler(enabled = found) { found = false }

    AuthScaffold(
        title = stringResource(R.string.reset_password),
        description = stringResource(
            if (found) R.string.reset_password_new_description else R.string.reset_password_find_description
        ),
        // On the second step, back returns to the first instead of leaving
        onBack = { if (found) found = false else onBack() }
    ) {
        if (!found) {
            AuthTextField(
                label = stringResource(R.string.mobile_or_email),
                value = identifier,
                onValueChange = { identifier = it; identifierError = null },
                placeholder = stringResource(R.string.mobile_or_email_placeholder),
                leadingIcon = Icons.Outlined.AccountCircle,
                error = identifierError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                onDone = ::findIt
            )
            Spacer(Modifier.height(20.dp))
            AuthButton(text = stringResource(R.string.continue_button), onClick = ::findIt)
        } else {
            Text(
                stringResource(R.string.resetting_for_s, identifier.trim()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            PasswordField(
                label = stringResource(R.string.new_password),
                value = password,
                onValueChange = { password = it; passwordError = null },
                placeholder = stringResource(R.string.create_a_password),
                error = passwordError
            )
            PasswordRules(password = password, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
            Spacer(Modifier.height(16.dp))
            PasswordField(
                label = stringResource(R.string.confirm_password),
                value = confirm,
                onValueChange = { confirm = it; confirmError = null },
                placeholder = stringResource(R.string.type_password_again),
                error = confirmError,
                imeAction = ImeAction.Done,
                onDone = ::reset
            )
            Spacer(Modifier.height(20.dp))
            AuthButton(text = stringResource(R.string.save_new_password), onClick = ::reset)
        }
    }
}
