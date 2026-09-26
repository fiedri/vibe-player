plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.fiedri.vibe"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.fiedri.vibe"
        minSdk = 24
        targetSdk = 37
        versionCode = 13
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    signingConfigs {
        create("release") {
            val storeFilePath = providers.gradleProperty("MYAPP_RELEASE_STORE_FILE")
            if (storeFilePath.isPresent) {
                storeFile = file(storeFilePath.get())
                storePassword = providers.gradleProperty("MYAPP_RELEASE_STORE_PASSWORD").get()
                keyAlias = providers.gradleProperty("MYAPP_RELEASE_KEY_ALIAS").get()
                keyPassword = providers.gradleProperty("MYAPP_RELEASE_KEY_PASSWORD").get()
            }
        }
    }
    buildTypes {
        release {
            optimization {
                signingConfig = signingConfigs.getByName("release")
                enable = false
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}