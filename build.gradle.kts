// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
        jcenter() // Use jcenter only if absolutely necessary
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0") // Updated Android Gradle Plugin version
        classpath("com.google.gms:google-services:4.3.15") // Add the Google Services classpath for Firebase
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.android.library") version "8.2.0" apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
