/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.common.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocaleUtilTest {
    @Test
    fun flagAssetUsesNormalizedCountryCode() {
        assertEquals(
            "file:///android_asset/circle-flags/tw.svg",
            LocaleUtil.normalizeFlagAssetUri("TW"),
        )
    }

    @Test
    fun invalidCountryCodeDoesNotCreateAssetTraversal() {
        assertNull(LocaleUtil.normalizeFlagAssetUri("../tw"))
        assertNull(LocaleUtil.normalizeFlagAssetUri("T1"))
        assertNull(LocaleUtil.normalizeFlagAssetUri(""))
    }
}
