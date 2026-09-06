/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.yumelira.yumebox.core.Clash
import com.github.yumelira.yumebox.presentation.component.AppDialog
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3OutlinedTextField
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AgeKeyGeneratorDialog(
    show: Boolean,
    hybrid: Boolean,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val spacing = AppTheme.spacing
    var secretKey by remember(show, hybrid) { mutableStateOf("") }
    var publicKey by remember(show, hybrid) { mutableStateOf("") }
    var generating by remember(show, hybrid) { mutableStateOf(false) }

    AppDialog(
        show = show,
        title = if (hybrid) MLang.MetaFeature.AgeKey.HybridTitle else MLang.MetaFeature.AgeKey.X25519Title,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.space12),
        ) {
            YumeMd3OutlinedTextField(
                value = secretKey,
                onValueChange = { secretKey = it },
                label = MLang.MetaFeature.AgeKey.SecretKey,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            YumeMd3OutlinedTextField(
                value = publicKey,
                onValueChange = { publicKey = it },
                label = MLang.MetaFeature.AgeKey.PublicKey,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            val derived = withContext(Dispatchers.Default) {
                                Clash.toPublicKeys(secretKey)?.firstOrNull()
                            }
                            if (!derived.isNullOrBlank()) publicKey = derived
                        }
                    },
                    enabled = secretKey.isNotBlank() && !generating,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(MLang.MetaFeature.AgeKey.DerivePublicKey)
                }
                TextButton(
                    onClick = {
                        if (generating) return@TextButton
                        generating = true
                        scope.launch {
                            val keyPair = withContext(Dispatchers.Default) {
                                if (hybrid) Clash.genHybridKeyPair() else Clash.genX25519KeyPair()
                            }
                            generating = false
                            if (keyPair != null) {
                                secretKey = keyPair.secretKey
                                publicKey = keyPair.publicKey
                            }
                        }
                    },
                    enabled = !generating,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(MLang.MetaFeature.AgeKey.Generate)
                }
            }
        }
    }
}
