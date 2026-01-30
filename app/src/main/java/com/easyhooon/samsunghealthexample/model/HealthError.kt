package com.easyhooon.samsunghealthexample.model

import com.samsung.android.sdk.health.data.error.HealthDataException

data class HealthError(
    var error: HealthDataException,
    val errorCode: String,
    val errorMessage: String,
    var resolvable: Boolean,
)
