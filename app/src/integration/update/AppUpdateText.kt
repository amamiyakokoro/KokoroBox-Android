package com.amamiyakokoro.box.integration.update

import android.content.Context
import com.amamiyakokoro.box.data.integration.update.ReleaseCheck
import com.amamiyakokoro.box.core.locale.R as LocaleR

internal fun ReleaseCheck.Published.versionLabel(context: Context): String =
    versionCode?.let { build ->
        context.getString(
            LocaleR.string.about_update_nightly_version,
            "v${version.major}.${version.minor}.${version.patch}",
            build,
        )
    } ?: tag
