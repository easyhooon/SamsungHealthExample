package com.easyhooon.samsunghealthexample.model

import androidx.compose.runtime.Immutable
import com.samsung.android.sdk.health.data.error.HealthDataException

@Immutable
data class HealthError(
    var error: HealthDataException,
    val errorCode: String,
    val errorMessage: String,
    var resolvable: Boolean,
)
