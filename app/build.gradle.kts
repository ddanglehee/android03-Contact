plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-android")
}

android {
    compileSdk = Apps.compileSdk

    defaultConfig {
        applicationId = "com.ivyclub.contact"
        minSdk = Apps.minSdk
        targetSdk = Apps.targetSdk
        versionCode = Apps.versionCode
        versionName = Apps.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(Dep.AndroidX.core)
    implementation(Dep.AndroidX.appcompat)
    implementation(Dep.AndroidX.material)
    implementation(Dep.AndroidX.constraintLayout)
    implementation(Dep.AndroidX.navigationUIKtx)
    implementation(Dep.AndroidX.legacySupport)
    implementation(Dep.AndroidX.navigationFragmentKtx)
    implementation(Dep.AndroidX.activityKtx)
    implementation(Dep.AndroidX.fragmentKtx)
    implementation(Dep.AndroidX.coroutine)
    implementation(Dep.AndroidX.coroutineCore)
    implementation(Dep.AndroidX.viewpager2)
    implementation(Dep.AndroidX.support)
    implementation(Dep.AndroidX.roomRuntime)
    implementation(Dep.Libs.glide)
    implementation(Dep.Libs.gson)
    kapt(Dep.AndroidX.roomCompiler)
    testImplementation(Dep.Test.jUnit)
    androidTestImplementation(Dep.Test.ext)
    androidTestImplementation(Dep.Test.espresso)
}