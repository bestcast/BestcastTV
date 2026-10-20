plugins {
    id("com.android.application")
}

android {
    namespace = "com.bestcasttv.bestcasttvapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bestcasttv.bestcasttvapp"
        minSdk = 23
        targetSdk = 36
        versionCode = 8
        versionName = "2.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation("androidx.leanback:leanback:1.2.0")
    implementation("androidx.media2:media2-session:1.3.0")
    implementation("androidx.media2:media2-player:1.3.0")
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.navigation:navigation-fragment:2.9.6")
    implementation("androidx.navigation:navigation-ui:2.9.6")
    implementation("com.google.zxing:core:3.5.4")
    implementation("io.github.chaosleung:pinview:1.4.4")
}
