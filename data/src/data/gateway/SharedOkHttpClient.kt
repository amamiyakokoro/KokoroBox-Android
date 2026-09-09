/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.data.gateway

import okhttp3.OkHttpClient

/**
 * Supplies process-local HTTP clients derived from one shared OkHttp transport.
 *
 * Callers must create a derived client for their own timeout, redirect, and retry policy.
 * [OkHttpClient.newBuilder] retains the underlying dispatcher and connection pool, while this
 * object deliberately remains process-local: Android processes cannot share JVM resources.
 */
object SharedOkHttpClient {
    private val baseClient: OkHttpClient by lazy { OkHttpClient() }

    fun newBuilder(): OkHttpClient.Builder = baseClient.newBuilder()
}
