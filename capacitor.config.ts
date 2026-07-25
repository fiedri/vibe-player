import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.rootwave.app',
  appName: 'RootWave',
  webDir: 'build',
  server: {
    url: 'http://192.168.0.103:5173',
    cleartext: true
  }
};

export default config;
