plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.ps.simplepapersoccer"
        minSdkVersion(19)
        targetSdkVersion(28)
        versionCode = 14
        versionName = "1.10"
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
        getByName("main").java.srcDirs("src/main/java", "src/main/java/2")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.1")
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("com.google.firebase:firebase-ads:17.1.2")
    androidTestImplementation("junit:junit:4.12")
    androidTestImplementation("org.mockito:mockito-core:2.19.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.11")
    implementation("co.metalab.asyncawait:asyncawait:1.0.0")
    implementation("me.grantland:autofittextview:0.2.1")
}

repositories {
    mavenCentral()
    google()
}