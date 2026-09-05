/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
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
 * Copyright (c)  YumeLira 2025 - Present
 *
 */

plugins {
    id("com.android.library")
    kotlin("plugin.serialization")
}

extensions.configure<com.android.build.api.dsl.LibraryExtension>("android") {
    namespace = "com.github.yumelira.yumebox.data"
    sourceSets.getByName("test").kotlin.directories.apply {
        clear()
        add("test")
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation(project(":core"))
    implementation(project(":locale"))
    implementation(project(":runtime:api"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${gropify.dep.version.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${gropify.dep.version.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${gropify.dep.version.serializationJson}")
    implementation("io.ktor:ktor-client-core:${gropify.dep.version.ktor}")
    implementation("io.ktor:ktor-client-android:${gropify.dep.version.ktor}")
    implementation("io.ktor:ktor-client-content-negotiation:${gropify.dep.version.ktor}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${gropify.dep.version.ktor}")
    implementation("com.squareup.okhttp3:okhttp:${gropify.dep.version.okhttp}")
    implementation("com.jakewharton.timber:timber:${gropify.dep.version.timber}")
    implementation("org.tukaani:xz:1.12")
    implementation("io.insert-koin:koin-core:${gropify.dep.version.koin}")

    implementation("com.tencent:mmkv:${gropify.dep.version.mmkv}")
}
