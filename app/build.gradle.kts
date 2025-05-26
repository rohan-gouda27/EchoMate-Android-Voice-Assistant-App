plugins {
    alias(libs.plugins.android.application)
    // Add this if you're using Kotlin in your project
    // alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.speechtotextapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.speechtotextapplication"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Dexter - runtime permission handler
    implementation("com.karumi:dexter:6.2.3")

    // Unit and UI testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
