plugins {
    kotlin("multiplatform") version "2.4.21"
}

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        useEsModules()
        browser()
        nodejs()
        binaries.executable()
    }

    sourceSets {
        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
