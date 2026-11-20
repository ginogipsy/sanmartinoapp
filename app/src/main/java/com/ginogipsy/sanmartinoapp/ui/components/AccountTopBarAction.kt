package com.ginogipsy.sanmartinoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ginogipsy.sanmartinoapp.R
import com.ginogipsy.sanmartinoapp.SanMartinoApplication
import com.ginogipsy.sanmartinoapp.data.model.AuthUser
import com.ginogipsy.sanmartinoapp.data.repository.AuthRepository
import com.ginogipsy.sanmartinoapp.i18n.localizedStringResource as stringResource
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import kotlinx.coroutines.launch

@Composable
fun AccountTopBarAction() {
    val context = LocalContext.current
    val authRepository = remember(context) {
        (context.applicationContext as SanMartinoApplication).container.authRepository
    }
    val currentUser by authRepository.currentUser.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { showDialog = true }) {
        Icon(
            imageVector = if (currentUser != null) Icons.Filled.AccountCircle else Icons.AutoMirrored.Filled.Login,
            contentDescription = stringResource(R.string.auth_account_cd),
            tint = if (currentUser != null) MaterialTheme.colorScheme.secondary else Color.White,
        )
    }

    if (showDialog) {
        AuthAccountDialog(
            authRepository = authRepository,
            currentUser = currentUser,
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
fun AuthAccountDialog(
    authRepository: AuthRepository,
    currentUser: AuthUser?,
    onDismiss: () -> Unit,
    onAuthenticated: ((AuthUser) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    var isRegisterMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorResId by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when {
                    currentUser != null -> stringResource(R.string.auth_profile_title)
                    isRegisterMode -> stringResource(R.string.auth_register_title)
                    else -> stringResource(R.string.auth_login_title)
                },
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            if (currentUser != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.auth_logged_as, currentUser.displayName),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "@${currentUser.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (currentUser.email.isNotBlank()) {
                        Text(
                            text = currentUser.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = stringResource(R.string.auth_profile_hint),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = !isRegisterMode,
                            onClick = {
                                isRegisterMode = false
                                errorResId = null
                            },
                            label = { Text(stringResource(R.string.auth_tab_login)) },
                        )
                        FilterChip(
                            selected = isRegisterMode,
                            onClick = {
                                isRegisterMode = true
                                errorResId = null
                            },
                            label = { Text(stringResource(R.string.auth_tab_register)) },
                        )
                    }

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorResId = null },
                        label = { Text(stringResource(R.string.auth_field_username)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorResId = null },
                            label = { Text(stringResource(R.string.auth_field_email)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text(stringResource(R.string.auth_field_first_name)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text(stringResource(R.string.auth_field_last_name)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorResId = null },
                        label = { Text(stringResource(R.string.auth_field_password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorResId = null },
                            label = { Text(stringResource(R.string.auth_field_confirm_password)) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    errorResId?.let { resId ->
                        Text(
                            text = stringResource(resId),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (currentUser != null) {
                OutlinedButton(
                    onClick = {
                        authRepository.logout()
                        onDismiss()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Text(stringResource(R.string.auth_action_logout))
                }
            } else {
                Button(
                    enabled = !isLoading && username.isNotBlank() && password.isNotBlank() && (!isRegisterMode || email.isNotBlank()),
                    onClick = {
                        if (isRegisterMode && password.length < 6) {
                            errorResId = R.string.auth_err_password_short
                            return@Button
                        }
                        if (isRegisterMode && password != confirmPassword) {
                            errorResId = R.string.auth_err_password_mismatch
                            return@Button
                        }
                        isLoading = true
                        errorResId = null
                        scope.launch {
                            val res = if (isRegisterMode) {
                                authRepository.register(
                                    username = username,
                                    email = email,
                                    password = password,
                                    confirmPassword = confirmPassword,
                                    firstName = firstName,
                                    lastName = lastName,
                                )
                            } else {
                                authRepository.login(
                                    username = username,
                                    password = password,
                                )
                            }
                            isLoading = false
                            when (res) {
                                is NetworkResult.Success -> {
                                    onAuthenticated?.invoke(res.data)
                                    onDismiss()
                                }
                                is NetworkResult.Error -> {
                                    errorResId = mapAuthErrorToResId(res, isRegisterMode)
                                }
                            }
                        }
                    },
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = if (isRegisterMode)
                                stringResource(R.string.auth_action_register)
                            else
                                stringResource(R.string.auth_action_login),
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}

private fun mapAuthErrorToResId(error: NetworkResult.Error, isRegisterMode: Boolean): Int {
    if (error is NetworkResult.Error.Network) {
        return R.string.auth_err_network
    }
    val raw = error.message.lowercase()
    return when {
        raw.contains("6 caratteri") || raw.contains("6 characters") || raw.contains("too short") ->
            R.string.auth_err_password_short
        raw.contains("non coincidono") || raw.contains("do not match") ->
            R.string.auth_err_password_mismatch
        raw.contains("già registrat") || raw.contains("already exists") || raw.contains("409") ->
            R.string.auth_err_user_exists
        raw.contains("credenziali") || raw.contains("non corretti") || raw.contains("unauthorized") ||
            (error is NetworkResult.Error.Http && error.code == 401 && !isRegisterMode) ->
            R.string.auth_err_invalid_credentials
        raw.contains("forbidden") || raw.contains("non autorizzato") ||
            (error is NetworkResult.Error.Http && error.code == 403) ->
            R.string.auth_err_forbidden
        raw.contains("non valid") || raw.contains("invalid") ||
            (error is NetworkResult.Error.Http && error.code == 400 && isRegisterMode) ->
            R.string.auth_err_invalid_data
        else -> R.string.auth_err_generic
    }
}
