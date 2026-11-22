import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidHilt)
}

// Release signing secrets live outside VCS: keystore.properties (gitignored)
// or RELEASE_* environment variables in CI.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun signingValue(property: String, environment: String): String? =
    keystoreProperties.getProperty(property) ?: System.getenv(environment)

val releaseStorePath = signingValue("storeFile", "RELEASE_STORE_FILE")

val releaseSigningIssue: String? = when {
    releaseStorePath == null ->
        "keystore.properties not found and RELEASE_STORE_FILE is not set"
    !rootProject.file(releaseStorePath).exists() ->
        "keystore file not found: $releaseStorePath"
    signingValue("storePassword", "RELEASE_STORE_PASSWORD") == null ->
        "storePassword is not set"
    signingValue("keyAlias", "RELEASE_KEY_ALIAS") == null ->
        "keyAlias is not set"
    signingValue("keyPassword", "RELEASE_KEY_PASSWORD") == null ->
        "keyPassword is not set"
    else -> null
}

// Android Studio's "Generate Signed App Bundle or APK" injects its own signing
// properties, so a missing keystore.properties must not block that flow.
val ideSigningInjected = project.hasProperty("android.injected.signing.store.file")
val releaseSigningAvailable = releaseSigningIssue == null || ideSigningInjected

android {
    namespace = "com.yasliks.easy_hiddify_vpn"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.yasliks.easy_hiddify_vpn"
        minSdk = 26
        targetSdk = 37
        versionCode = 17
        versionName = "2.2.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    androidResources {
        generateLocaleConfig = true
        localeFilters += listOf("en", "ru", "fr", "zh", "it", "es")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    signingConfigs {
        getByName("debug") { }
        create("release") {
            if (releaseSigningIssue == null) {
                storeFile = rootProject.file(requireNotNull(releaseStorePath))
                storePassword = signingValue("storePassword", "RELEASE_STORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "RELEASE_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "RELEASE_KEY_PASSWORD")
            } else if (!ideSigningInjected) {
                // Keep the signing config non-empty so packaging fails loudly
                // instead of silently producing an unsigned release APK.
                // This is configuration-cache safe (no task actions/script refs).
                storeFile = rootProject.file(
                    "MISSING_RELEASE_KEYSTORE__see_keystore.properties.example",
                )
            }
        }
    }

    flavorDimensions += "server"

    productFlavors {
        create("dev") {
            dimension = "server"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

            buildConfigField("boolean", "IS_PROD", "false")
//            buildConfigField("String", "BASE_URL", "")
        }

        create("prod") {
            dimension = "server"

            buildConfigField("boolean", "IS_PROD", "true")
//            buildConfigField("String", "BASE_URL", "")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
//            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
        }
    }
}

val releasePackagingRequested = gradle.startParameter.taskNames.any { task ->
    val name = task.substringAfterLast(':')
    (name.startsWith("assemble") || name.startsWith("bundle") || name.startsWith("package")) &&
        name.contains("Release", ignoreCase = true)
}

if (releasePackagingRequested && !releaseSigningAvailable) {
    error(
        "Release signing is not configured: $releaseSigningIssue. " +
            "Create keystore.properties in the project root (see keystore.properties.example) " +
            "or set RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD.",
    )
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.coil.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.java)
    implementation(libs.protobuf.java.util)
    implementation(libs.androidx.datastore.core)

    implementation(project(":database"))
    // external dependence
    implementation(project(":easy_hiddify_lib"))
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val date = SimpleDateFormat("dd-MM-yy").format(Date())
            val versionName = output.versionName.get()
            val regexVersion = Regex("\\d*\\.\\d*\\.\\d*")
            val mainVersion = regexVersion.find(versionName)?.value ?: versionName

            val apkName = "easy_vpn_${variant.name}_v${mainVersion}_${date}.apk"
            (output as VariantOutputImpl).outputFileName = apkName
        }
    }
}
