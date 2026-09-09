package com.github.yumelira.yumebox.integration.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.context.GlobalContext

/** Receives PackageInstaller status after a verified update has been committed. */
class AppUpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        GlobalContext.get().get<AppUpdateManager>().handleInstallResult(intent)
    }
}
