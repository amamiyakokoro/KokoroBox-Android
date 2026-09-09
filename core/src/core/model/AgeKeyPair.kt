/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AgeKeyPair(
    val secretKey: String,
    val publicKey: String,
)
