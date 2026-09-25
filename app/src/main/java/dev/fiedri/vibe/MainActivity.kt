package dev.fiedri.vibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.fiedri.vibe.ui.components.Pager
import dev.fiedri.vibe.player.Player
import dev.fiedri.vibe.player.PlayerState
import dev.fiedri.vibe.player.Song
import dev.fiedri.vibe.ui.VibeApp
import dev.fiedri.vibe.ui.components.VibeToBar
import dev.fiedri.vibe.ui.theme.VibeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {

            VibeTheme {
                VibeApp()
            }
        }
    }
}
