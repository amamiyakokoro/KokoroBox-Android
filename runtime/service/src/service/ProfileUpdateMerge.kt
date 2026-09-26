package com.amamiyakokoro.box.service

import com.amamiyakokoro.box.service.runtime.entity.Imported
import java.io.IOException

internal fun hasSameProfileDownloadSettings(before: Imported, current: Imported): Boolean =
    before.source == current.source && before.type == current.type && before.userAgent == current.userAgent

/** Merge server-owned fields without reverting edits made while the download was in flight. */
internal fun mergeProfileUpdate(before: Imported, current: Imported, downloaded: Imported): Imported? {
    if (!hasSameProfileDownloadSettings(before, current)) return null
    return current.copy(
        name = if (current.name == before.name) downloaded.name else current.name,
        interval = if (current.interval == before.interval) downloaded.interval else current.interval,
        upload = downloaded.upload,
        download = downloaded.download,
        total = downloaded.total,
        expire = downloaded.expire,
        lastUpdateAttemptAt = downloaded.lastUpdateAttemptAt,
        lastUpdateFailed = downloaded.lastUpdateFailed,
    )
}

internal class ProfileUpdateSupersededException :
    IOException("Subscription settings changed during download. Update the profile again.")
