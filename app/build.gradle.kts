import org.apache.tools.ant.util.JavaEnvUtils.VERSION_11

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.beerbasement"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.beerbasement"
        minSdk = 28
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
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    kotlinOptions {
        jvmTarget = "18"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes.add("META-INF/{AL2.0,LGPL2.1}")  // Exclude conflicting resources
            excludes.add("META-INF/DEPENDENCIES")     // Exclude the DEPENDENCIES file
            excludes.add("META-INF/INDEX.LIST")       // Exclude the INDEX.LIST file
            excludes.add("META-INF/io.netty.versions.properties")
        }
    }
}

dependencies {
    // Android & Jetpack libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.firebase.common.ktx)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.animation.core.android)

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Jetpack Compose Material 3
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Mockito for testing
    implementation("org.mockito:mockito-core:5.14.2")
    implementation("org.mockito:mockito-inline:5.2.0")

    // Lifecycle and Compose Integration
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.5") // observeAsState

    // Test dependencies
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.5")

    // Google Cloud Vision API
    implementation("com.google.auth:google-auth-library-oauth2-http:1.30.0")
    implementation("com.google.cloud:google-cloud-vision:3.52.0")

    // Other dependencies
    implementation("androidx.activity:activity-ktx:1.9.3")

    // gRPC dependencies
    implementation("io.grpc:grpc-core:1.68.2")
    implementation("io.grpc:grpc-okhttp:1.68.2") // Ensure this is included
    implementation("io.grpc:grpc-netty-shaded:1.68.2") // Ensure this is included
    implementation("io.grpc:grpc-protobuf:1.68.2")
    implementation("io.grpc:grpc-stub:1.68.2")
    implementation("io.grpc:grpc-context:1.68.2")
    implementation("io.grpc:grpc-api:1.68.2")
}

