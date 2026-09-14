/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.screen.settings

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
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.Clash
import com.amamiyakokoro.box.presentation.component.AppActionBottomSheet
import com.amamiyakokoro.box.presentation.component.AppBottomSheetCloseAction
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3OutlinedTextField
import com.amamiyakokoro.box.presentation.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AgeKeyGeneratorSheet(
    show: Boolean,
    hybrid: Boolean,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val spacing = AppTheme.spacing
    var secretKey by remember(show, hybrid) { mutableStateOf("") }
    var publicKey by remember(show, hybrid) { mutableStateOf("") }
    var generating by remember(show, hybrid) { mutableStateOf(false) }

    AppActionBottomSheet(
        show = show,
        title = if (hybrid) {
            stringResource(LocaleR.string.meta_feature_age_key_hybrid_title)
        } else {
            stringResource(LocaleR.string.meta_feature_age_key_x25519_title)
        },
        onDismissRequest = onDismiss,
        startAction = {
            AppBottomSheetCloseAction(onClick = onDismiss)
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.space12),
        ) {
            YumeMd3OutlinedTextField(
                value = secretKey,
                onValueChange = { secretKey = it },
                label = stringResource(LocaleR.string.meta_feature_age_key_secret_key),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            YumeMd3OutlinedTextField(
                value = publicKey,
                onValueChange = { publicKey = it },
                label = stringResource(LocaleR.string.meta_feature_age_key_public_key),
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
                    Text(stringResource(LocaleR.string.meta_feature_age_key_derive_public_key))
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
                    Text(stringResource(LocaleR.string.meta_feature_age_key_generate))
                }
            }
        }
    }
}
