plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.nexosolar.android.data.remote"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"https://francisco-pacheco.com/api/\"")
            buildConfigField("String", "API_BASE_URL_2", "\"https://viewnextandroid.mocklab.io/\"")
        }
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://francisco-pacheco.com/api/\"")
            buildConfigField("String", "API_BASE_URL_2", "\"https://viewnextandroid.mocklab.io/\"")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retromock)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
