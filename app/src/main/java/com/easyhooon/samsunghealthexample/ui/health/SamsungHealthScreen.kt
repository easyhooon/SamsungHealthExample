package com.easyhooon.samsunghealthexample.ui.health

import android.app.Activity
import android.app.DatePickerDialog
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easyhooon.samsunghealthexample.model.ExerciseData
import com.easyhooon.samsunghealthexample.model.HealthError
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDate

@Composable
fun SamsungHealthScreen(
    modifier: Modifier = Modifier,
    viewModel: SamsungHealthViewModel = hiltViewModel(),
) {
    val activity = LocalActivity.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    SamsungHealthContent(
        modifier = modifier,
        state = state,
        activity = activity,
        onCheckPermissions = viewModel::checkPermissions,
        onRequestPermissions = viewModel::requestPermissions,
        onReadSteps = viewModel::readSteps,
        onReadExercises = viewModel::readExercises,
        onReadExercisesForDateRange = viewModel::readExercisesForCustomDateRange,
        onResolveError = viewModel::resolveError,
    )
}

@Composable
private fun SamsungHealthContent(
    modifier: Modifier = Modifier,
    state: SamsungHealthState,
    activity: Activity?,
    onCheckPermissions: () -> Unit,
    onRequestPermissions: (Activity) -> Unit,
    onReadSteps: () -> Unit,
    onReadExercises: () -> Unit,
    onReadExercisesForDateRange: (LocalDate, LocalDate) -> Unit,
    onResolveError: (Activity) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Samsung Health",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        StatusCard(statusMessage = state.statusMessage)

        Button(
            onClick = onCheckPermissions,
            enabled = !state.isLoading,
        ) {
            Text("상태 확인")
        }

        Button(
            onClick = { activity?.let { onRequestPermissions(it) } },
            enabled = !state.isLoading,
        ) {
            Text("Samsung Health 권한 요청")
        }

        StepsCard(
            todaySteps = state.todaySteps,
            weekSteps = state.weekSteps,
        )

        Button(
            onClick = onReadSteps,
            enabled = !state.isLoading,
        ) {
            Text(if (state.isLoading) "조회 중..." else "걸음수 조회")
        }

        ExerciseCard(
            exercises = state.todayExercises,
            dateLabel = state.exerciseDateLabel,
        )

        Button(
            onClick = onReadExercises,
            enabled = !state.isLoading,
        ) {
            Text(if (state.isLoading) "조회 중..." else "오늘 운동 데이터 조회")
        }

        DateRangeSelectorCard(
            isLoading = state.isLoading,
            onReadExercisesForDateRange = onReadExercisesForDateRange,
        )

        state.errorLevel?.let { error ->
            ErrorCard(
                error = error,
                onResolve = { activity?.let { onResolveError(it) } },
            )
        }
    }
}

@Composable
private fun StatusCard(statusMessage: String) {
    Card(
        modifier = Modifier.padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "상태",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StepsCard(
    todaySteps: Long,
    weekSteps: Long,
) {
    Card(
        modifier = Modifier.padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "걸음수 데이터",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "오늘: $todaySteps 걸음",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
            )
            Text(
                text = "이번 주: $weekSteps 걸음",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    exercises: ImmutableList<ExerciseData>,
    dateLabel: String,
) {
    Card(
        modifier = Modifier.padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "운동 데이터",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$dateLabel: ${exercises.size}개 운동",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            exercises.forEach { exercise ->
                ExerciseItem(exercise = exercise)
            }
        }
    }
}

@Composable
private fun ExerciseItem(exercise: ExerciseData) {
    Text(
        text = "${exercise.exerciseTypeName}: ${exercise.getDurationMinutes()}분, ${exercise.calorie.toInt()}kcal",
        style = MaterialTheme.typography.bodyMedium,
    )
    if (exercise.distance > 0) {
        Text(
            text = "  거리: ${"%.2f".format(exercise.getDistanceKm())}km",
            style = MaterialTheme.typography.bodySmall,
        )
    }
    if (exercise.meanHeartRate > 0) {
        Text(
            text = "  평균 심박수: ${exercise.meanHeartRate.toInt()}bpm, 최대: ${exercise.maxHeartRate.toInt()}bpm",
            style = MaterialTheme.typography.bodySmall,
        )
    }
    if (exercise.meanSpeed > 0) {
        Text(
            text = "  평균 속도: ${"%.2f".format(exercise.meanSpeed)}m/s",
            style = MaterialTheme.typography.bodySmall,
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun DateRangeSelectorCard(
    isLoading: Boolean,
    onReadExercisesForDateRange: (LocalDate, LocalDate) -> Unit,
) {
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    val context = LocalContext.current

    Card(
        modifier = Modifier.padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "날짜 범위 선택",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                startDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            startDate.year,
                            startDate.monthValue - 1,
                            startDate.dayOfMonth,
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "시작: $startDate",
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Button(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                endDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            endDate.year,
                            endDate.monthValue - 1,
                            endDate.dayOfMonth,
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "종료: $endDate",
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onReadExercisesForDateRange(startDate, endDate) },
                enabled = !isLoading,
            ) {
                Text(if (isLoading) "조회 중..." else "날짜 범위로 조회")
            }
        }
    }
}

@Composable
private fun ErrorCard(
    error: HealthError,
    onResolve: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "오류",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Code: ${error.errorCode}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Message: ${error.errorMessage}",
                style = MaterialTheme.typography.bodySmall,
            )
            if (error.resolvable) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onResolve) {
                    Text("문제 해결")
                }
            }
        }
    }
}