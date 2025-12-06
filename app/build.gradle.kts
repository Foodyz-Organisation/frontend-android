// build.gradle (Module :app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20" // <-- Use your project's Kotlin version

}

android {
    namespace = "com.example.damprojectfinal"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.damprojectfinal"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM manages all Compose versions automatically
    implementation(platform(libs.androidx.compose.bom))

    // Ktor Network Client (Your Existing)
    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-android:2.3.6")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("io.coil-kt:coil-compose:2.5.0") // Check for the latest version
    // Ktor JSON Serialization (Your Existing)
    implementation("androidx.compose.ui:ui-text") // <--- THIS ONE
    implementation("io.ktor:ktor-client-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // 🔑 NEW ADDITION FOR SECURE TOKEN STORAGE (DataStore) 🔑
    // DataStore (Preferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Kotlin Coroutines (Needed by DataStore and Ktor, matching the common version)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("io.ktor:ktor-client-auth:2.3.6")
    implementation("androidx.compose.foundation:foundation") // <--- This is essential
    // Compose UI & Material (Your Existing)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Retrofit & OkHttp (Your Existing)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Keeping your version, but consider updating to 4.12.0 for OkHttp consistency
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    // Font Awesome icons (Your Existing)
    implementation("br.com.devsrsouza.compose.icons:font-awesome:1.1.0")

    implementation("io.coil-kt:coil-compose:2.6.0") // Use the latest stable version, check Coil website for current
    // Also ensure you have androidx.activity:activity-compose if you haven't already:
    implementation("androidx.activity:activity-compose:1.8.2") // Use the latest stable version

    // Accompanist Swipe Refresh for Pull-to-Refresh
    implementation("com.google.accompanist:accompanist-swiperefresh:0.34.0")
    //video implements
    implementation("io.coil-kt:coil-video:2.6.0")

    // --- START: NEW DEPENDENCIES FOR REELS FEATURE ---
    // Coroutines for Android-specific dispatchers
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Ensure this matches your core coroutines version

    // Video Playback (ExoPlayer)
    implementation("androidx.media3:media3-exoplayer:1.2.0") // Replace with latest stable version if desired
    implementation("androidx.media3:media3-ui:1.2.0")       // For PlayerView UI component

    // UI Components for Reels (ViewPager2 & RecyclerView)
    implementation("androidx.viewpager2:viewpager2:1.0.0") // For the reels scrolling mechanism
    implementation("androidx.recyclerview:recyclerview:1.3.2") // Explicitly added for clarity, often a transitive dep
    // --- END: NEW DEPENDENCIES FOR REELS FEATURE ---

    // Testing (Your Existing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug tools (Your Existing)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
