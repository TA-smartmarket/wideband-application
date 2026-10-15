plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.smartmarket.kiosk"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.smartmarket.kiosk"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "3.0"

        // UWB positioning server (Flask, wideband-positioning repo). Default targets
        // the host's localhost from an Android emulator (10.0.2.2). On a physical
        // phone running against a LAN server, override SYSTEM_IMAGE the value:
        //   -PUWB_BASE_URL=http://192.168.x.y:8080/
        // See docs: no "localhost" is hardcoded as the production address.
        val uwbBaseUrl = (project.findProperty("UWB_BASE_URL") as String?) ?: "http://10.0.2.2:8080/"
        val uwbTagId = (project.findProperty("UWB_TAG_ID") as String?) ?: "tag-1"

        buildConfigField("String", "UWB_BASE_URL", "\"$uwbBaseUrl\"")
        buildConfigField("String", "UWB_TAG_ID", "\"$uwbTagId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    // Coroutines (explicit)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Networking (UWB positioning polling; REST only, no Retrofit needed)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}