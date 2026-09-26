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

plugins {
    id("com.android.library")
    kotlin("plugin.serialization")
}

extensions.configure<com.android.build.api.dsl.LibraryExtension>("android") {
    namespace = "com.amamiyakokoro.box.runtime.service"
    sourceSets.getByName("test").kotlin.directories.apply {
        clear()
        add("test")
    }
    buildFeatures {
        aidl = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation(project(":core"))
    implementation(project(":platform"))
    implementation(project(":locale"))
    implementation(project(":data"))
    implementation(project(":runtime:api"))

    implementation("androidx.core:core-ktx:${libs.versions.coreKtx.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${libs.versions.coroutines.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.serializationJson.get()}")

    implementation("com.tencent:mmkv:${libs.versions.mmkv.get()}")

    implementation("com.jakewharton.timber:timber:${libs.versions.timber.get()}")
    implementation("com.squareup.okhttp3:okhttp:${libs.versions.okhttp.get()}")
    implementation("com.github.topjohnwu.libsu:core:6.0.0")
    implementation("com.github.topjohnwu.libsu:service:6.0.0")
}
