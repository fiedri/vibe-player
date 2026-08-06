package com.vibe.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import com.vibe.app.MediaDeletePlugin;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(MediaDeletePlugin.class);
        super.onCreate(savedInstanceState);
    }
}

