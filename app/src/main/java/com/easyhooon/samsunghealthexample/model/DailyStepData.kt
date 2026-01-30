package com.easyhooon.samsunghealthexample.model

import java.time.LocalDate

/**
 * 일자별 걸음 수 데이터 모델
 */
data class DailyStepData(
    val date: LocalDate,
    val stepCount: Long,
)
