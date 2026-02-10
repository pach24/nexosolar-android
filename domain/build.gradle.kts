plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // Corrutinas (producción)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ========== TESTING (NO CONTAMINA PRODUCCIÓN) ==========
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Coroutines Testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Mockito para Kotlin
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}

// Habilita JUnit 5 (Jupiter)
tasks.withType<Test> {
    useJUnitPlatform()
}
