import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

buildscript {
    dependencies {
        classpath("com.google.cloud.tools:jib-native-image-extension-gradle:0.1.0")
    }
}

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

group = "builders.kong"
version = "0.0.1"
val buildNumber by extra("0")

val myDockerImageName = "gamussa/mesh4java-meet-ktor-native"
val myDockerTag = setOf("$version", "$version.${extra["buildNumber"]}")

plugins {
    application
    kotlin("multiplatform") version "1.9.10"
    id("io.ktor.plugin") version "2.3.5"
    id("com.google.cloud.tools.jib") version "3.4.0"
}

repositories {
    mavenCentral()
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

kotlin {
    linuxX64 {
        binaries {
            executable(listOf(DEBUG, RELEASE)) {
                entryPoint = "main"
                linkerOpts("--as-needed")
                freeCompilerArgs += "-Xoverride-konan-properties=linkerGccFlags.linux_x64=-lgcc -lgcc_eh -lc"
            }
        }
    }

    macosArm64 {
        binaries {
            executable(listOf(DEBUG, RELEASE)) {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-core:$ktor_version")
                implementation("io.ktor:ktor-server-cio:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-server-test-host:$ktor_version")
            }
        }
    }
}

tasks.register<Copy>("copyLinuxBinary") {
    dependsOn(tasks.first { it.name.contains("linkReleaseExecutableLinuxX64") })
    from(layout.buildDirectory.file("bin/linuxX64/releaseExecutable/meeting-ktor-native.kexe"))
    into(layout.buildDirectory.dir("native/nativeCompile"))
}

tasks.withType<com.google.cloud.tools.jib.gradle.JibTask> {
    dependsOn("copyLinuxBinary")
}

jib {
    to {
        image = myDockerImageName
        tags = myDockerTag
    }
    from {
        image = "gcr.io/distroless/base"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    pluginExtensions {
        pluginExtension {
            implementation = "com.google.cloud.tools.jib.gradle.extension.nativeimage.JibNativeImageExtension"
            properties = mapOf(Pair("imageName", "meeting-ktor-native.kexe"))
        }
    }
    container {
        mainClass = "ApplicationKt"
    }
}

tasks.named<Test>("test") {
    testLogging {
        outputs.upToDateWhen { false }
        outputs.upToDateWhen { false }
        showStandardStreams = false
        events = setOf(PASSED, SKIPPED, FAILED)
        exceptionFormat = FULL
    }
}