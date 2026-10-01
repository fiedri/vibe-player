package dev.fiedri.vibe

import android.app.Application

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // actua como el motor de la inyección
class VibeApplication: Application() {// lifecycle at application level

}
