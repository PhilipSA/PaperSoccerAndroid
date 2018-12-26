buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.3.0-rc03")
        classpath("com.google.gms:google-services:4.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.11")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:1.3.11")
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
