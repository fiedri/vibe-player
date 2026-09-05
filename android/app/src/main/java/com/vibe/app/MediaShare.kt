package com.vibe.app

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import java.io.File

@CapacitorPlugin(name = "MediaShare")
class MediaShare : Plugin() {

    @PluginMethod
    fun share(call: PluginCall) {
        val uriString = call.getString("uri")

        if (uriString == null) {
            call.reject("Se requiere una URI para compartir")
            return
        }

        try {

            val parsedUri: Uri = if (uriString.startsWith("file://")) {
                val file = File(Uri.parse(uriString).path ?: "")
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.parse(uriString)
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, parsedUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Enviar audio a:")
            context.startActivity(chooser)

            call.resolve()
        } catch (e: Exception) {
            call.reject("Error al compartir audio: ${e.localizedMessage}")
        }
    }

    @PluginMethod
    fun shareMultiple(call: PluginCall) {
        val uriArray = call.getArray("uris")
        if(uriArray == null || uriArray.length() == 0) {
        call.reject("Se Require al menos una uri para compartir")
        }
        try {
            val audioUris = ArrayList<Uri>()
            for (i in 0 until uriArray.length()) {
                val uriString = uriArray.getString(i)?: continue
                val parsedUri = if(uriString.startsWith("file://")) {
                val file = File(Uri.parse(uriString).path ?: "")
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                }else {
                    Uri.parse(uriString)
                }
            audioUris.add(parsedUri)
            }
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "audio/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, audioUris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Enviar audio a:")
                context.startActivity(chooser)
                call.resolve()
        }catch (e: Exception){
            call.reject("Error al compartir archivos: ${e.localizedMessage}")
            }
    }

}