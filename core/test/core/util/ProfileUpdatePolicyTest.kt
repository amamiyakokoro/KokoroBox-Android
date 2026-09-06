/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c)  YumeLira 2025 - Present
 *
 */

package com.github.yumelira.yumebox.core.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileUpdatePolicyTest {
    private val hour = ProfileUpdatePolicy.RETRY_INTERVAL_MILLIS
    private val start = 100 * hour

    @Test
    fun failureRetriesAfterOneHourEvenWithLongSubscriptionInterval() {
        assertFalse(due(start + hour - 1, failed = true))
        assertTrue(due(start + hour, failed = true))
        assertFalse(due(start + 2 * hour - 1, failed = true, attempt = start + hour))
        assertTrue(due(start + 2 * hour, failed = true, attempt = start + hour))
    }

    @Test
    fun successReturnsToConfiguredInterval() {
        assertFalse(due(start + hour, failed = false))
        assertTrue(due(start + 24 * hour, failed = false))
    }

    @Test
    fun disabledSubscriptionDoesNotRetryFailures() {
        assertFalse(ProfileUpdatePolicy.isDue(start + hour, 0, start, true, start))
    }

    @Test
    fun existingProfilesUseLastSuccessfulUpdateUntilFirstAttempt() {
        assertFalse(ProfileUpdatePolicy.isDue(start + hour - 1, hour, 0, false, start))
        assertTrue(ProfileUpdatePolicy.isDue(start + hour, hour, 0, false, start))
    }

    @Test
    fun clockRollbackDoesNotBlockUpdates() {
        assertTrue(due(start - hour, failed = true))
    }

    private fun due(now: Long, failed: Boolean, attempt: Long = start) =
        ProfileUpdatePolicy.isDue(now, 24 * hour, attempt, failed, start)
}
