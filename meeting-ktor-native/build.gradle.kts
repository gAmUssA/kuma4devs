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
    //id("io.ktor.plugin") version "2.3.4"
    id("com.google.cloud.tools.jib") version "3.4.0"
}

repositories {
    mavenCentral()
}

kotlin {

    //val hostOs = System.getProperty("os.name")
    //val arch = System.getProperty("os.arch")
    /*val nativeTarget = when {
        hostOs == "Mac OS X" && arch == "x86_64" -> macosX64("native")
        hostOs == "Mac OS X" && arch == "aarch64" -> macosArm64("native")
        hostOs == "Linux" -> linuxX64("native")
        // Other supported targets are listed here: https://ktor.io/docs/native-server.html#targets
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }*/


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
        //image = "gcr.io/distroless/base"
        image = "bellsoft/alpaquita-linux-base"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            /*                platform {
                                architecture = "arm64"
                                os = "linux"
                            }*/
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
