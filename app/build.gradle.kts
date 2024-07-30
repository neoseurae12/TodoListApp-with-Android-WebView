plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

    id("com.google.devtools.ksp")
}

android {
    namespace = "com.daangn.todolistapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.daangn.todolistapp"
        minSdk = 26
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.activity.ktx)  // ViewModel

    implementation(libs.kotlinx.coroutines.android) // Coroutines

    // Room
    implementation(libs.androidx.room.ktx)  // coroutines support for database transactions
    implementation(libs.androidx.room.runtime)  // Room API, containing all the classes & annotations for defining database
    ksp(libs.androidx.room.compiler)   // Room compiler, generating database implementation based on the annotations

    implementation(libs.gson)
}