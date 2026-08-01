import type { CapacitorConfig } from "@capacitor/cli";

const config: CapacitorConfig = {
  appId: "com.vibe.app",
  appName: "Vibe",
  //appVersion: "0.1.0",
  webDir: "build",
  server: {
    url: "http://192.168.0.103:5173/",
    cleartext: true,
  },
  plugins: {
    MediaSession: {
      foregroundService: "always",
    },
  },
};

export default config;
