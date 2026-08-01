import type { CapacitorConfig } from "@capacitor/cli";

const config: CapacitorConfig = {
  appId: "com.vibe.app",
  appName: "Vibe",
  //appVersion: "0.1.2",
  webDir: "build",
  plugins: {
    MediaSession: {
      foregroundService: "always",
    },
  },
};

export default config;
