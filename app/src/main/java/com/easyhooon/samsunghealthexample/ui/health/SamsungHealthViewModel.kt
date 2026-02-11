package com.easyhooon.samsunghealthexample.ui.health

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyhooon.samsunghealthexample.health.SamsungHealthManager
import com.easyhooon.samsunghealthexample.model.ExerciseData
import com.easyhooon.samsunghealthexample.model.HealthError
import com.easyhooon.samsunghealthexample.model.HourlyStepData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import com.samsung.android.sdk.health.data.error.AuthorizationException
import com.samsung.android.sdk.health.data.error.HealthDataException
import com.samsung.android.sdk.health.data.error.InvalidRequestException
import com.samsung.android.sdk.health.data.error.PlatformInternalException
import com.samsung.android.sdk.health.data.error.ResolvablePlatformException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SamsungHealthViewModel @Inject constructor(
    private val samsungHealthManager: SamsungHealthManager,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SamsungHealthState(
            permissionsGranted = false,
            todaySteps = 0L,
            weekSteps = 0L,
            todayExercises = persistentListOf(),
            exerciseDateLabel = "오늘",
            statusMessage = "Samsung Health 준비",
            errorLevel = null,
            isLoading = false,
        ),
    )
    val state: StateFlow<SamsungHealthState> = _state

    init {
        Timber.tag("SamsungHealthVM").d("ViewModel initialized")
        checkPermissions()
    }

    fun checkPermissions() {
        Timber.tag("SamsungHealthVM").d("checkPermissions()")
        viewModelScope.launch {
            try {
                val isAvailable = samsungHealthManager.isSamsungHealthAvailable()
                if (!isAvailable) {
                    _state.update { it.copy(statusMessage = "Samsung Health를 사용할 수 없습니다") }
                    return@launch
                }

                val hasPermissions = samsungHealthManager.hasAllPermissions()
                _state.update { currentState ->
                    currentState.copy(
                        permissionsGranted = hasPermissions,
                        statusMessage = when {
                            hasPermissions -> "권한이 승인되어 있습니다"
                            else -> "권한 승인이 필요합니다"
                        },
                        errorLevel = null,
                    )
                }
            } catch (e: HealthDataException) {
                handleHealthDataException(e)
            } catch (e: Exception) {
                _state.update { it.copy(statusMessage = "상태 확인 실패: ${e.message}") }
                Timber.tag("SamsungHealthVM").e(e, "Failed to check status")
            }
        }
    }

    fun requestPermissions(activity: Activity) {
        Timber.tag("SamsungHealthVM").d("requestPermissions()")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val isAvailable = samsungHealthManager.isSamsungHealthAvailable()
                if (!isAvailable) {
                    _state.update {
                        it.copy(
                            statusMessage = "Samsung Health를 사용할 수 없습니다",
                            isLoading = false,
                        )
                    }
                    return@launch
                }

                val result = samsungHealthManager.requestPermissions(activity)
                val hasAllPermissions = result.containsAll(samsungHealthManager.permissions)

                _state.update { currentState ->
                    currentState.copy(
                        permissionsGranted = hasAllPermissions,
                        statusMessage = if (hasAllPermissions) {
                            "권한이 승인되었습니다! (승인된 권한: ${result.size}/${samsungHealthManager.permissions.size})"
                        } else {
                            "권한이 거부되었습니다. 승인된 권한: ${result.size}/${samsungHealthManager.permissions.size}"
                        },
                        errorLevel = null,
                        isLoading = false,
                    )
                }
            } catch (e: ResolvablePlatformException) {
                Timber.tag("SamsungHealthVM").e(e, "Resolvable platform exception")
                if (e.hasResolution) {
                    try {
                        e.resolve(activity)
                    } catch (ex: Exception) {
                        _state.update {
                            it.copy(
                                statusMessage = "권한 요청 실패: ${ex.message}",
                                isLoading = false,
                            )
                        }
                        Timber.tag("SamsungHealthVM").e(ex, "Failed to resolve exception")
                    }
                }
                handleHealthDataException(e)
            } catch (e: HealthDataException) {
                handleHealthDataException(e)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        statusMessage = "권한 요청 실패: ${e.message}",
                        isLoading = false,
                    )
                }
                Timber.tag("SamsungHealthVM").e(e, "Failed to request permissions")
            }
        }
    }

    fun readSteps() {
        Timber.tag("SamsungHealthVM").d("readSteps()")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val hasPermissions = samsungHealthManager.hasAllPermissions()
                if (!hasPermissions) {
                    _state.update {
                        it.copy(
                            statusMessage = "권한이 필요합니다. 먼저 권한을 요청해주세요.",
                            isLoading = false,
                        )
                    }
                    return@launch
                }

                val todaySteps = samsungHealthManager.getTodaySteps()
                val weekSteps = samsungHealthManager.getStepsForDateRange(
                    LocalDate.now().minusDays(7),
                    LocalDate.now(),
                )
                val hourlySteps = samsungHealthManager.getHourlyStepsForToday()

                _state.update { currentState ->
                    currentState.copy(
                        todaySteps = todaySteps,
                        weekSteps = weekSteps,
                        hourlySteps = hourlySteps.toImmutableList(),
                        statusMessage = "걸음수 데이터를 성공적으로 가져왔습니다!",
                        errorLevel = null,
                        isLoading = false,
                    )
                }
            } catch (e: HealthDataException) {
                handleHealthDataException(e)
                _state.update { currentState ->
                    currentState.copy(
                        todaySteps = 0L,
                        weekSteps = 0L,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        statusMessage = "데이터 조회 실패: ${e.message}",
                        todaySteps = 0L,
                        weekSteps = 0L,
                        isLoading = false,
                    )
                }
                Timber.tag("SamsungHealthVM").e(e, "Failed to read data")
            }
        }
    }

    fun readExercises() {
        Timber.tag("SamsungHealthVM").d("readExercises()")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val hasPermissions = samsungHealthManager.hasAllPermissions()
                if (!hasPermissions) {
                    _state.update {
                        it.copy(
                            statusMessage = "권한이 필요합니다. 먼저 권한을 요청해주세요.",
                            isLoading = false,
                        )
                    }
                    return@launch
                }

                val exercises = samsungHealthManager.getTodayExercises()

                _state.update { currentState ->
                    currentState.copy(
                        todayExercises = exercises.toImmutableList(),
                        exerciseDateLabel = "오늘",
                        statusMessage = "운동 데이터를 성공적으로 가져왔습니다! (${exercises.size}개)",
                        errorLevel = null,
                        isLoading = false,
                    )
                }
            } catch (e: HealthDataException) {
                handleHealthDataException(e)
                _state.update { currentState ->
                    currentState.copy(
                        todayExercises = persistentListOf(),
                        exerciseDateLabel = "오늘",
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        statusMessage = "운동 데이터 조회 실패: ${e.message}",
                        todayExercises = persistentListOf(),
                        exerciseDateLabel = "오늘",
                        isLoading = false,
                    )
                }
                Timber.tag("SamsungHealthVM").e(e, "Failed to read exercises")
            }
        }
    }

    fun readExercisesForCustomDateRange(startDate: LocalDate, endDate: LocalDate) {
        Timber.tag("SamsungHealthVM").d("readExercisesForCustomDateRange($startDate ~ $endDate)")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val hasPermissions = samsungHealthManager.hasAllPermissions()
                if (!hasPermissions) {
                    _state.update {
                        it.copy(
                            statusMessage = "권한이 필요합니다. 먼저 권한을 요청해주세요.",
                            isLoading = false,
                        )
                    }
                    return@launch
                }

                val exercises = samsungHealthManager.getExercisesForDateRange(startDate, endDate)
                val dateLabel = "${startDate} ~ ${endDate}"

                _state.update { currentState ->
                    currentState.copy(
                        todayExercises = exercises.toImmutableList(),
                        exerciseDateLabel = dateLabel,
                        statusMessage = "${dateLabel} 운동 데이터 (${exercises.size}개)",
                        errorLevel = null,
                        isLoading = false,
                    )
                }
            } catch (e: HealthDataException) {
                handleHealthDataException(e)
                _state.update { currentState ->
                    currentState.copy(
                        todayExercises = persistentListOf(),
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        statusMessage = "운동 데이터 조회 실패: ${e.message}",
                        todayExercises = persistentListOf(),
                        isLoading = false,
                    )
                }
                Timber.tag("SamsungHealthVM").e(e, "Failed to read exercises for custom date range")
            }
        }
    }

    fun resolveError(activity: Activity) {
        val error = _state.value.errorLevel ?: return
        if (!error.resolvable) return

        val resolvableException = error.error as? ResolvablePlatformException ?: return
        try {
            resolvableException.resolve(activity)
            _state.update { it.copy(errorLevel = null) }
        } catch (e: Exception) {
            Timber.tag("SamsungHealthVM").e(e, "Failed to resolve error")
        }
    }

    private fun handleHealthDataException(healthDataException: HealthDataException) {
        val errorMessage = healthDataException.errorMessage
        val errorCode = healthDataException.errorCode ?: 0
        val healthError = HealthError(
            healthDataException,
            errorCode.toString(),
            errorMessage,
            false,
        )

        when (healthDataException) {
            is ResolvablePlatformException -> {
                if (healthDataException.hasResolution) {
                    Timber.tag("SamsungHealthVM").i(
                        "Resolvable Exception; message: ${healthDataException.errorMessage}",
                    )
                    healthError.error = healthDataException
                    healthError.resolvable = true
                }
            }

            is AuthorizationException -> {
                Timber.tag("SamsungHealthVM").i("Authorization Exception")
            }

            is InvalidRequestException -> {
                Timber.tag("SamsungHealthVM").i("Invalid Request Exception")
            }

            is PlatformInternalException -> {
                Timber.tag("SamsungHealthVM").i("Platform Internal Exception")
            }
        }

        _state.update { currentState ->
            currentState.copy(
                errorLevel = healthError,
                statusMessage = "오류 발생: $errorMessage (code: $errorCode)",
                isLoading = false,
            )
        }
    }
}

data class SamsungHealthState(
    val permissionsGranted: Boolean,
    val todaySteps: Long,
    val weekSteps: Long,
    val hourlySteps: ImmutableList<HourlyStepData> = persistentListOf(),
    val todayExercises: ImmutableList<ExerciseData>,
    val exerciseDateLabel: String,
    val statusMessage: String,
    val errorLevel: HealthError?,
    val isLoading: Boolean,
)
