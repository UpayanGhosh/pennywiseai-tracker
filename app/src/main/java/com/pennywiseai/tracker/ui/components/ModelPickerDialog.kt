package com.pennywiseai.tracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pennywiseai.tracker.core.LlmModel

/**
 * Lets the user choose which on-device LLM to download. Shown when a "Download"
 * action is tapped. [onConfirm] receives the chosen model; the caller is
 * responsible for persisting the selection and starting the download.
 */
@Composable
fun ModelPickerDialog(
    models: List<LlmModel>,
    selectedModelId: String,
    onConfirm: (LlmModel) -> Unit,
    onDismiss: () -> Unit
) {
    var chosenId by remember(selectedModelId) { mutableStateOf(selectedModelId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose AI model") },
        text = {
            Column {
                models.forEach { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = chosenId == model.id,
                                onClick = { chosenId = model.id }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = chosenId == model.id,
                            onClick = { chosenId = model.id }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = model.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${model.family} • ${model.sizeMb} MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = model.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val chosen = models.firstOrNull { it.id == chosenId } ?: models.first()
                    onConfirm(chosen)
                }
            ) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
