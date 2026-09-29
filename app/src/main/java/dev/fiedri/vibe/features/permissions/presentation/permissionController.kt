package dev.fiedri.vibe.features.permissions.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val KEY_AUDIO_ASKED = "audio_asked"

enum class PermissionStatus {
    GRANTED,
    NOT_REQUESTED,
    DENIED_RETRYABLE,
    DENIED_PERMANENTLY,
}

class PermissionController(
    private val context: Context,
    private val permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    private val prefs: SharedPreferences,
) {
    val audioPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
    }

    fun status(): PermissionStatus {
        if (hasPermission()) {
            return PermissionStatus.GRANTED
        }
        val alreadyAsked = prefs.getBoolean(KEY_AUDIO_ASKED, false)
        return if (!alreadyAsked) {
            PermissionStatus.NOT_REQUESTED
        } else if (shouldShowRationale()) {
            PermissionStatus.DENIED_RETRYABLE
        } else {
            PermissionStatus.DENIED_PERMANENTLY
        }
    }

    fun requestPermission() {
        prefs.edit().putBoolean(KEY_AUDIO_ASKED, true).apply()
        permissionLauncher.launch(audioPermission)
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun shouldShowRationale(): Boolean {
        val activity = context as? Activity ?: return false
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, audioPermission)
    }
}
