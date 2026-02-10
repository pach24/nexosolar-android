plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)  // ✅ Usa la versión del catálogo (2.0.21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    // ✅ Kotlin 2.0+ necesita stdlib-jdk8 para extension functions
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")  // Sin versión (hereda 2.0.21)
    implementation(libs.annotations)

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")  // Sin versión
}

tasks.test {
    useJUnitPlatform()
}
