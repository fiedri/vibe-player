import type { CapacitorConfig } from "@capacitor/cli";
const isDev = process.env.CAP_ENV === 'dev';
const config: CapacitorConfig = {
  appId: "dev.fiedri.vibe",
  appName: "Vibe",
  webDir: "build",
  server: isDev ? {
    url: "http://192.168.0.103:5173/",
    cleartext: true,
  }: undefined,
  plugins: {
    MediaSession: {
      foregroundService: "always",
    },
  },
};

export default config;
