package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import dev.nura.app.thozhil.diary.ui.components.AuthSwitchLine
import dev.nura.app.thozhil.diary.ui.components.AuthTextField
import dev.nura.app.thozhil.diary.ui.components.CoverLanguageSwitch
import dev.nura.app.thozhil.diary.ui.components.PasswordField
import dev.nura.app.thozhil.diary.util.AppLanguage
import dev.nura.app.thozhil.diary.util.Language

@Composable
fun LoginScreen(
    /** Returns false when the details don't match the account on this phone. */
    onLogin: suspend (identifier: String, password: String) -> Boolean,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit
) {
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var identifierError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    val identifierRequired = stringResource(R.string.enter_mobile_or_email)
    val passwordRequired = stringResource(R.string.enter_your_password)
    val wrongDetails = stringResource(R.string.wrong_login_details)

    val scope = rememberCoroutineScope()

    fun submit() {
        identifierError = if (identifier.isBlank()) identifierRequired else null
        passwordError = if (password.isEmpty()) passwordRequired else null
        if (identifierError != null || passwordError != null) return
        scope.launch {
            if (!onLogin(identifier.trim(), password)) passwordError = wrongDetails
        }
    }

    AuthScaffold(
        title = stringResource(R.string.welcome),
        description = stringResource(R.string.login_description),
        coverAction = { CoverLanguageSwitch(selected = Language.current, onSelect = onLanguageChange) },
        footer = {
            AuthSwitchLine(
                prompt = stringResource(R.string.no_account_yet),
                action = stringResource(R.string.sign_up),
                onClick = onSignUp
            )
        }
    ) {
        AuthTextField(
            label = stringResource(R.string.mobile_or_email),
            value = identifier,
            onValueChange = {
                identifier = it
                identifierError = null
            },
            placeholder = stringResource(R.string.mobile_or_email_placeholder),
            leadingIcon = Icons.Outlined.AccountCircle,
            error = identifierError,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(
            label = stringResource(R.string.password),
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            placeholder = stringResource(R.string.enter_your_password),
            error = passwordError,
            imeAction = ImeAction.Done,
            onDone = ::submit
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onForgotPassword) {
                Text(stringResource(R.string.forgot_password), style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.height(8.dp))
        AuthButton(text = stringResource(R.string.log_in), onClick = ::submit)
    }
}
