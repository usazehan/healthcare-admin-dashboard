plugins {
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    kotlin("jvm") version "1.9.0" apply false
    kotlin("plugin.spring") version "1.9.0" apply false
    id("com.google.protobuf") version "0.9.5" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}