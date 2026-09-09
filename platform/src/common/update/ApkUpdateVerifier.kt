package com.github.yumelira.yumebox.common.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.android.apksig.ApkVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest

data class VerifiedUpdateApk(
    val file: File,
    val versionName: String,
    val versionCode: Long,
    val signerSha256: Set<String>,
)

class ApkUpdateVerificationException(message: String, cause: Throwable? = null) : IOException(message, cause)

/**
 * Verifies a downloaded APK against the installed KokoroBox package.
 *
 * The installed app's signing certificate is the trust anchor. Release metadata and checksums
 * detect transport corruption, but cannot authorize an APK signed by someone else.
 */
class ApkUpdateVerifier(
    private val context: Context,
) {
    suspend fun verify(
        apk: File,
        expectedVersionName: String,
        expectedVersionCode: Long? = null,
    ): VerifiedUpdateApk = withContext(Dispatchers.IO) {
        require(expectedVersionName.isNotBlank()) { "Expected update version is blank" }
        if (!apk.isFile || apk.length() == 0L) {
            throw ApkUpdateVerificationException("Downloaded update APK is missing")
        }

        try {
            val apkResult = ApkVerifier.Builder(apk).build().verify()
            if (!apkResult.isVerified) {
                throw ApkUpdateVerificationException("Downloaded update APK has an invalid signature")
            }

            val packageManager = context.packageManager
            val archive = packageManager.getPackageArchiveInfoCompat(apk.absolutePath)
                ?: throw ApkUpdateVerificationException("Downloaded update is not a valid APK")
            if (archive.packageName != context.packageName) {
                throw ApkUpdateVerificationException("Downloaded update is for a different app")
            }
            val archiveVersionName = archive.versionName
                ?: throw ApkUpdateVerificationException("Downloaded update has no version name")
            if (archiveVersionName != expectedVersionName) {
                throw ApkUpdateVerificationException("Downloaded update version does not match the release")
            }

            val archiveVersionCode = archive.versionCodeCompat()
            if (expectedVersionCode != null && archiveVersionCode != expectedVersionCode) {
                throw ApkUpdateVerificationException("Downloaded update version code does not match the release")
            }

            val installed = packageManager.getPackageInfoCompat(context.packageName)
            if (archiveVersionCode <= installed.versionCodeCompat()) {
                throw ApkUpdateVerificationException("Downloaded update is not newer than the installed app")
            }

            val archiveSigners = archive.signerSha256()
            val installedSigners = installed.signerSha256()
            if (archiveSigners.isEmpty() || installedSigners.isEmpty() || archiveSigners != installedSigners) {
                throw ApkUpdateVerificationException("Downloaded update signer does not match this app")
            }

            VerifiedUpdateApk(
                file = apk,
                versionName = archiveVersionName,
                versionCode = archiveVersionCode,
                signerSha256 = archiveSigners,
            )
        } catch (error: ApkUpdateVerificationException) {
            throw error
        } catch (error: Exception) {
            throw ApkUpdateVerificationException("Unable to verify downloaded update", error)
        }
    }

    private fun PackageManager.getPackageArchiveInfoCompat(path: String): PackageInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageArchiveInfo(
                path,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION")
            getPackageArchiveInfo(path, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            getPackageArchiveInfo(path, PackageManager.GET_SIGNATURES)
        }

    private fun PackageManager.getPackageInfoCompat(packageName: String): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION")
            getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        }

    private fun PackageInfo.versionCodeCompat(): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode else {
            @Suppress("DEPRECATION")
            versionCode.toLong()
        }

    private fun PackageInfo.signerSha256(): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            signingInfo?.apkContentsSigners?.map { it.toByteArray() }.orEmpty()
        } else {
            @Suppress("DEPRECATION")
            signatures?.map { it.toByteArray() }.orEmpty()
        }
        return signatures.mapTo(linkedSetOf()) { signature ->
            MessageDigest.getInstance("SHA-256").digest(signature).toHexString()
        }
    }
}

private fun ByteArray.toHexString(): String = joinToString(separator = "") { byte ->
    "%02x".format(byte.toInt() and 0xFF)
}
