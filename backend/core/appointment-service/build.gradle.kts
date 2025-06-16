// healthcare-admin-dashboard/backend/core/appointment-service/build.gradle.kts
import com.google.protobuf.gradle.*
import com.google.protobuf.gradle.GenerateProtoTask

plugins {
  id("org.springframework.boot")
  id("io.spring.dependency-management")
  kotlin("jvm")
  kotlin("plugin.spring")
  id("com.google.protobuf")
}

group = "com.healthcare"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // gRPC
    implementation("io.grpc:grpc-netty-shaded:1.59.0")
    implementation("io.grpc:grpc-protobuf:1.59.0")
    implementation("io.grpc:grpc-stub:1.59.0")
    implementation("io.grpc:grpc-kotlin-stub:1.4.0")
    implementation("com.google.protobuf:protobuf-kotlin:3.23.4")
    implementation("net.devh:grpc-server-spring-boot-starter:2.14.0.RELEASE")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")

    // Database
    implementation("org.postgresql:postgresql")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}


protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.24.0" }
    plugins {
      id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.58.0" }
    }
    generateProtoTasks {
      all().configureEach {
        plugins { id("grpc") } // only grpc here—java is on by default
      }
    }
  }

sourceSets {
    main {
        proto {
            // your module's own protos (if any)
            // srcDir("src/main/proto")
            // *plus* compile the shared appointment/v1 folder
            srcDir("$rootDir/backend/shared/protos")
        }
        java {
            srcDir("build/generated/source/proto/main/grpc")
            srcDir("build/generated/source/proto/main/java")
        }
        kotlin {
            srcDir("src/main/kotlin")
            // keep your Kotlin grpc stubs if you generate them
            srcDir("build/generated/source/proto/main/grpckt")
            srcDir("build/generated/source/proto/main/kotlin")
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn("generateProto")
}

// Add this to ensure clean removes generated files
tasks.named("clean") {
    delete("build/generated")
}
