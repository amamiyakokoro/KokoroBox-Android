package com.github.yumelira.yumebox.data.integration.kokoro

import kotlinx.serialization.Serializable

/** All transforms must read and persist the entire credential record atomically. */
internal interface KokoroAuthStore {
    fun load(): StoredAuthData
    fun update(transform: (StoredAuthData) -> StoredAuthData)

    fun replaceTokens(replacement: StoredAuthData) = update { current ->
        replacement.copy(pendingLogin = current.pendingLogin)
    }

    fun clearTokens() = update { current -> StoredAuthData(pendingLogin = current.pendingLogin) }
}

@Serializable
internal data class StoredAuthData(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val accessTokenExpiresAt: Long = 0L,
    val refreshTokenExpiresAt: Long = 0L,
    val pendingLogin: PendingKokoroLogin? = null,
) {
    override fun toString(): String = "StoredAuthData([redacted])"
}
