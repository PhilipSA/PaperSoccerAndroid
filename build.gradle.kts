buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.30")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:1.3.30")
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
