package com.ginogipsy.sanmartinoapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ginogipsy.sanmartinoapp.R
import com.ginogipsy.sanmartinoapp.data.model.LocalizedText
import com.ginogipsy.sanmartinoapp.i18n.LocalLanguage

@Composable
fun BilingualDescriptionDialog(
    title: String,
    description: LocalizedText,
    onDismiss: () -> Unit,
    extra: (@Composable () -> Unit)? = null,
) {
    val lang by LocalLanguage.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = description.pick(lang),
                    style = MaterialTheme.typography.bodyMedium,
                )
                extra?.let {
                    Column(modifier = Modifier.padding(top = 12.dp)) { it() }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}
