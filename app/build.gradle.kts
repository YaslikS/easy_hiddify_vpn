import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidHilt)
}

android {
    namespace = "com.yasliks.easy_hiddify_vpn"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.yasliks.easy_hiddify_vpn"
        minSdk = 26
        targetSdk = 37
        versionCode = 17
        versionName = "2.2.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
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
            storeFile = file("key.jks")
            keyAlias = "easy_key"
            storePassword = "f4wv435c"
            keyPassword = "f4wv435c"
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