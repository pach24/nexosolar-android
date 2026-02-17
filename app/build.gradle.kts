plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.nexosolar.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nexosolar.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.3.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // --- MÓDULOS ---
    implementation(project(":domain"))
    implementation(project(":data-retrofit"))
    implementation(project(":core"))
    implementation(project(":data"))

    // --- ANDROID UI ---
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.shimmer)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // --- LIFECYCLE & NAVIGATION ---
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.fragment.ktx)

    // --- KOTLIN & COROUTINES ---
    implementation(libs.kotlinx.coroutines.android)

    // --- DESUGARING ---
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // --- TESTING UNITARIO (JUnit 5) ---
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.arch.core.testing)

    // --- TESTING INSTRUMENTAL (Android) ---
    // Por ahora mantenemos compatibilidad básica.
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.javax.inject)

}
