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
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

android {
    namespace = "com.amamiyakokoro.box.feature.override"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":platform"))
    implementation(project(":locale"))
    implementation(project(":ui"))
    implementation(project(":data"))
    implementation(project(":feature:editor"))

    val composeBom = platform("androidx.compose:compose-bom:${libs.versions.composeBom.get()}")
    implementation(composeBom)
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:${libs.versions.activityCompose.get()}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${libs.versions.lifecycle.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${libs.versions.coroutines.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.serializationJson.get()}")
    implementation("io.insert-koin:koin-core:${libs.versions.koin.get()}")
    implementation("io.insert-koin:koin-android:${libs.versions.koin.get()}")
    implementation("io.insert-koin:koin-androidx-compose:${libs.versions.koin.get()}")
    implementation("io.github.raamcosta.compose-destinations:core:${libs.versions.composeDestinations.get()}")
    implementation("com.jakewharton.timber:timber:${libs.versions.timber.get()}")
    implementation("sh.calvin.reorderable:reorderable:${libs.versions.reorderable.get()}")
}
