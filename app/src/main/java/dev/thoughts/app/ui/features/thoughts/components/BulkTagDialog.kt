package dev.thoughts.app.ui.features.thoughts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.thoughts.app.R

@Composable
fun BulkTagDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var tag by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.bulk_tag_dialog_title)) },
        text = {
            OutlinedTextField(
                value = tag,
                onValueChange = { tag = it },
                placeholder = { Text(stringResource(R.string.tag_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onAdd(tag) }, enabled = tag.isNotBlank()) { Text(stringResource(R.string.add)) }
        },
        dismissButton = {
            TextButton(onClick = { onRemove(tag) }, enabled = tag.isNotBlank()) { Text(stringResource(R.string.remove)) }
        },
    )
}
