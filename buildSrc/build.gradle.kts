plugins {

}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.jvm)
    implementation("net.linguica.gradle:maven-settings-plugin:0.5")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.1")
    implementation("io.freefair.gradle:lombok-plugin:8.10")
}