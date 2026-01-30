package com.easyhooon.samsunghealthexample.model

/**
 * 운동 데이터 모델
 */
data class ExerciseData(
    // HealthDataPoint의 uid + session index로 구성된 고유 식별자
    val id: String,
    val exerciseType: Int,
    val exerciseTypeName: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val calorie: Float,
    val distance: Float,
    val meanHeartRate: Float,
    val maxHeartRate: Float,
    val meanSpeed: Float,
    val maxSpeed: Float,
    // 운동 중 측정된 심박수 리스트 (시간, 심박수)
    val heartRates: List<HeartRateSample>? = null,
) {
    fun getDurationMinutes(): Long = duration / 60000

    fun getDistanceKm(): Float = distance / 1000f
}

/**
 * 심박수 샘플 데이터
 */
data class HeartRateSample(
    val timestamp: Long, // epoch millis
    val heartRate: Int,
)
