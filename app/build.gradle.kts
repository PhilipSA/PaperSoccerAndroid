plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.ps.simplepapersoccer"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 15
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.11")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.1")
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.preference:preference:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("com.google.android.material:material:1.0.0")

    implementation("com.google.android.gms:play-services-ads:17.1.2")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("org.mockito:mockito-core:2.19.0")
    //Life cycles
    implementation("androidx.lifecycle:lifecycle-extensions:2.0.0")
}

repositories {
    mavenCentral()
    google()
    jcenter()
}