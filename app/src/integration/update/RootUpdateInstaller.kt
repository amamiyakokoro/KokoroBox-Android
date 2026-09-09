package com.amamiyakokoro.box.integration.update

import com.amamiyakokoro.box.service.root.RootPackageShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Installs a verified APK through the app's existing libsu root shell. */
class RootUpdateInstaller {
    suspend fun install(apk: File): String = withContext(Dispatchers.IO) {
        val result = RootPackageShell.installApk(apk)
        if (!result.isSuccess) throw IllegalStateException(result.output)
        result.output
    }
}
