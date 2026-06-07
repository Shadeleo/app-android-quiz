plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.projetmobilel3informatiqueleopereira"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projetmobilel3informatiqueleopereira"
        minSdk = 30
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.generativeai)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Bibliothèque Gemini stable (v0.7.0 est très fiable pour Java)
    implementation("com.google.ai.client.generativeai:generativeai:0.1.2")

    // Support pour les tâches asynchrones (Futures)
    implementation("com.google.guava:guava:31.1-android")

    // JSON et PDF
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.itextpdf:itextg:5.5.10")

    // Base de données Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")


    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Pour la Caméra (CameraX)
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // Pour l'IA (MediaPipe - Reconnaissance des mains)
    implementation("com.google.mediapipe:tasks-vision:0.10.0")

    // Pour les composants Material (recommandé)
    implementation("com.google.android.material:material:1.11.0")

    // Pour la CardView classique de base
    implementation("androidx.cardview:cardview:1.0.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
