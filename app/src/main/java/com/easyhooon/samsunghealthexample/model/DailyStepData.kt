package com.easyhooon.samsunghealthexample.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

/**
 * 일자별 걸음 수 데이터 모델
 */
@Immutable
data class DailyStepData(
    val date: LocalDate,
    val stepCount: Long,
)

/**
 * 시간별 걸음 수 데이터 모델
 */
@Immutable
data class HourlyStepData(
    val hour: Int,
    val stepCount: Long,
)
