plugins {
    id("java-library")
    kotlin("jvm")
}
java {
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(11)
}