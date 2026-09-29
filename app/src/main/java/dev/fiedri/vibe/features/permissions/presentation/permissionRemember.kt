package dev.fiedri.vibe.features.permissions.presentation

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val PREFS_NAME = "vibe_permissions"

@Composable
fun rememberPermissionController(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
): PermissionController {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    return remember(context, launcher, prefs) {
        PermissionController(context, launcher, prefs)
    }
}
