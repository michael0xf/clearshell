plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mtk.shell"
    compileSdk = 27

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = "" 
            keyAlias = "keyalias"
            keyPassword = ""
        }
    }

    defaultConfig {
        applicationId = "com.mtk.shell"
        minSdk = 24
        targetSdk = 27
        versionCode = 1
        versionName = "1.0"

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
    lint {
        disable.add("ExpiredTargetSdkVersion")
    }
}

dependencies {
    implementation("com.android.support:appcompat-v7:27.1.1")
    implementation("com.android.support:support-v4:27.1.1")
    implementation("com.android.support:design:27.1.1")
    testImplementation(libs.junit)
}