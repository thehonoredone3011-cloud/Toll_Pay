import org.gradle.accessors.dm.LibrariesForLibs

val LibrariesForLibs.zxing: Any
    get() {
        TODO("Not yet implemented")
    }

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services) // Google Services for Firebase
}


android {
    namespace = "com.example.tollpay"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tollpay"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Enable multidex if you expect more than 64K methods
        multiDexEnabled = true
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
}

dependencies {


    implementation (libs.google.core)
    implementation (libs.zxing.android.embedded) // Or the latest version

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.multidex:multidex:2.0.1")
    implementation(libs.firebase.database)
    implementation(libs.google.firebase.firestore)
    implementation(libs.support.annotations)
    implementation(libs.firebase.storage)
    implementation(libs.annotation)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
