plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.yusuf.connectcircle"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yusuf.connectcircle"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
        dataBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    packagingOptions {
        resources.excludes.add("META-INF/*")
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    //Google Fonts
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.8")


    //Coil
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("androidx.navigation:navigation-compose:2.4.0-alpha07")

    //Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    implementation("androidx.compose.runtime:runtime-livedata:1.6.8")

    implementation("com.guolindev.permissionx:permissionx:1.7.1")

    implementation("com.google.accompanist:accompanist-permissions:0.34.0") // Accompanist Permissions

    implementation("org.jitsi.react:jitsi-meet-sdk:10.0.1")

    // https://mvnrepository.com/artifact/com.google.auth/google-auth-library-oauth2-http
    implementation("com.google.auth:google-auth-library-oauth2-http:1.24.1")

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-functions-ktx")

    implementation("io.grpc:grpc-okhttp:1.66.0")
    // https://mvnrepository.com/artifact/io.grpc/grpc-protobuf-lite
    implementation("io.grpc:grpc-protobuf-lite:1.66.0")

    implementation("io.grpc:grpc-stub:1.66.0")


}