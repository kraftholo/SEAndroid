plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.rtse"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rtse"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))

    val composeVersion = "1.0.0"
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
//    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
//    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //1.12.2 -> libsni.so error
    //1.10.0 -> Model should be between 5 and 7 version error
    val android_lite_version = "1.13.1"
    implementation ("org.pytorch:pytorch_android_lite:$android_lite_version")
//    implementation("org.tensorflow:tensorflow-lite:2.14.0")
//    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")


    val nav_version = "2.7.3"
    implementation("androidx.navigation:navigation-compose:$nav_version")

//    implementation(files("libs/commons-math3-3.6.1.jar")) // Complex, FFT
//    implementation(files("libs/sound-1.1.1.jar")) // STFT
    implementation(files("libs/jlibrosa-1.0.2.jar"))

////    ndarray stuff
//    implementation ("org.jetbrains.kotlinx:multik-core:0.2.2")
//    implementation ("org.jetbrains.kotlinx:multik-default:0.2.2")

}