/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c)  AmamiyaKokoro 2025 - Present
 *
 */

@file:Suppress("UnstableApiUsage")

import java.util.*
import org.gradle.api.tasks.Exec

plugins {
    id("com.android.application")
    kotlin("plugin.serialization")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
    id("com.mikepenz.aboutlibraries.plugin.android")
}

val projectName = providers.gradleProperty("project.name").get()
val projectNamespace = providers.gradleProperty("project.namespace.base").get()
val projectVersionName = providers.gradleProperty("project.version.name").get()
val projectVersionCode = providers.gradleProperty("project.version.code").map(String::toInt).get()
val androidTargetSdk = providers.gradleProperty("android.targetSdk").map(String::toInt).get()
val androidJvm = providers.gradleProperty("android.jvm").get()
val defaultAppAbis = providers.gradleProperty("abi.app.list").get()

base {
    archivesName.set(projectName)
}


val signingPropsFile = rootProject.file("signing.properties")
val signingFileProps = if (signingPropsFile.exists()) {
    Properties().apply { signingPropsFile.inputStream().use(::load) }
} else {
    null
}

val injectedAbi = (
    rootProject.extra.properties["forcedBuildAbi"] as? String
        ?: providers.gradleProperty("android.injected.build.abi").orNull
)?.trim()?.takeIf { it.isNotEmpty() }

val appAbiList = injectedAbi
    ?.split(',')
    ?.map { it.trim() }
    ?.filter { it.isNotEmpty() }
    ?.takeIf { it.isNotEmpty() }
    ?: defaultAppAbis.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

val isSingleAbiPackage = injectedAbi != null
val startupGateEnabled = providers.gradleProperty("startupGate.enabled").orNull
    ?.trim()
    ?.ifEmpty { null }
    ?: signingFileProps?.getProperty("startupGate.enabled")
        ?.trim()
        ?.ifEmpty { null }
    ?: "false"
val startupGateEnforceSigner = providers.gradleProperty("startupGate.enforceSigner").orNull
    ?.trim()
    ?.ifEmpty { null }
    ?: signingFileProps?.getProperty("startupGate.enforceSigner")
        ?.trim()
        ?.ifEmpty { null }
    ?: "false"
val startupGateExpectedSignerSha256 = providers.gradleProperty("startupGate.expectedSignerSha256").orNull
    ?.trim()
    ?.ifEmpty { "" }
    ?: signingFileProps?.getProperty("startupGate.expectedSignerSha256")
        ?.trim()
        ?.ifEmpty { "" }
    ?: ""

val projectApplicationId = providers.gradleProperty("project.applicationId")
    .orElse(projectNamespace)
    .get()

val bridgeJniPackage = "${projectNamespace.replace('.', '/')}/core/bridge"
val bridgeJniClass = "$bridgeJniPackage/Bridge"
val bridgeCallbackJniClass = "$bridgeJniPackage/TunInterface"
val nativeBridgeSource = rootProject.file("lib/native/cpp/main.cpp")
val nativeCompilerSource = rootProject.file("lib/native/rust/src/lib.rs")
val nativeBridgeLibraries = appAbiList.map { abi ->
    rootProject.file("jniLibs/$abi/libbridge.so")
}
val nativeOverrideLibraries = appAbiList.map { abi ->
    rootProject.file("jniLibs/$abi/liboverride.so")
}
val nativeClashLibraries = appAbiList.map { abi ->
    rootProject.file("jniLibs/$abi/libclash.so")
}

val verifyBridgeJniNamespace = tasks.register<Exec>("verifyBridgeJniNamespace") {
    group = "verification"
    description = "Verifies that packaged native libraries match the Kotlin JNI namespace."

    commandLine(
        listOf(
            "python3",
            rootProject.file("scripts/verify-native-bridge.py").absolutePath,
            "--cpp-source",
            nativeBridgeSource.absolutePath,
            "--rust-source",
            nativeCompilerSource.absolutePath,
            "--bridge-class-name",
            bridgeJniClass,
            "--callback-class-name",
            bridgeCallbackJniClass,
            "--root",
            rootProject.projectDir.absolutePath,
            "--bridge-libraries",
        ) + nativeBridgeLibraries.map { it.absolutePath } + listOf(
            "--override-libraries",
        ) + nativeOverrideLibraries.map { it.absolutePath } + listOf(
            "--other-libraries",
        ) + nativeClashLibraries.map { it.absolutePath },
    )
}

tasks.configureEach {
    if (name.startsWith("merge") && name.endsWith("NativeLibs")) {
        dependsOn(verifyBridgeJniNamespace)
    }
}

// The JNI bridge reports this project's Git revision, not the upstream Mihomo release.
// Keep the About screen aligned with the core revision selected by the build configuration.
val mihomoVersion = providers.fileContents(rootProject.layout.projectDirectory.file("kernel.properties"))
    .asText.map { contents ->
        val properties = Properties().apply { load(contents.reader()) }
        val revision = requireNotNull(properties.getProperty("external.mihomo.branch")) {
            "Missing Mihomo revision in kernel.properties"
        }.trim().also { require(it.isNotEmpty()) { "Empty Mihomo revision" } }
        revision + properties.getProperty("external.mihomo.suffix", "").trim()
    }

android {
    namespace = projectNamespace

    bundle {
        language {
            enableSplit = false
        }
    }

    defaultConfig {
        applicationId = projectApplicationId
        targetSdk = androidTargetSdk
        versionCode = projectVersionCode
        versionName = projectVersionName
        buildConfigField(
            "String",
            "MIHOMO_VERSION",
            "\"${mihomoVersion.get().replace("\\", "\\\\").replace("\"", "\\\"")}\"",
        )
        manifestPlaceholders["appName"] = projectName
        manifestPlaceholders["startupGateEnabled"] = startupGateEnabled
        manifestPlaceholders["startupGateEnforceSigner"] = startupGateEnforceSigner
        manifestPlaceholders["startupGateExpectedSignerSha256"] = startupGateExpectedSignerSha256

        if (isSingleAbiPackage) {
            ndk {
                abiFilters += appAbiList
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(androidJvm)
        targetCompatibility = JavaVersion.toVersion(androidJvm)
        isCoreLibraryDesugaringEnabled = true
    }

    sourceSets {
        getByName("main") {
            kotlin.directories.apply {
                clear()
                add("src")
            }
            res.directories.apply {
                clear()
                add("res")
            }
            assets.directories.apply {
                clear()
                add("assets")
            }
            aidl.directories.apply {
                clear()
                add("aidl")
            }
            resources.directories.apply {
                clear()
                add("resources")
            }
            jniLibs.directories.apply {
                clear()
                add("../jniLibs")
            }
            if (project.file("AndroidManifest.xml").isFile) {
                manifest.srcFile("AndroidManifest.xml")
            }
        }
    }

    androidResources {
        generateLocaleConfig = false
    }

    buildFeatures {
        compose = true
        buildConfig = true
        aidl = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    splits {
        abi {
            //noinspection WrongGradleMethod
            isEnable = !isSingleAbiPackage &&
                gradle.startParameter.taskNames.none { it.contains("bundle", ignoreCase = true) }
            reset()
            include(*appAbiList.toTypedArray())
            isUniversalApk = false
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    signingConfigs {
        val resolvedStoreFilePath = providers.gradleProperty("signing.store.file").orNull
            ?: signingFileProps?.getProperty("signing.store.file")
            ?: signingFileProps?.getProperty("keystore.file")
            ?: rootProject.file("release.keystore").takeIf { it.exists() }?.absolutePath
        val resolvedStorePassword = providers.gradleProperty("signing.store.password").orNull
            ?: signingFileProps?.getProperty("signing.store.password")
            ?: signingFileProps?.getProperty("keystore.password")
        val resolvedKeyAlias = providers.gradleProperty("signing.key.alias").orNull
            ?: signingFileProps?.getProperty("signing.key.alias")
            ?: signingFileProps?.getProperty("key.alias")
        val resolvedKeyPassword = providers.gradleProperty("signing.key.password").orNull
            ?: signingFileProps?.getProperty("signing.key.password")
            ?: signingFileProps?.getProperty("key.password")

        if (!resolvedStoreFilePath.isNullOrBlank() &&
            !resolvedStorePassword.isNullOrBlank() &&
            !resolvedKeyAlias.isNullOrBlank() &&
            !resolvedKeyPassword.isNullOrBlank()
        ) {
            create("release") {
                storeFile = rootProject.file(resolvedStoreFilePath)
                storePassword = resolvedStorePassword
                keyAlias = resolvedKeyAlias
                keyPassword = resolvedKeyPassword
            }
        }
    }

    if (signingConfigs.findByName("release") != null) {
        buildTypes.named("release").configure {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

//noinspection WrongGradleMethod
androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val splitAbiName = output.filters.find {
                it.filterType == com.android.build.api.variant.FilterConfiguration.FilterType.ABI
            }?.identifier
            val abiName = injectedAbi ?: splitAbiName ?: "universal"
            val buildTypeName = variant.buildType ?: "release"
            output.versionName.set(projectVersionName)
            (output as com.android.build.api.variant.impl.VariantOutputImpl).outputFileName.set(
                "$projectName-v$projectVersionName-${abiName}-${buildTypeName}.apk"
            )
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${libs.versions.desugarJdkLibs.get()}")

    implementation(project(":core"))
    implementation(project(":platform"))
    implementation(project(":locale"))
    implementation(project(":ui"))
    implementation(project(":data"))
    implementation(project(":runtime:api"))
    implementation(project(":runtime:client"))
    implementation(project(":runtime:service"))
    implementation(project(":feature:proxy"))
    implementation(project(":feature:override"))
    implementation(project(":feature:editor"))
    implementation(project(":feature:meta"))

    val composeBom = platform("androidx.compose:compose-bom:${libs.versions.composeBom.get()}")
    implementation(composeBom)
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:${libs.versions.activityCompose.get()}")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("top.yukonga.miuix.kmp:miuix-ui:${libs.versions.miuix.get()}")
    implementation("top.yukonga.miuix.kmp:miuix-preference:${libs.versions.miuix.get()}")
    implementation("top.yukonga.miuix.kmp:miuix-icons:${libs.versions.miuix.get()}")
    implementation("top.yukonga.miuix.kmp:miuix-blur-android:${libs.versions.miuix.get()}")
    implementation("dev.chrisbanes.haze:haze:${libs.versions.haze.get()}")
    implementation("androidx.navigationevent:navigationevent-compose:${libs.versions.navigationevent.get()}")

    implementation("com.tencent:mmkv:${libs.versions.mmkv.get()}")

    implementation("io.insert-koin:koin-core:${libs.versions.koin.get()}")
    implementation("io.insert-koin:koin-android:${libs.versions.koin.get()}")
    implementation("io.insert-koin:koin-androidx-compose:${libs.versions.koin.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${libs.versions.coroutines.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.serializationJson.get()}")

    implementation("io.github.raamcosta.compose-destinations:core:${libs.versions.composeDestinations.get()}")
    ksp("io.github.raamcosta.compose-destinations:ksp:${libs.versions.composeDestinations.get()}")

    implementation("com.android.tools.smali:smali-dexlib2:${libs.versions.smaliDexlib2.get()}") {
        exclude(group = "com.google.guava", module = "guava")
    }

    implementation("com.jakewharton.timber:timber:${libs.versions.timber.get()}")
    implementation("org.tukaani:xz:1.12")

    implementation("com.google.mlkit:barcode-scanning:${libs.versions.mlkitBarcodeScanning.get()}")

    implementation("androidx.camera:camera-camera2:${libs.versions.camera.get()}")
    implementation("androidx.camera:camera-lifecycle:${libs.versions.camera.get()}")
    implementation("androidx.camera:camera-view:${libs.versions.camera.get()}")
    implementation("androidx.camera:camera-core:${libs.versions.camera.get()}")

    implementation("io.github.panpf.sketch4:sketch-compose:${libs.versions.sketch4.get()}")
    implementation("io.github.panpf.sketch4:sketch-http:${libs.versions.sketch4.get()}")
    implementation("io.github.panpf.sketch4:sketch-animated-gif:${libs.versions.sketch4.get()}")
    implementation("io.github.panpf.sketch4:sketch-animated-webp:${libs.versions.sketch4.get()}")

    implementation("sh.calvin.reorderable:reorderable:${libs.versions.reorderable.get()}")
    implementation("com.mikepenz:aboutlibraries-core:${libs.versions.aboutLibraries.get()}")
    implementation("com.mikepenz:aboutlibraries-compose:${libs.versions.aboutLibraries.get()}")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${libs.versions.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.lifecycle.get()}")
    implementation("androidx.work:work-runtime-ktx:${libs.versions.work.get()}")
    implementation("dev.rikka.shizuku:api:${libs.versions.shizuku.get()}")
    implementation("dev.rikka.shizuku:provider:${libs.versions.shizuku.get()}")

    implementation("com.squareup.okhttp3:okhttp:${libs.versions.okhttp.get()}")
    implementation("androidx.biometric:biometric:${libs.versions.biometric.get()}")
}

ksp {
    arg("compose-destinations.defaultTransitions", "none")
}
