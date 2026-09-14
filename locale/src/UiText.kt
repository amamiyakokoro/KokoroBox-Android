/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.core.locale

import android.content.res.Resources
import java.util.Locale

/** A UI message resolved only at the Android presentation boundary. */
sealed interface UiText {
    data class Resource(
        val id: Int,
        val formatArgs: List<Any> = emptyList(),
    ) : UiText

    data class Dynamic(val value: String) : UiText
}

fun UiText.resolve(resources: Resources): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> String.format(
        resources.configuration.locales[0] ?: Locale.getDefault(),
        resources.getString(id),
        *formatArgs.toTypedArray(),
    )
}
