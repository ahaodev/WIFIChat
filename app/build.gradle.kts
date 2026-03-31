import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    jacoco
}

fun getDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm")
    return sdf.format(Date())
}

val verCode = 1
val verName = "1.0.0"

android {
    namespace = "com.ruanhao.wifichat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ruanhao.wifichat"
        minSdk = 21
        targetSdk = 35
        versionCode = verCode
        versionName = verName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    lint {
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    applicationVariants.all {
        val variant = this
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val appVersion = project.findProperty("APP_VERSION") as? String ?: "1.0.0"
            val isJenkins = project.findProperty("IS_JENKINS") as? String ?: "false"

            if (isJenkins == "true") {
                output.outputFileName = "WiFi-Chat-${appVersion}.apk"
            } else {
                val newName = if (variant.buildType.name == "debug") {
                    "WiFi-Chat-v${appVersion}-debug.apk"
                } else {
                    "WiFi-Chat-${appVersion}-${variant.buildType.name}.apk"
                }
                output.outputFileName = newName
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.kotlin.stdlib)
    implementation(libs.appcompat)
    implementation(libs.gson)
    implementation(libs.litepal)
    implementation(libs.rxandroid)
    implementation(libs.rxjava)
    implementation(libs.logger)
    implementation(libs.photoview)
    implementation(libs.matisse)
    implementation(libs.rxpermissions)

    testImplementation(libs.junit)

    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.espresso.core)
}
