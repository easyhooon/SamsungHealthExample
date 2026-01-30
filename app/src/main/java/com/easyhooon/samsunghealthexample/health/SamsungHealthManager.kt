package com.easyhooon.samsunghealthexample.health

import android.app.Activity
import com.easyhooon.samsunghealthexample.model.DailyStepData
import com.easyhooon.samsunghealthexample.model.ExerciseData
import com.easyhooon.samsunghealthexample.model.HeartRateSample
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.error.HealthDataException
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SamsungHealthManager @Inject constructor(
    private val healthDataStore: HealthDataStore,
) {
    // Samsung Health 권한
    val permissions = setOf(
        Permission.of(DataTypes.STEPS, AccessType.READ),
        Permission.of(DataTypes.EXERCISE, AccessType.READ),
    )

    /**
     * Samsung Health 권한 요청
     * @param activity 권한 요청을 실행할 Activity
     * @return 허용된 권한 Set
     */
    @Throws(HealthDataException::class)
    suspend fun requestPermissions(activity: Activity): Set<Permission> = withContext(Dispatchers.IO) {
        try {
            val result = healthDataStore.requestPermissions(permissions, activity)
            val grantedPermissions = result.joinToString(", ") {
                "${it.dataType.name.uppercase()}/${it.accessType}"
            }
            Timber.d("Permission request result: [$grantedPermissions]")
            result
        } catch (e: HealthDataException) {
            Timber.e(e, "Failed to request Samsung Health permissions")
            throw e
        }
    }

    /**
     * 모든 권한이 허용되었는지 확인
     */
    @Throws(HealthDataException::class)
    suspend fun hasAllPermissions(): Boolean = withContext(Dispatchers.IO) {
        try {
            val granted = healthDataStore.getGrantedPermissions(permissions)
            val result = granted.containsAll(permissions)
            Timber.d("hasAllPermissions: $result (granted: ${granted.size}/${permissions.size})")
            result
        } catch (e: HealthDataException) {
            Timber.e(e, "Failed to check Samsung Health permissions")
            false
        }
    }

    /**
     * 권한 상태 맵 반환
     */
    suspend fun getPermissionsStatus(): Map<String, Boolean> = withContext(Dispatchers.IO) {
        try {
            val granted = healthDataStore.getGrantedPermissions(permissions)
            mapOf(
                "STEPS" to (Permission.of(DataTypes.STEPS, AccessType.READ) in granted),
                "EXERCISE" to (Permission.of(DataTypes.EXERCISE, AccessType.READ) in granted),
            )
        } catch (e: HealthDataException) {
            Timber.e(e, "Failed to get permissions status")
            mapOf(
                "STEPS" to false,
                "EXERCISE" to false,
            )
        }
    }

    /**
     * 오늘의 걸음 수 조회
     */
    @Throws(HealthDataException::class)
    suspend fun getTodaySteps(): Long = withContext(Dispatchers.IO) {
        try {
            if (!hasAllPermissions()) {
                Timber.w("Samsung Health permissions not granted")
                return@withContext 0L
            }

            val startTime = LocalDate.now().atStartOfDay()
            val endTime = LocalDateTime.now()

            Timber.d("Querying steps for today: startTime=$startTime, endTime=$endTime")

            val localTimeFilter = LocalTimeFilter.of(startTime, endTime)
            val aggregateRequest = DataType.StepsType.TOTAL.requestBuilder
                .setLocalTimeFilter(localTimeFilter)
                .build()

            val result = healthDataStore.aggregateData(aggregateRequest)
            var stepCount = 0L
            result.dataList.forEach { aggregatedData ->
                aggregatedData.value?.let { stepCount = it }
            }

            Timber.d("Total steps calculated: $stepCount")
            stepCount
        } catch (e: HealthDataException) {
            Timber.e(e, "Failed to get today's steps")
            0L
        }
    }

    /**
     * 기간별 걸음 수 조회
     */
    @Throws(HealthDataException::class)
    suspend fun getStepsForDateRange(startDate: LocalDate, endDate: LocalDate): Long =
        withContext(Dispatchers.IO) {
            try {
                if (!hasAllPermissions()) {
                    Timber.w("Samsung Health permissions not granted")
                    return@withContext 0L
                }

                val startTime = startDate.atStartOfDay()
                val endTime = endDate.atTime(23, 59, 59)

                val localTimeFilter = LocalTimeFilter.of(startTime, endTime)
                val aggregateRequest = DataType.StepsType.TOTAL.requestBuilder
                    .setLocalTimeFilter(localTimeFilter)
                    .build()

                val result = healthDataStore.aggregateData(aggregateRequest)
                var stepCount = 0L
                result.dataList.forEach { aggregatedData ->
                    aggregatedData.value?.let { stepCount = it }
                }

                stepCount
            } catch (e: HealthDataException) {
                Timber.e(e, "Failed to get steps for date range")
                0L
            }
        }

    /**
     * 기간별 일자별 걸음 수 조회
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 일자별 걸음 수 리스트
     */
    @Throws(HealthDataException::class)
    suspend fun getDailyStepsForDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<DailyStepData> = withContext(Dispatchers.IO) {
        try {
            if (!hasAllPermissions()) {
                Timber.w("Samsung Health permissions not granted")
                return@withContext emptyList()
            }

            val dailySteps = mutableListOf<DailyStepData>()
            var currentDate = startDate

            // 각 날짜별로 걸음 수 조회
            while (!currentDate.isAfter(endDate)) {
                val startTime = currentDate.atStartOfDay()
                val endTime = currentDate.atTime(23, 59, 59)

                val localTimeFilter = LocalTimeFilter.of(startTime, endTime)
                val aggregateRequest = DataType.StepsType.TOTAL.requestBuilder
                    .setLocalTimeFilter(localTimeFilter)
                    .build()

                val result = healthDataStore.aggregateData(aggregateRequest)
                var stepCount = 0L
                result.dataList.forEach { aggregatedData ->
                    aggregatedData.value?.let { stepCount = it }
                }

                // 걸음 수가 0보다 큰 경우만 추가 (데이터가 있는 날짜만)
                if (stepCount > 0) {
                    dailySteps.add(
                        DailyStepData(
                            date = currentDate,
                            stepCount = stepCount,
                        ),
                    )
                }

                currentDate = currentDate.plusDays(1)
            }

            Timber.d("Daily steps retrieved: ${dailySteps.size} days with data")
            dailySteps
        } catch (e: HealthDataException) {
            Timber.e(e, "Failed to get daily steps for date range")
            emptyList()
        }
    }

    /**
     * 기간별 운동 데이터 조회
     */
    @Throws(HealthDataException::class)
    suspend fun getExercisesForDateRange(startDate: LocalDate, endDate: LocalDate): List<ExerciseData> =
        withContext(Dispatchers.IO) {
            try {
                if (!hasAllPermissions()) {
                    Timber.w("Samsung Health permissions not granted")
                    return@withContext emptyList()
                }

                val startTime = startDate.atStartOfDay()
                val endTime = endDate.atTime(23, 59, 59)

                Timber.d("Querying exercises for date range: startDate=$startDate, endDate=$endDate")

                val localTimeFilter = LocalTimeFilter.of(startTime, endTime)
                val request = DataTypes.EXERCISE.readDataRequestBuilder
                    .setLocalTimeFilter(localTimeFilter)
                    .build()

                val result = healthDataStore.readData(request)
                val exercises = mutableListOf<ExerciseData>()

                result.dataList.forEach { healthDataPoint ->
                    // HealthDataPoint에서 ExerciseSession 리스트 추출
                    val sessions = healthDataPoint.getValue(DataType.ExerciseType.SESSIONS) ?: emptyList()

                    sessions.forEachIndexed { index, session ->
                        // 고유 ID 생성: HealthDataPoint uid + session index
                        // 하나의 HealthDataPoint에 여러 ExerciseSession이 포함될 수 있으므로 index로 구분
                        val uniqueId = "${healthDataPoint.uid}_$index"

                        // 운동 중 측정된 심박수 데이터 추출
                        val heartRateSamples = session.log?.mapNotNull { exerciseLog ->
                            // heartRate가 null이 아닌 경우만 수집
                            exerciseLog.heartRate?.let { hr ->
                                HeartRateSample(
                                    timestamp = exerciseLog.timestamp.toEpochMilli(),
                                    heartRate = hr.toInt(),
                                )
                            }
                        }

                        exercises.add(
                            ExerciseData(
                                id = uniqueId,
                                exerciseType = session.exerciseType.ordinal,
                                exerciseTypeName = session.exerciseType.name,
                                startTime = session.startTime.toEpochMilli(),
                                endTime = session.endTime.toEpochMilli(),
                                duration = session.duration.toMillis(),
                                calorie = session.calories,
                                distance = session.distance ?: 0f,
                                meanHeartRate = session.meanHeartRate ?: 0f,
                                maxHeartRate = session.maxHeartRate ?: 0f,
                                meanSpeed = session.meanSpeed ?: 0f,
                                maxSpeed = session.maxSpeed ?: 0f,
                                heartRates = heartRateSamples,
                            ),
                        )
                    }
                }

                Timber.d("Found ${exercises.size} exercises for date range")
                exercises
            } catch (e: HealthDataException) {
                Timber.e(e, "Failed to get exercises for date range")
                emptyList()
            }
        }

    /**
     * 오늘의 운동 데이터 조회
     */
    @Throws(HealthDataException::class)
    suspend fun getTodayExercises(): List<ExerciseData> = withContext(Dispatchers.IO) {
        getExercisesForDateRange(LocalDate.now(), LocalDate.now())
    }

    /**
     * Samsung Health 사용 가능 여부 확인
     */
    fun isSamsungHealthAvailable(): Boolean {
        return try {
            // HealthDataService.getStore()가 정상적으로 반환되면 사용 가능
            healthDataStore != null
        } catch (e: Exception) {
            Timber.e(e, "Samsung Health is not available")
            false
        }
    }

}
