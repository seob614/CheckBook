plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    //파이어베이스
    id("com.google.gms.google-services")
    //Crashlytics Gradle plugin:app
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.courr.checkbook"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.courr.checkbook"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //바텀네비게이션 관련 라이브러리
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material)
    implementation(libs.kotlinx.serialization.json)

    //hiltviewmodel로 composable간 viewmodel 공유
    implementation(libs.hilt.navigation.compose)

    //livedata
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.runtime.compose)
    implementation(libs.androidx.livedata.ktx)

    //파이어베이스
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth-ktx")
    // Firebase Crashlytics 추가
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    //location
    implementation("com.google.android.gms:play-services-location:21.0.1")
}