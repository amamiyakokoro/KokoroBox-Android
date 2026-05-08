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

package com.github.yumelira.yumebox.presentation.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Retains scroll states for root-level pages outside individual page composition.
 *
 * Main pager pages can be disposed and recreated when navigating to another destination
 * or when the ACG home changes layout/prefetch behavior. Storing the actual lazy state
 * instances here avoids repeatedly serializing/restoring positions during layout changes,
 * which can otherwise overwrite the last user-visible position with a transient one.
 */
object PageScrollStateStore {
    private val lazyListStates = linkedMapOf<String, LazyListState>()
    private val lazyGridStates = linkedMapOf<String, LazyGridState>()

    fun lazyListState(key: String): LazyListState {
        return lazyListStates.getOrPut(key) { LazyListState() }
    }

    fun lazyGridState(key: String): LazyGridState {
        return lazyGridStates.getOrPut(key) { LazyGridState() }
    }

    fun clear(key: String) {
        lazyListStates.remove(key)
        lazyGridStates.remove(key)
    }

    fun clearAll() {
        lazyListStates.clear()
        lazyGridStates.clear()
    }
}

@Composable
fun rememberRetainedLazyListState(key: String): LazyListState {
    return remember(key) { PageScrollStateStore.lazyListState(key) }
}

@Composable
fun rememberRetainedLazyGridState(key: String): LazyGridState {
    return remember(key) { PageScrollStateStore.lazyGridState(key) }
}
