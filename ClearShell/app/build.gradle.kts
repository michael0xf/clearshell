import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val signingProperties = Properties().apply {
    val signingPropertiesFile = file("signing.properties")
    if (signingPropertiesFile.isFile) {
        signingPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.mtk.shell"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = file(signingProperties.getProperty("storeFile", "release.keystore"))
            storePassword = signingProperties.getProperty("storePassword", "")
            keyAlias = signingProperties.getProperty("keyAlias", "keyalias")
            keyPassword = signingProperties.getProperty("keyPassword", "")
        }
    }

    defaultConfig {
        applicationId = "com.mtk.shell"
        minSdk = 21
        targetSdk = 34
        versionCode = 196
        versionName = "1.0.8"
        multiDexEnabled = true

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.material:material:1.13.0")
    testImplementation(libs.junit)
}
