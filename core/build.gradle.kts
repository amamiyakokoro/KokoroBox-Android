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


android {
    namespace = "${providers.gradleProperty("project.namespace.base").get()}.core"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

extensions.configure<com.android.build.api.dsl.LibraryExtension>("android") {
    sourceSets.getByName("test").kotlin.directories.apply {
        clear()
        add("test")
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:${libs.versions.lifecycle.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${libs.versions.coroutines.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.serializationJson.get()}")
    implementation("androidx.annotation:annotation-jvm:${libs.versions.annotationJvm.get()}")
    implementation("com.jakewharton.timber:timber:${libs.versions.timber.get()}")
}
