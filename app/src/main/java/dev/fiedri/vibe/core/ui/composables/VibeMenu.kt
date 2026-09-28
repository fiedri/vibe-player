package dev.fiedri.vibe.core.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.fiedri.vibe.core.ui.theme.VibeTheme


@Composable
fun VibeMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RectangleShape,
        containerColor = VibeTheme.colors.popover,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        content = content
    )
}

@Composable
fun VibeMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    destructive: Boolean = false
) {
    val contentColor = if (destructive) {
        VibeTheme.colors.destructive
    } else {
        VibeTheme.colors.foreground
    }
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                color = contentColor,
                style = VibeTheme.typography.bodyLarge
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = MenuDefaults.itemColors(
            textColor = contentColor,
            leadingIconColor = contentColor,
            trailingIconColor = contentColor,
            disabledTextColor = VibeTheme.colors.mutedForeground,
            disabledLeadingIconColor = VibeTheme.colors.mutedForeground,
            disabledTrailingIconColor = VibeTheme.colors.mutedForeground
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    )
}
