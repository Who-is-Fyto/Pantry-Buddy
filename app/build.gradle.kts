plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.pantrybuddy"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.pantrybuddy"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX & Material UI
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)

    // Jetpack Lifecycle, ViewModel & LiveData
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.common.java8)

    // Navigation Component
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Room Database (Java Annotation Processor)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // WorkManager for background expiry tasks
    implementation(libs.work.runtime)

    // JSON parsing
    implementation(libs.gson)

    // Image Loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}