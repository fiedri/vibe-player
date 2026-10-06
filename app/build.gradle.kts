plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)

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
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.rules)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // navigation
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)

    // coroutines
    implementation(libs.kotlinx.coroutines.android)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // test
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.test:runner:1.5.2")

    // media3
    implementation(libs.androidx.media3.exoplayer)

    // coil
    implementation(libs.coil.compose)

    //Room
    implementation(libs.androidx.room3.runtime)
    ksp(libs.androidx.room3.compiler)
}