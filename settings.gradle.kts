pluginManagement {
    val springBootVersion: String by settings
    plugins {
        id("org.springframework.boot") version springBootVersion
    }
}

rootProject.name = "ever_age_eshop_backend"
