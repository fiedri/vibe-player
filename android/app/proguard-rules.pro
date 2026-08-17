# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
# Reproducible build: deterministic R8 output
# -dontobfuscate disables name obfuscation — REQUIRED for bit-exact reproducibility.
# Re-enable obfuscation (comment this line) if APK size > reproducibility for your use case.
-optimizationpasses 1
-dontobfuscate
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-keepattributes *Annotation*

# --- Capacitor R8/ProGuard rules (REQUIRED when minifyEnabled true) ---
# Keep Capacitor framework classes and annotations so the JS↔Native bridge survives minification
-keep class com.getcapacitor.** { *; }
-keep @com.getcapacitor.annotation.CapacitorPlugin class *
-keepclassmembers class * { @com.getcapacitor.PluginMethod <methods>; }
-keep public class * extends com.getcapacitor.Plugin
-keep class com.getcapacitor.BridgeActivity { *; }
-keep class com.getcapacitor.Bridge { *; }
-keep class com.getcapacitor.Plugin { *; }
-keep class com.getcapacitor.PluginCall { *; }
-keep class com.getcapacitor.JSObject { *; }
-keep class com.getcapacitor.JSArray { *; }
-keep class com.getcapacitor.annotation.** { *; }
# Keep Capacitor Community plugins
-keep class com.getcapacitor.community.** { *; }
# Keep Cordova plugin adapters used by Capacitor
-keep class com.getcapacitor.adapters.** { *; }
# --- Project-specific plugins ---
-keep class dev.fiedri.vibe.** { *; }
-keep class dev.fiedri.vibe.MediaDeletePlugin { *; }
# Keep MediaScanner / AndroidX Activity Result contracts used by plugins
-keep class androidx.activity.result.contract.ActivityResultContracts$* { *; }
