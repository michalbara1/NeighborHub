plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.androidx.navigation.safeargs)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.neighborhub"
    compileSdk = 35

    packaging {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }

    defaultConfig {
        applicationId = "com.example.neighborhub"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        dataBinding = true
        viewBinding = true
    }
}

configurations.all {
    exclude(group = "com.j256.ormlite", module = "ormlite-core")
}

dependencies {
    // Play Services and Location
    implementation(libs.play.services.location)
    implementation(libs.play.services.base)
    implementation(libs.play.services.basement)
    implementation(libs.play.services.auth)

    // OSMDroid Map Libraries
    implementation(libs.osmdroid.android)
    implementation(libs.osmdroid.wms)
    implementation(libs.osmdroid.mapsforge)
    implementation(libs.osmdroid.geopackage)

    // Networking
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)

    // Androidx Libraries
    implementation("com.j256.ormlite:ormlite-android:6.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.runner)
    implementation(libs.material)
    implementation(libs.androidx.appcompat.v170)

    // Lifecycle and Coroutines
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.fragment.ktx.v276)
    implementation(libs.androidx.navigation.ui.ktx.v276)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.auth.v2101)
    implementation(libs.firebase.firestore.ktx)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Image Loading
    implementation(libs.coil.compose.v210)
    implementation(libs.glide.v4120)
    implementation(libs.circleimageview)
    annotationProcessor(libs.compiler)

    // Other
    implementation(libs.play.services.cast.tv)
    implementation(libs.identity.jvm)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt("androidx.room:room-compiler:2.6.1")

    // Cloudinary
    implementation(libs.cloudinary.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
apply(plugin = "com.google.gms.google-services")
