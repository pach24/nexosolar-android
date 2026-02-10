plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


kotlin {
    jvmToolchain(11)
}

dependencies {

    implementation(libs.annotations)


    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
}
