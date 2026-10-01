package dev.fiedri.vibe.features.home.presentation

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.fiedri.vibe.R
import dev.fiedri.vibe.core.ui.composables.VibeMenu
import dev.fiedri.vibe.core.ui.composables.VibeMenuItem
import dev.fiedri.vibe.core.ui.theme.VibeTheme

enum class SortField(@StringRes val labelRes: Int) {
    TITLE(R.string.menus_home_overflow_menu_order_by_options_title),
    ARTIST(R.string.menus_home_overflow_menu_order_by_options_artist),
    DURATION(R.string.menus_home_overflow_menu_order_by_options_duration),
    DATE_ADDED(R.string.menus_home_overflow_menu_order_by_options_date),
    NAME(R.string.menus_home_overflow_menu_order_by_options_name),
    SONG_COUNT(R.string.menus_home_overflow_menu_order_by_options_number_of_songs)
}

fun sortFieldsForTab(tab: String): List<SortField> = when (tab) {
    "Songs" -> listOf(
        SortField.TITLE,
        SortField.DATE_ADDED,
        SortField.DURATION
    )
    "Albums" -> listOf(SortField.NAME, SortField.ARTIST)
    "Artists" -> listOf(SortField.NAME, SortField.SONG_COUNT)
    else -> emptyList()
}

@Composable
private fun SortRadioGlyph(selected: Boolean) {
    Icon(
        imageVector = if (selected) {
            Icons.Filled.RadioButtonChecked
        } else {
            Icons.Filled.RadioButtonUnchecked
        },
        contentDescription = null,
        tint = if (selected) {
            VibeTheme.colors.primary
        } else {
            VibeTheme.colors.mutedForeground
        },
        modifier = Modifier.size(20.dp)
    )
}

@Composable
fun HomeMenu(
    activeTab: String,
    expanded: Boolean = false,
    onDismissRequest: () -> Unit = {},
    onRefreshLibrary: () -> Unit = {},
    onSortSelected: (SortField) -> Unit = {},
    onSortAscendingChanged: (Boolean) -> Unit = {}
) {
    val fields = sortFieldsForTab(activeTab)
    val sortable = fields.isNotEmpty()

    var sortExpanded by remember { mutableStateOf(false) }
    var selectedField by remember(activeTab, fields) {
        mutableStateOf(fields.firstOrNull())
    }
    var ascending by remember { mutableStateOf(true) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (sortExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "chevronRotation"
    )

    VibeMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        VibeMenuItem(
            text = stringResource(R.string.menus_home_overflow_menu_refresh_library),
            onClick = {
                onRefreshLibrary()
                onDismissRequest()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        if (sortable) {
            VibeMenuItem(
                text = stringResource(R.string.menus_home_overflow_menu_order_by),
                onClick = { sortExpanded = !sortExpanded },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation)
                    )
                }
            )
        }
        if (sortable && sortExpanded) {
            fields.forEach { field ->
                VibeMenuItem(
                    text = stringResource(field.labelRes),
                    onClick = {
                        selectedField = field
                        onSortSelected(field)
                        onDismissRequest()
                    },
                    trailingIcon = { SortRadioGlyph(selected = field == selectedField) }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(VibeTheme.colors.border)
            )
            VibeMenuItem(
                text = stringResource(R.string.menus_home_overflow_menu_order_by_options_asc),
                onClick = {
                    ascending = true
                    onSortAscendingChanged(true)
                    onDismissRequest()
                },
                trailingIcon = { SortRadioGlyph(selected = ascending) }
            )
            VibeMenuItem(
                text = stringResource(R.string.menus_home_overflow_menu_order_by_options_desc),
                onClick = {
                    ascending = false
                    onSortAscendingChanged(false)
                    onDismissRequest()
                },
                trailingIcon = { SortRadioGlyph(selected = !ascending) }
            )
        }
    }
}
