import java.net.URI
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val serverConfigFile = rootProject.file("app/server.properties")
val serverConfig = Properties().apply {
    require(serverConfigFile.isFile) { "Server settings file is missing: app/server.properties" }
    serverConfigFile.inputStream().use { load(it) }
}
val activationEndpoint = serverConfig.getProperty("activation.endpoint")
    ?.takeIf(String::isNotBlank)
    ?: error("activation.endpoint is missing in app/server.properties")
val activationHost = URI(activationEndpoint).host
    ?: error("activation.endpoint is invalid in app/server.properties")
val cleartextHosts = buildSet {
    add(activationHost)
    addAll(
        serverConfig.getProperty("network.cleartextHosts", "")
            .split(',')
            .map(String::trim)
            .filter(String::isNotBlank),
    )
}
val activationXorKey = serverConfig.getProperty("activation.xorKey")
    ?.takeIf(String::isNotBlank)
    ?: error("activation.xorKey is missing in app/server.properties")
val activationXorKeyLiteral = "\"${activationXorKey.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.galaxytv.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.galaxytv.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "ACTIVATION_ENDPOINT", "\"$activationEndpoint\"")
        buildConfigField("String", "ACTIVATION_XOR_KEY", activationXorKeyLiteral)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets.getByName("main").res.srcDir(layout.buildDirectory.dir("generated/res/activation-security"))
}

val generateActivationNetworkConfig = tasks.register("generateActivationNetworkConfig") {
    val outputDirectory = layout.buildDirectory.dir("generated/res/activation-security")
    outputs.dir(outputDirectory)
    doLast {
        val configFile = outputDirectory.get().file("xml/network_security_config.xml").asFile
        configFile.parentFile.mkdirs()
        val permittedDomains = cleartextHosts.joinToString("\n") {
            "                    <domain includeSubdomains=\"false\">$it</domain>"
        }
        configFile.writeText(
            """<network-security-config>
                <base-config cleartextTrafficPermitted="false" />
                <domain-config cleartextTrafficPermitted="true">
            $permittedDomains
                </domain-config>
            </network-security-config>""".trimIndent(),
        )
    }
}

tasks.configureEach {
    if (name == "preBuild") dependsOn(generateActivationNetworkConfig)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-hls:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
