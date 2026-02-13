plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nexosolar.android.core"
    compileSdk = 35 // Asegúrate de que coincida con el de tu módulo :app

    defaultConfig {
        minSdk = 24 // Asegúrate de que coincida con el de tu módulo :app

        // El test runner es necesario para ejecutar tests instrumentados (si los hubiera)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.gson)

    // Testing
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

// Configuración de JUnit 5
tasks.withType<Test> {
    useJUnitPlatform()
}
