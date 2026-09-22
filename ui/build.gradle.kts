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
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

android {
    namespace = "com.amamiyakokoro.box.core.ui"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":platform"))
    implementation(project(":locale"))
    implementation(project(":data"))
    implementation(project(":runtime:api"))

    val composeBom = platform("androidx.compose:compose-bom:${libs.versions.composeBom.get()}")
    implementation(composeBom)
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3:1.5.0-alpha28")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.core:core-ktx:${libs.versions.coreKtx.get()}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.lifecycle.get()}")
    implementation("io.github.raamcosta.compose-destinations:core:${libs.versions.composeDestinations.get()}")
    implementation("io.github.panpf.sketch4:sketch-compose:${libs.versions.sketch4.get()}")
    implementation("io.github.panpf.sketch4:sketch-http:${libs.versions.sketch4.get()}")
    implementation("io.github.panpf.sketch4:sketch-svg:${libs.versions.sketch4.get()}")
    implementation("io.github.panpf.sketch4:sketch-animated-gif:${libs.versions.sketch4.get()}")
    implementation("io.github.panpf.sketch4:sketch-animated-webp:${libs.versions.sketch4.get()}")
    implementation("io.github.panpf.sketch4:sketch-compose-resources:${libs.versions.sketch4.get()}")
    implementation("dev.chrisbanes.haze:haze:${libs.versions.haze.get()}")
    implementation("io.github.kyant0:shapes:1.2.1")

    api("top.yukonga.miuix.kmp:miuix-ui:${libs.versions.miuix.get()}")
    api("top.yukonga.miuix.kmp:miuix-preference:${libs.versions.miuix.get()}")
    api("top.yukonga.miuix.kmp:miuix-icons:${libs.versions.miuix.get()}")
    api("top.yukonga.miuix.kmp:miuix-blur-android:${libs.versions.miuix.get()}")
}
