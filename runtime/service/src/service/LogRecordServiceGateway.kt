/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
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
 * Copyright (c)  AmamiyaKokoro 2025 - Present
 *
 */



package com.amamiyakokoro.box.service

import android.app.Application
import com.amamiyakokoro.box.core.util.PollingTimerSpec
import com.amamiyakokoro.box.data.gateway.LogRecordGateway
import java.io.File

class LogRecordServiceGateway : LogRecordGateway {
    override val isRecording: Boolean
        get() = LogRecordService.isRecording

    override val currentLogFileName: String?
        get() = LogRecordService.currentLogFileName

    override val logPrefix: String
        get() = LogRecordService.LOG_PREFIX

    override val logSuffix: String
        get() = LogRecordService.LOG_SUFFIX

    override val stopWaitSpec: PollingTimerSpec
        get() = PollingTimerSpec(
            name = "log_record_stop_wait",
            intervalMillis = 300L,
            initialDelayMillis = 300L,
        )

    override fun start(application: Application) {
        LogRecordService.start(application)
    }

    override fun stop(application: Application) {
        LogRecordService.stop(application)
    }

    override fun getLogDir(application: Application): File {
        return LogRecordService.getLogDir(application)
    }
}
