plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    // CHANGE THIS to your own unique id before publishing (cannot be changed later on Play Store)
    namespace = "com.yourname.dailydots"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yourname.dailydots"
        minSdk = 26
        targetSdk = 35 // Play requires a recent target level; raise it when Play Console asks
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5")

    // Offline database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Home-screen widget
    implementation("androidx.glance:glance-appwidget:1.1.0")

    // Google Play Billing (lifetime unlock / annual)
    implementation("com.android.billingclient:billing-ktx:7.1.1")
}
