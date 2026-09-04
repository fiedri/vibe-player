package dev.fiedri.vibe;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import dev.fiedri.vibe.MediaDeletePlugin;
import com.vibe.app.MediaShare;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(MediaDeletePlugin.class);
        registerPlugin(MediaShare.class);
        super.onCreate(savedInstanceState);
    }
}

