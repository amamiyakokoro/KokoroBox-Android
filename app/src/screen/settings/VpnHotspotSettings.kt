package com.amamiyakokoro.box.screen.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import org.xmlpull.v1.XmlPullParser

internal object VpnHotspotSettings {
    private const val TETHER_SETTINGS_ACTION = "android.settings.TETHER_SETTINGS"
    private const val VPN_SHARING_KEY = "tethering_allow_vpn_upstreams"
    private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    private const val FRAGMENT_ARGS_KEY = ":settings:fragment_args_key"
    private const val SHOW_FRAGMENT_ARGS = ":settings:show_fragment_args"

    @SuppressLint("DiscouragedApi") // The system Settings app owns this resource ID.
    fun resolve(context: Context): Intent? = runCatching {
        val packageManager = context.packageManager
        val intent = Intent(TETHER_SETTINGS_ACTION)
        val activity = packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_SYSTEM_ONLY,
        )?.activityInfo ?: return null
        if (!activity.exported) return null

        val resources = packageManager.getResourcesForApplication(activity.applicationInfo)
        val preferencesId = resources.getIdentifier("tether_prefs", "xml", activity.packageName)
        if (preferencesId == 0) return null

        val supportsVpnSharing = resources.getXml(preferencesId).use { parser ->
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG &&
                    parser.getAttributeValue(ANDROID_NAMESPACE, "key") == VPN_SHARING_KEY
                ) {
                    return@use true
                }
            }
            false
        }
        if (!supportsVpnSharing) return null

        intent.setClassName(activity.packageName, activity.name)
            .putExtra(FRAGMENT_ARGS_KEY, VPN_SHARING_KEY)
            .putExtra(SHOW_FRAGMENT_ARGS, Bundle().apply {
                putString(FRAGMENT_ARGS_KEY, VPN_SHARING_KEY)
            })
    }.getOrNull()
}
