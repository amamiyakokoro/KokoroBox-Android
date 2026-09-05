/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.data.integration.kokoro

import android.net.Uri
import com.github.yumelira.yumebox.screen.profiles.KokoroAccount
import com.github.yumelira.yumebox.screen.profiles.KokoroAccountClient
import com.github.yumelira.yumebox.screen.profiles.KokoroSubscriptionOptions
import com.github.yumelira.yumebox.screen.profiles.MihomoSubscriptionSettings
import com.github.yumelira.yumebox.screen.profiles.ResolvedSubscription

/** Shared process-memory Kokoro read cache and session invalidation boundary. */
class KokoroRepository(
    private val accountClient: KokoroAccountClient,
    private val rulesClient: KokoroCustomRulesClient,
) {
    private val accountCache = MemorySingleFlightCache<KokoroAccount?>(CACHE_TTL_MILLIS)
    private val subscriptionOptionsCache =
        MemorySingleFlightCache<KokoroSubscriptionOptions>(CACHE_TTL_MILLIS)
    private val rulesEditorCache =
        MemorySingleFlightCache<KokoroCustomRulesEditorData>(CACHE_TTL_MILLIS)

    internal fun hasSession(): Boolean = accountClient.hasSession()

    internal suspend fun getAccount(forceRefresh: Boolean = false): KokoroAccount? =
        accountCache.get(forceRefresh) {
            accountClient.getAccount().also { account ->
                if (account == null) invalidateSessionDependentData()
            }
        }

    internal suspend fun getSubscriptionOptions(
        account: KokoroAccount,
        forceRefresh: Boolean = false,
    ): KokoroSubscriptionOptions = try {
        subscriptionOptionsCache.get(forceRefresh) {
            accountClient.getSubscriptionOptions(account)
        }
    } catch (error: KokoroAuthenticationRequiredException) {
        invalidateAll()
        throw error
    }

    internal suspend fun getRulesEditorData(forceRefresh: Boolean = false): KokoroCustomRulesEditorData =
        try {
            rulesEditorCache.get(forceRefresh, rulesClient::getEditorData)
        } catch (error: KokoroAuthenticationRequiredException) {
            invalidateAll()
            throw error
        }

    internal suspend fun getFreshRulesOptions(): KokoroCustomRulesOptions =
        try {
            rulesClient.getOptions()
        } catch (error: KokoroAuthenticationRequiredException) {
            invalidateAll()
            throw error
        }

    internal suspend fun getFreshRulesState(): KokoroCustomRulesState =
        try {
            rulesClient.getState()
        } catch (error: KokoroAuthenticationRequiredException) {
            invalidateAll()
            throw error
        }

    internal suspend fun replaceRules(
        setId: Long,
        expectedRevision: Int,
        rules: List<KokoroCustomRuleInput>,
        options: KokoroCustomRulesOptions,
    ): KokoroRuleSet = try {
        try {
            rulesClient.replaceRules(setId, expectedRevision, rules, options)
        } catch (error: KokoroAuthenticationRequiredException) {
            invalidateAll()
            throw error
        }
    } finally {
        invalidateRules()
    }

    internal suspend fun beginLogin(): String = accountClient.beginLogin()

    internal suspend fun cancelLogin(loginUrl: String) = accountClient.cancelLogin(loginUrl)

    internal suspend fun handleOAuthCallback(uri: Uri) {
        accountClient.handleOAuthCallback(uri)
        invalidateAll()
    }

    internal suspend fun revoke() {
        try {
            accountClient.revoke()
        } finally {
            invalidateAll()
        }
    }

    internal suspend fun resolveSubscription(settings: MihomoSubscriptionSettings): ResolvedSubscription = try {
        accountClient.resolveSubscription(settings)
    } catch (error: KokoroAuthenticationRequiredException) {
        invalidateAll()
        throw error
    }

    internal fun invalidateAll() {
        accountCache.invalidate()
        invalidateSessionDependentData()
    }

    internal fun invalidateRules() {
        rulesEditorCache.invalidate()
    }

    private fun invalidateSessionDependentData() {
        subscriptionOptionsCache.invalidate()
        rulesEditorCache.invalidate()
    }

    private companion object {
        const val CACHE_TTL_MILLIS = 5L * 60L * 1_000L
    }
}
