import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("maven-publish")
    id("checkstyle")
    id("org.springframework.boot")
}

group = "com.everage.eshop"
version = "1.0.0-SNAPSHOT"


// Apply a specific Java toolchain to ease working on different environments.
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // SPRING DEPENDENCIES
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    annotationProcessor(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

    // SPRING DEPENDENCIES
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    // Swagger/OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.property("springdocVersion")}")

    // Email
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // Rate limiting
    implementation("com.bucket4j:bucket4j-core:${project.property("bucket4jVersion")}")

    // Logstash Logback Encoder (JSON logs)
    implementation("net.logstash.logback:logstash-logback-encoder:${project.property("logstashLogbackEncoderVersion")}")

    // DB
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("io.hypersistence:hypersistence-utils-hibernate-71:${project.property("hypersistenceUtilsVersion")}")

    // Utils
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.mapstruct:mapstruct:${project.property("mapStructVersion")}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${project.property("mapStructVersion")}")

    // Payment Gateway
    implementation("com.stripe:stripe-java:${project.property("stripeJavaVersion")}")
    // Stripe's SDK uses Gson internally; declared explicitly so ApiResource.GSON is
    // usable at compile time (transitively available, but not exported by stripe-java)
    implementation("com.google.code.gson:gson:${project.property("gsonVersion")}")

    // HTTP Client for Shipping API
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Cloudflare R2 / S3-compatible storage
    implementation("software.amazon.awssdk:s3:${project.property("awsSdkS3Version")}")

    // TEST
    "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    "testImplementation"("org.springframework.boot:spring-boot-starter-webmvc-test")
    "testImplementation"("org.springframework.boot:spring-boot-starter-security-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<BootJar>("bootJar") {
    archiveFileName.set("everage-service.jar")
    // launchScript() removed — incompatible with layered JAR extraction in Docker
}

// Turn off default jar task (we need only bootJar)
tasks.named<Jar>("jar") {
    enabled = false
}

// ========================================
// CheckStyle Configuration
// ========================================
checkstyle {
    toolVersion = project.property("checkstyleToolVersion").toString()
    configFile = file("${rootProject.projectDir}/config/checkstyle/checkstyle-main.xml")
}

tasks.named<Checkstyle>("checkstyleMain") {
    configFile = file("${rootProject.projectDir}/config/checkstyle/checkstyle-main.xml")

    // Exclude generated code and build artifacts
    exclude("**/generated/**")
    exclude("**/build/**")
}

tasks.named<Checkstyle>("checkstyleTest") {
    configFile = file("${rootProject.projectDir}/config/checkstyle/checkstyle-test.xml")

    // Exclude generated code and build artifacts
    exclude("**/generated/**")
    exclude("**/build/**")
}

tasks.withType<Checkstyle> {
    reports {
        xml.required = true
        html.required = true
    }
}

// Make build depend on checkstyleMain to check imports automatically
tasks.named("build") {
    dependsOn("checkstyleMain")
}

// ========================================
// Maven Publishing Configuration
// ========================================
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.everage.eshop"
            artifactId = "everage-service"
            version = project.version.toString()

            // Publish bootJar
            artifact(tasks.named("bootJar"))
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${getGithubRepository()}")
            credentials {
                username = getGithubActor()
                password = getGithubToken()
            }
        }
    }
}

// ========================================
// Helper Functions
// ========================================
fun getGithubRepository(): String {
    return System.getenv("GITHUB_REPOSITORY")
        ?: findProperty("github.repository")?.toString()
        ?: "goosza/everage"
}

fun getGithubActor(): String {
    return System.getenv("GITHUB_ACTOR")
        ?: findProperty("gpr.user")?.toString()
        ?: ""
}

fun getGithubToken(): String {
    return System.getenv("GITHUB_TOKEN")
        ?: findProperty("gpr.key")?.toString()
        ?: ""
}

fun getVersionFromGit(): String {
    val tagVersion = System.getenv("GITHUB_REF")
    return if (tagVersion != null && tagVersion.startsWith("refs/tags/v")) {
        tagVersion.removePrefix("refs/tags/v")
    } else {
        project.version.toString()
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
