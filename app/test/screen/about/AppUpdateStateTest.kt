package com.amamiyakokoro.box.screen.about

import com.amamiyakokoro.box.data.integration.update.ReleaseCheck
import com.amamiyakokoro.box.data.integration.update.ReleaseVersion
import com.amamiyakokoro.box.data.integration.update.isNewerThan
import com.amamiyakokoro.box.integration.update.AppUpdateInstallState
import org.junit.Assert.*
import org.junit.Test

class AppUpdateStateTest {
    private val release = ReleaseCheck.Published(
        tag = "v0.8.6", version = ReleaseVersion(0, 8, 6), notes = "",
        releaseUrl = "https://github.com/amamiyakokoro/KokoroBox-Android/releases/tag/v0.8.6",
        apkName = null, apkUrl = null, apkSizeBytes = null, checksumUrl = null,
    )

    @Test fun newVersionCheckIgnoresPreviousOrLateInstallCompletion() {
        val dialog = AppUpdateState(result = release)
        assertTrue(release.isNewerThan("0.8.5", 8500))
        assertSame(AppUpdateInstallState.Idle, dialog.installStateForDialog(AppUpdateInstallState.Installed))
        assertSame(release, dialog.result)
    }

    @Test fun checkFailureIsNotReplacedByAnOlderInstallation() {
        val dialog = AppUpdateState(result = ReleaseCheck.Failure.Network)
        listOf(
            AppUpdateInstallState.Installed,
            AppUpdateInstallState.Installing(release),
            AppUpdateInstallState.Failed(1),
        ).forEach { assertSame(AppUpdateInstallState.Idle, dialog.installStateForDialog(it)) }
        assertSame(ReleaseCheck.Failure.Network, dialog.result)
    }

    @Test fun explicitlyRequestedInstallationStillShowsProgressErrorsAndCompletion() {
        val dialog = AppUpdateState(result = release, installationRequested = true)
        listOf(
            AppUpdateInstallState.Downloading(release, 10, 100),
            AppUpdateInstallState.InstallPermissionRequired(release),
            AppUpdateInstallState.Installing(release),
            AppUpdateInstallState.WaitingForUserConfirmation(release),
            AppUpdateInstallState.Failed(1),
            AppUpdateInstallState.Installed,
        ).forEach { assertSame(it, dialog.installStateForDialog(it)) }
    }

    @Test fun checkingAgainStartsANewSessionEvenAfterInstallation() {
        val completed = AppUpdateState(result = release, installationRequested = true)
        assertSame(AppUpdateInstallState.Installed, completed.installStateForDialog(AppUpdateInstallState.Installed))
        val checking = AppUpdateState(checking = true)
        assertSame(AppUpdateInstallState.Idle, checking.installStateForDialog(AppUpdateInstallState.Installed))
        assertNull(checking.result)
        assertFalse(AppUpdateState(result = release).installationRequested)
    }
}
