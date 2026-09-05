package com.github.yumelira.yumebox.common.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

/** Select one foreground surface, preferring nested modal windows over their parents. */
class NoticeHostRegistry {
    private val ids = AtomicLong()
    private val hosts = mutableMapOf<Long, Int>()
    private val selected = MutableStateFlow<Long?>(null)
    val activeHost = selected.asStateFlow()

    fun newId(): Long = ids.incrementAndGet()

    @Synchronized fun activate(id: Long, depth: Int) {
        hosts[id] = depth
        updateSelection()
    }

    @Synchronized fun deactivate(id: Long) {
        hosts.remove(id)
        updateSelection()
    }

    private fun updateSelection() {
        selected.value = hosts.entries.maxWithOrNull(compareBy({ it.value }, { it.key }))?.key
    }
}
