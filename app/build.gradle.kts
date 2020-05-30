import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.ps.simplepapersoccer"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 16
        versionName = "1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.72")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("com.google.android.material:material:1.1.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.9.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.2")

    testImplementation("androidx.test:core:1.2.0")
    testImplementation("junit:junit:4.13")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    androidTestImplementation("org.mockito:mockito-core:3.2.4")
    //Life cycles
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6")
}

repositories {
    mavenCentral()
    google()
    jcenter()
}