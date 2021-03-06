buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:1.4.30")
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
