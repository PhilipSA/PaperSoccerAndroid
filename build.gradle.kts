// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.3.0-rc03")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
        classpath("com.google.gms:google-services:3.0.0")
    }
}

allprojects {
    repositories {
        jcenter()
        google()
    }
}

tasks.create<Delete>("clean") {
    delete = setOf(rootProject.buildDir)
}
