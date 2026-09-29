package dev.fiedri.vibe.features.permissions.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.fiedri.vibe.R
import dev.fiedri.vibe.core.ui.theme.VibeTheme

data class PermissionItem(
    val titleRes: Int,
    val descriptionRes: Int,
)

@Composable
fun PermissionDialog(
    items: List<PermissionItem>,
    onGrant: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true),
    ) {
        PermissionDialogContent(
            items = items,
            onGrant = onGrant,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}

@Composable
private fun PermissionDialogContent(
    items: List<PermissionItem>,
    onGrant: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(max = 420.dp)
            .background(VibeTheme.colors.popover)
            .border(2.dp, VibeTheme.colors.border)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.permisos_dialog_title),
            style = VibeTheme.typography.titleLarge,
            color = VibeTheme.colors.foreground,
        )

        items.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VibeTheme.colors.secondary)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(item.titleRes),
                    style = VibeTheme.typography.titleMedium,
                    color = VibeTheme.colors.foreground,
                )
                Text(
                    text = stringResource(item.descriptionRes),
                    style = VibeTheme.typography.bodyMedium,
                    color = VibeTheme.colors.mutedForeground,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = VibeTheme.colors.mutedForeground,
                ),
                shape = RectangleShape
            ) {
                Text(
                    text = stringResource(R.string.permisos_dialog_later),
                    style = VibeTheme.typography.titleMedium,
                )
            }
            TextButton(
                onClick = onGrant,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = VibeTheme.colors.primary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.permisos_dialog_grant),
                    style = VibeTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PermissionDialogPreview() {
    VibeTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VibeTheme.colors.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            PermissionDialogContent(
                items = listOf(
                    PermissionItem(
                        titleRes = R.string.permisos_audio_title,
                        descriptionRes = R.string.permisos_audio_description,
                    ),
                ),
                onGrant = {},
                onDismiss = {},
            )
        }
    }
}
