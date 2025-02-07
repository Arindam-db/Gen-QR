plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.qrscanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.qrscanner"
        minSdk = 29
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation(libs.cardview)
        implementation(libs.core.ktx)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
        implementation(libs.core.splashscreen)
        implementation(libs.zxing.android.embedded)
        implementation(libs.core)
        implementation(libs.camera.core)
        implementation(libs.camera.camera2)
        implementation(libs.camera.lifecycle)
        implementation(libs.camera.view)
        implementation(libs.barcode.scanning)

}
