package com.amamiyakokoro.box.integration.update

import android.os.ParcelFileDescriptor
import java.util.concurrent.TimeUnit

/** Runs under Shizuku or Sui with shell/root identity. */
class ShizukuUpdateUserService : IShizukuUpdateInstaller.Stub() {
    override fun destroy() {
        System.exit(0)
    }

    override fun install(apk: ParcelFileDescriptor, length: Long): String {
        require(length > 0L) { "Verified update APK is empty" }

        val process = ProcessBuilder(
            "pm",
            "install",
            "-r",
            "-S",
            length.toString(),
        ).redirectErrorStream(true).start()

        ParcelFileDescriptor.AutoCloseInputStream(apk).use { input ->
            process.outputStream.use { output -> input.copyTo(output) }
        }

        val output = process.inputStream.bufferedReader().use { it.readText().trim() }
        if (!process.waitFor(INSTALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            throw IllegalStateException("Shizuku package installation timed out")
        }
        if (process.exitValue() != 0 || !output.contains("Success", ignoreCase = true)) {
            throw IllegalStateException(output.ifBlank { "Shizuku package installation failed" })
        }
        return output
    }

    private companion object {
        const val INSTALL_TIMEOUT_SECONDS = 120L
    }
}
