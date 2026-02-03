plugins {
    id("java-library")
    kotlin("jvm")
}
java {
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(11)
}