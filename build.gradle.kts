import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    application
    id("net.ltgt.errorprone") version "4.1.0"
}

group = "dev.labs.httpserver"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    compileOnly("org.jspecify:jspecify:1.0.0")
    errorprone("com.google.errorprone:error_prone_core:2.37.0")
    errorprone("com.uber.nullaway:nullaway:0.12.6")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("dev.labs.WebServer")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableAllChecks.set(true)
        option("NullAway:OnlyNullMarked", "true")
        error("NullAway")
    }
}

