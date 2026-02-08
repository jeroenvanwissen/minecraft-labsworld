import java.io.File
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    jacoco
}

group = "nl.jeroenlabs"
version = "0.0.1-SNAPSHOT"

val minecraftServerVersion = "1.21.11"
val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.github.twitch4j:twitch4j:1.25.0")
    implementation("com.github.philippheuer.credentialmanager:credentialmanager:0.3.1")

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

val jacocoMinCoverage =
    providers
        .gradleProperty("jacocoMinCoverage")
        .map { it.toBigDecimal() }
        .orElse(0.0.toBigDecimal())

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = jacocoMinCoverage.get()
                }
            }
        }
    }

    named("check") {
        dependsOn(jacocoTestCoverageVerification)
    }

    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion(minecraftServerVersion)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

val serverPluginsDir =
    providers
        .gradleProperty("serverPluginsDir")
        .orElse(providers.environmentVariable("SERVER_PLUGINS_DIR"))
        .orElse("../server/minecraft-plugins")

val serverPluginsDirFile =
    serverPluginsDir.map { path ->
        val file = File(path)
        if (file.isAbsolute) file else layout.projectDirectory.dir(path).asFile
    }

val shadowJarTask =
    tasks.named<ShadowJar>("shadowJar") {
        mergeServiceFiles()

        // Produce a single, predictable jar file for the server's /plugins folder.
        archiveBaseName.set("LabsWorld")
        archiveClassifier.set("")
    }

tasks.named("assemble") {
    dependsOn(shadowJarTask)
}

tasks.register<Copy>("installPlugin") {
    group = "distribution"
    description = "Builds the shaded jar and copies it into the server plugins directory."

    dependsOn(shadowJarTask)

    from(shadowJarTask.flatMap { it.archiveFile })
    into(serverPluginsDirFile)

    doFirst {
        logger.lifecycle("Installing plugin jar to: ${serverPluginsDirFile.get()}")
    }
}

tasks.register("buildAndInstall") {
    group = "distribution"
    description = "Runs a full build and installs the plugin jar to the server plugins directory."
    dependsOn("build")
    finalizedBy("installPlugin")
}
