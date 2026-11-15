plugins {
    kotlin("multiplatform")
}

group = "com.dremonj.apppersonal"
version = "1.0"

kotlin {
    jvmToolchain(21)

    // JVM: para compilar y ejecutar los tests en Windows.
    jvm()

    // iPhone real. Genera el framework "Shared" que usa Xcode.
    iosArm64 {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
