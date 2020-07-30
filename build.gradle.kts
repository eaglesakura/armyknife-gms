buildscript {
    rootProject.extra["kotlin_version"] = "1.3.72"
    rootProject.extra["kotlin_coroutines_version"] = "1.3.8"
    repositories {
        mavenLocal()
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${rootProject.extra["kotlin_version"]}")
        classpath("org.jetbrains.dokka:dokka-android-gradle-plugin:0.9.18") // kotlin-docs
        classpath("com.github.ben-manes:gradle-versions-plugin:0.21.0") // version checking plugin

        // deploy to bintray
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://dl.bintray.com/eaglesakura/maven/")
    }
    apply(from = rootProject.file("configure.gradle.kts"))
}
