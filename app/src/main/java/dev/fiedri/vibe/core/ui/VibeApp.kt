package dev.fiedri.vibe.core.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.fiedri.vibe.R
import dev.fiedri.vibe.features.permissions.presentation.PermissionDialog
import dev.fiedri.vibe.features.permissions.presentation.PermissionItem
import dev.fiedri.vibe.features.permissions.presentation.PermissionStatus
import dev.fiedri.vibe.features.permissions.presentation.rememberPermissionController
import dev.fiedri.vibe.features.player.presentation.Player
import dev.fiedri.vibe.features.player.presentation.PlayerState
import dev.fiedri.vibe.features.player.presentation.PlayerUiState

@Composable
fun VibeApp(){
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    val grantedMessage = stringResource(R.string.permisos_toast_granted)
    val deniedMessage = stringResource(R.string.permisos_toast_denied)
    var hasAudioPermission: Boolean? by remember { mutableStateOf(null) }
    val permissionController = rememberPermissionController(
        onPermissionGranted = {
            hasAudioPermission = true
            showPermissionDialog = false
            Toast.makeText(context, grantedMessage, Toast.LENGTH_SHORT).show()
            // trigger audio loading
        },
        onPermissionDenied = {
            hasAudioPermission = false
            showPermissionDialog = false
            Toast.makeText(context, deniedMessage, Toast.LENGTH_SHORT).show()
        }
    )
    LaunchedEffect(Unit) {
        val status = permissionController.status()
        hasAudioPermission = status == PermissionStatus.GRANTED

        when (status) {
            PermissionStatus.GRANTED -> {
                // trigger audio loading
            }
            PermissionStatus.NOT_REQUESTED,
            PermissionStatus.DENIED_RETRYABLE -> {
                showPermissionDialog = true
            }
            PermissionStatus.DENIED_PERMANENTLY -> {
                showPermissionDialog = false
            }
        }
    }
    var isPlaying by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    BackHandler(enabled = isExpanded) {
        if(isExpanded){
            isExpanded = false
        }
    }
    Box(Modifier.fillMaxSize()){
        if(showPermissionDialog) {
            PermissionDialog(
                items = listOf(
                    PermissionItem(
                        titleRes = R.string.permisos_audio_title,
                        descriptionRes = R.string.permisos_audio_description,
                    ),
                ),
                onGrant = {
  permissionController.requestPermission()
                },
                onDismiss = {showPermissionDialog = false},
            )
        }
        Column(
            modifier = Modifier.padding(bottom = 92.dp).fillMaxSize()
        ) {
            VibeNavGraph(
                hasAudioPermission = hasAudioPermission == true,
                onOpenSettings = { permissionController.openAppSettings() },
                onPermissionRetry = { hasAudioPermission = permissionController.hasPermission() },
            )
        }
        Player(
            modifier = Modifier.align(Alignment.BottomCenter),
            onCycleRepeat = {}
        )
    }

}
