package dev.fiedri.vibe;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import dev.fiedri.vibe.MediaDeletePlugin;
import com.vibe.app.MediaShare;
import com.vibe.app.NativeAudioEnginePlugin;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(MediaDeletePlugin.class);
        registerPlugin(MediaShare.class);
        registerPlugin(NativeAudioEnginePlugin.class);
        super.onCreate(savedInstanceState);
    }
}

